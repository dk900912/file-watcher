package io.github.dk900912.filewatcher;

import io.github.dk900912.filewatcher.filter.FileFilterFactory;
import io.github.dk900912.filewatcher.listener.FileChangeListener;
import io.github.dk900912.filewatcher.model.ChangedFiles;
import io.github.dk900912.filewatcher.model.DirectorySnapshot;
import io.github.dk900912.filewatcher.utils.Assert;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Monitors designated directories for file system events and changes. While this class
 * is thread-safe, it is recommended to maintain a single instance to avoid redundant
 * resource utilization.
 *
 * @author dukui
 */
public class FileSystemWatcher {

    private final List<FileChangeListener> listeners = new ArrayList<>();

    private final SnapshotStateRepository snapshotStateRepository;

    private final Map<File, DirectorySnapshot> directories = new HashMap<>();

    private final FileWatcherProperties properties;

    private Thread watchThread;

    private FileFilter fileFilter;

    private final Object monitor = new Object();

    public FileSystemWatcher(FileWatcherProperties properties) {
        Assert.notNull(properties, "FileWatcherProperties must not be null");
        Assert.isTrue(properties.getDirectories() != null && !properties.getDirectories().isEmpty(),
                "FileWatcherProperties.directories must not be empty");
        this.properties = properties;
        for (String s : properties.getDirectories()) {
            File dir = new File(s);
            this.directories.put(dir, null);
        }
        this.fileFilter = FileFilterFactory.create(this.properties);
        this.snapshotStateRepository = properties.getSnapshotEnabled() ? SnapshotStateRepository.LOCAL : SnapshotStateRepository.NONE;
    }

    public void addListener(FileChangeListener fileChangeListener) {
        Assert.notNull(fileChangeListener, "FileChangeListener must not be null");
        synchronized (this.monitor) {
            checkNotStarted();
            this.listeners.add(fileChangeListener);
        }
    }

    /**
     * Typically, there is no need to replace the file filter, as a default {@link FileFilter}
     * is automatically provided based on the configuration in {@link FileWatcherProperties}.
     *
     * @param fileFilter the new {@link FileFilter} instance to set
     */
    public void replaceFileFilter(FileFilter fileFilter) {
        synchronized (this.monitor) {
            this.fileFilter = fileFilter;
        }
    }

    private void checkNotStarted() {
        synchronized (this.monitor) {
            Assert.state(this.watchThread == null, "FileSystemWatcher already started");
        }
    }

