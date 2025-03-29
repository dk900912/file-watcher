package io.github.dk900912.filewatcher.model;

import io.github.dk900912.filewatcher.utils.Assert;

import java.io.File;

/**
 * A snapshot of a File at a given point in time.
 *
 * @author dukui
 */
public class FileSnapshot {

    private final File file;

    private final boolean exists;

    private final long length;

    private final long lastModified;

    public FileSnapshot(File file) {
        Assert.notNull(file, "File must not be null");
        Assert.isTrue(file.isFile() || !file.exists(), "File must not be a directory");
        this.file = file;
        this.exists = file.exists();
        this.length = file.length();
        this.lastModified = file.lastModified();
    }

    /**
     * Constructs a new FileSnapshot instance. This constructor is intended for internal use only.
     *
     * @param file the file object to create a snapshot of
     * @param exists the existence status of the file
     * @param length the length of the file in bytes
     * @param lastModified the last modified timestamp of the file
     */
    public FileSnapshot(File file, boolean exists, long length, long lastModified) {
        Assert.notNull(file, "File must not be null");
        Assert.isTrue(file.isFile() || !file.exists(), "File must not be a directory");
        this.file = file;
        this.exists = exists;
        this.length = length;
        this.lastModified = lastModified;
    }

    public File getFile() {
        return this.file;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof FileSnapshot other) {
            boolean equals = this.file.equals(other.file);
            equals = equals && this.exists == other.exists;
            equals = equals && this.length == other.length;
            equals = equals && this.lastModified == other.lastModified;
            return equals;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hashCode = this.file.hashCode();
        hashCode = 31 * hashCode + Boolean.hashCode(this.exists);
        hashCode = 31 * hashCode + Long.hashCode(this.length);
        hashCode = 31 * hashCode + Long.hashCode(this.lastModified);
        return hashCode;
    }

    @Override
    public String toString() {
        return this.file.toString();
    }

    public boolean exists() {
        return exists;
    }

    public long getLength() {
        return length;
    }

    public long getLastModified() {
        return lastModified;
    }
}