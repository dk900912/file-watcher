package io.github.dk900912.filewatcher.model;

import io.github.dk900912.filewatcher.utils.Assert;

import java.io.File;
import java.io.FileFilter;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static io.github.dk900912.filewatcher.model.ChangedFile.Type.ADD;
import static io.github.dk900912.filewatcher.model.ChangedFile.Type.DELETE;
import static io.github.dk900912.filewatcher.model.ChangedFile.Type.MODIFY;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

/**
 * A snapshot of a directory at a given point in time.
 *
 * @author dukui
 */
public class DirectorySnapshot {

    private static final Set<String> DOTS
            = Set.of(".", "..");

    private final File directory;

    private final LocalDateTime time;

    private final Set<FileSnapshot> files;

    /**
     * Create a new {@link DirectorySnapshot} for the given directory.
     *
     * @param directory the directory
     */
    public DirectorySnapshot(File directory) {
        Assert.notNull(directory, "Directory must not be null");
        Assert.isTrue(!directory.isFile(), () -> "Directory '" + directory + "' must not be a file");
        this.directory = directory;
        this.time = LocalDateTime.now();
        Set<FileSnapshot> files = new LinkedHashSet<>();
        collectFiles(directory, files);
        this.files = Collections.unmodifiableSet(files);
    }

    /**
     * Constructs a new DirectorySnapshot instance. This constructor is intended for internal use only.
     *
     * @param directory the directory to snapshot must not be null
     * @param time the exact snapshot capture time
     * @param files the immutable set of file snapshots
     */
    public DirectorySnapshot(File directory, LocalDateTime time, Set<FileSnapshot> files) {
        Assert.notNull(directory, "Directory must not be null");
        Assert.isTrue(!directory.isFile(), () -> "Directory '" + directory + "' must not be a file");
        this.directory = directory;
        this.time = time;
        this.files = files;
    }

    /**
     * Recursively collects directory snapshots from the specified directory and its subdirectories
     *
     * @param directory   The directory to process (recursively handles directories)
     * @param result The set to store collected directory snapshots (will be modified)
     */
    private void collectFiles(File directory, Set<FileSnapshot> result) {
        File[] children = directory.listFiles();
        // Process all entries in the current directory
        if (children != null) {
            for (File child : children) {
                // Recursively handle non-special directories (excluding "." and "..")
                if (child.isDirectory() && !DOTS.contains(child.getName())) {
                    collectFiles(child, result);
                } else if (child.isFile()) {
                    // Add files to a result set
                    result.add(new FileSnapshot(child));
                }
            }
        }
    }

    /**
     * Get the changed files between this snapshot and the given one.
     *
     * @param snapshot the previous snapshot
     * @param fileFilter the file filter
     * @return the changed files
     */
    public ChangedFiles getChangedFiles(DirectorySnapshot snapshot, FileFilter fileFilter) {
        Assert.notNull(snapshot, "DirectorySnapshot must not be null");
        File directory = this.directory;
        Assert.isTrue(snapshot.directory.equals(directory),
                () -> "DirectorySnapshot's directory must be '" + directory + "'");
        Set<ChangedFile> changes = new LinkedHashSet<>();
        // Map of previous files (this snapshot) with File as a key.
        // File equality is determined by path string comparison (case-sensitive on some OS)
        Map<File, FileSnapshot> previousFiles = getFilesMap();
        for (FileSnapshot currentFile : snapshot.files) {
            // Skip files not matching the filter
            if (acceptChangedFile(fileFilter, currentFile)) {
                // Remove and get the previous file snapshot by current file's path.
                // NOTE: File equality relies on path string comparison, not physical file identity.
                // This means files with different path representations (even if pointing to the same physical file)
                // will be considered different entries.
                FileSnapshot previousFile = previousFiles.remove(currentFile.getFile());
                if (previousFile == null) {
                    // Case 1: File added (including renamed files - old path will appear as DELETE later)
                    changes.add(new ChangedFile(directory, currentFile.getFile(), ADD));
                } else if (!previousFile.equals(currentFile)) {
                    // Case 2: File modified (content or metadata changed)
                    changes.add(new ChangedFile(directory, currentFile.getFile(), MODIFY));
                }
                // Case 3: File unchanged - no action
            }
        }
        // Remaining entries in previousFiles represent:
        // - Deleted files (an original path no longer exists)
        // - Renamed files (original path will appear here as DELETE, new path already registered as ADD)
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
     * Return the directory of this snapshot.
     * @return the directory
     */
    public File getDirectory() {
        return this.directory;
    }

    @Override
    public String toString() {
        return this.directory + " snapshot at " + ISO_LOCAL_DATE_TIME.format(this.time);
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

    private Set<FileSnapshot> filter(Set<FileSnapshot> snapshots, FileFilter filter) {
        if (filter == null) {
            return snapshots;
        }
        Set<FileSnapshot> filtered = new LinkedHashSet<>();
        for (FileSnapshot file : snapshots) {
            if (filter.accept(file.getFile())) {
                filtered.add(file);
            }
        }
        return filtered;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public Set<FileSnapshot> getFiles() {
        return files;
    }
}