    /**
     * Start monitoring the directory for changes.
     */
    public void start() {
        synchronized (this.monitor) {
            createOrRestoreInitialSnapshots();
            if (this.watchThread == null) {
                Map<File, DirectorySnapshot> localDirectories = new HashMap<>(this.directories);
                Watcher watcher = new Watcher(this.properties.getRemainingScans(), new ArrayList<>(this.listeners), this.fileFilter,
                        this.properties.getPollInterval().toMillis(), this.properties.getQuietPeriod().toMillis(),
                        localDirectories, this.snapshotStateRepository);
                this.watchThread = new Thread(watcher);
                this.watchThread.setName(this.properties.getName());
                this.watchThread.setDaemon(this.properties.getDaemon());
                this.watchThread.start();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void createOrRestoreInitialSnapshots() {
        Map<File, DirectorySnapshot> restored = (Map<File, DirectorySnapshot>) this.snapshotStateRepository.restore();
        this.directories.replaceAll((f, v) -> {
            DirectorySnapshot restoredSnapshot = (restored != null) ? restored.get(f) : null;
            return (restoredSnapshot != null) ? restoredSnapshot : new DirectorySnapshot(f);
        });
    }

    /**
     * Stop monitoring the directories.
     */
    public void stop() {
        stopAfter(0);
    }

    /**
     * Stop monitoring the directories.
     *
     * @param remainingScans the number of remaining scans
     */
    void stopAfter(int remainingScans) {
        Thread thread;
        synchronized (this.monitor) {
            thread = this.watchThread;
            if (thread != null) {
                this.properties.getRemainingScans().set(remainingScans);
                if (remainingScans <= 0) {
                    thread.interrupt();
                }
            }
            this.watchThread = null;
        }
        if (thread != null && Thread.currentThread() != thread) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static final class Watcher implements Runnable {

        private final AtomicInteger remainingScans;

        private final List<FileChangeListener> listeners;

        private final FileFilter fileFilter;

        private final long pollInterval;

        private final long quietPeriod;

        private Map<File, DirectorySnapshot> directories;

        private final SnapshotStateRepository snapshotStateRepository;

        private Watcher(AtomicInteger remainingScans, List<FileChangeListener> listeners, FileFilter fileFilter,
                        long pollInterval, long quietPeriod, Map<File, DirectorySnapshot> directories,
                        SnapshotStateRepository snapshotStateRepository) {
            this.remainingScans = remainingScans;
            this.listeners = listeners;
            this.fileFilter = fileFilter;
            this.pollInterval = pollInterval;
            this.quietPeriod = quietPeriod;
            this.directories = directories;
            this.snapshotStateRepository = snapshotStateRepository;
        }

        @Override
        public void run() {
            int remainingScans = this.remainingScans.get();
            while (remainingScans > 0 || remainingScans == -1) {
                try {
                    if (remainingScans > 0) {
                        this.remainingScans.decrementAndGet();
                    }
                    scan();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                remainingScans = this.remainingScans.get();
            }
        }

        private void scan() throws InterruptedException {
            Thread.sleep(this.pollInterval - this.quietPeriod);
            Map<File, DirectorySnapshot> previous;
            Map<File, DirectorySnapshot> current = this.directories;
            do {
                previous = current;
                current = getCurrentSnapshots();
                Thread.sleep(this.quietPeriod);
            } while (isDifferent(previous, current));
            if (isDifferent(this.directories, current)) {
                updateSnapshots(current.values());
            }
        }

        private boolean isDifferent(Map<File, DirectorySnapshot> previous, Map<File, DirectorySnapshot> current) {
            if (!previous.keySet().equals(current.keySet())) {
                return true;
            }
            for (Map.Entry<File, DirectorySnapshot> entry : previous.entrySet()) {
                DirectorySnapshot previousDirectory = entry.getValue();
                DirectorySnapshot currentDirectory = current.get(entry.getKey());
                if (!previousDirectory.equals(currentDirectory, this.fileFilter)) {
                    return true;
                }
            }
            return false;
        }

        private Map<File, DirectorySnapshot> getCurrentSnapshots() {
            Map<File, DirectorySnapshot> snapshots = new LinkedHashMap<>();
            for (File directory : this.directories.keySet()) {
                snapshots.put(directory, new DirectorySnapshot(directory));
            }
            return snapshots;
        }

        private void updateSnapshots(Collection<DirectorySnapshot> snapshots) {
            Map<File, DirectorySnapshot> updated = new LinkedHashMap<>();
            Set<ChangedFiles> changeSet = new LinkedHashSet<>();
            for (DirectorySnapshot snapshot : snapshots) {
                updated.put(snapshot.getDirectory(), snapshot);
                DirectorySnapshot previous = this.directories.get(snapshot.getDirectory());
                ChangedFiles changedFiles = previous.getChangedFiles(snapshot, this.fileFilter);
                if (!changedFiles.getFiles().isEmpty()) {
                    changeSet.add(changedFiles);
                }
            }
            this.directories = updated;
            this.snapshotStateRepository.save(updated);
            if (!changeSet.isEmpty()) {
                fireListeners(Collections.unmodifiableSet(changeSet));
            }
        }

        private void fireListeners(Set<ChangedFiles> changeSet) {
            for (FileChangeListener listener : this.listeners) {
                listener.onChange(changeSet);
            }
        }
    }
}