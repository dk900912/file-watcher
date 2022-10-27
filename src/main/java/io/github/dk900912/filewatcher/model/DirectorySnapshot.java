package io.github.dk900912.filewatcher.model;

import io.github.dk900912.filewatcher.utils.Assert;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static io.github.dk900912.filewatcher.model.ChangedFile.Type.ADD;
import static io.github.dk900912.filewatcher.model.ChangedFile.Type.DELETE;
import static io.github.dk900912.filewatcher.model.ChangedFile.Type.MODIFY;

/**
 * A snapshot of a directory at a given point in time.
 *
 * @author dukui
 */
public final class DirectorySnapshot {

    private static final Set<String> DOTS
            = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(".", "..")));

    private final File directory;

    private final Date time;

    private final Set<FileSnapshot> files;

    /**
     * Create a new {@link DirectorySnapshot} for the given directory.
     * @param directory the source directory
     */
    public DirectorySnapshot(File directory) {
        Assert.notNull(directory, "Directory must not be null");
        Assert.isTrue(!directory.isFile(), () -> "Directory '" + directory + "' must not be a file");
        this.directory = directory;
        this.time = new Date();
        Set<FileSnapshot> files = new LinkedHashSet<>();
        collectFiles(directory, files);
        this.files = Collections.unmodifiableSet(files);
    }

    private void collectFiles(File source, Set<FileSnapshot> result) {
        File[] children = source.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory() && !DOTS.contains(child.getName())) {
                    collectFiles(child, result);
                } else if (child.isFile()) {
                    result.add(new FileSnapshot(child));
                }
            }
        }
    }

    public ChangedFiles getChangedFiles(DirectorySnapshot snapshot, FileFilter fileFilter) {
        Assert.notNull(snapshot, "Snapshot must not be null");
        File directory = this.directory;
        Assert.isTrue(snapshot.directory.equals(directory),
                () -> "Snapshot source directory must be '" + directory + "'");
        Set<ChangedFile> changes = new LinkedHashSet<>();
        Map<File, FileSnapshot> previousFiles = getFilesMap();
        for (FileSnapshot currentFile : snapshot.files) {
            if (acceptChangedFile(fileFilter, currentFile)) {
                FileSnapshot previousFile = previousFiles.remove(currentFile.getFile());
                if (previousFile == null) {
                    changes.add(new ChangedFile(directory, currentFile.getFile(), ADD));
                } else if (!previousFile.equals(currentFile)) {
                    changes.add(new ChangedFile(directory, currentFile.getFile(), MODIFY));
                }
            }
        }
        for (FileSnapshot previousFile : previousFiles.values()) {
            if (acceptChangedFile(fileFilter, previousFile)) {
                changes.add(new ChangedFile(directory, previousFile.getFile(), DELETE));
            }
        }
        return new ChangedFiles(directory, changes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof DirectorySnapshot) {
            return equals((DirectorySnapshot) obj, null);
        }
        return super.equals(obj);
    }

    public boolean equals(DirectorySnapshot other, FileFilter filter) {
        if (this.directory.equals(other.directory)) {
            Set<FileSnapshot> ourFiles = filter(this.files, filter);
            Set<FileSnapshot> otherFiles = filter(other.files, filter);
            return ourFiles.equals(otherFiles);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * this.directory.hashCode() + this.files.hashCode();
    }

    /**
     * Return the source directory of this snapshot.
     * @return the source directory
     */
    public File getDirectory() {
        return this.directory;
    }

    @Override
    public String toString() {
        return this.directory + " snapshot at " + this.time;
    }

    private boolean acceptChangedFile(FileFilter fileFilter, FileSnapshot file) {
        return (fileFilter == null || fileFilter.accept(file.getFile()));
    }

    private Map<File, FileSnapshot> getFilesMap() {
        Map<File, FileSnapshot> files = new LinkedHashMap<>();
        for (FileSnapshot file : this.files) {
            files.put(file.getFile(), file);
        }
        return files;
    }

    private Set<FileSnapshot> filter(Set<FileSnapshot> source, FileFilter filter) {
        if (filter == null) {
            return source;
        }
        Set<FileSnapshot> filtered = new LinkedHashSet<>();
        for (FileSnapshot file : source) {
            if (filter.accept(file.getFile())) {
                filtered.add(file);
            }
        }
        return filtered;
    }

}