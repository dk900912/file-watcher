package io.github.dk900912.filewatcher.model;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * A collections of files from a specific directory that have changed.
 *
 * @author dukui
 */
public final class ChangedFiles implements Iterable<ChangedFile> {

    private final File directory;

    private final Set<ChangedFile> files;

    public ChangedFiles(File directory, Set<ChangedFile> files) {
        this.directory = directory;
        this.files = Collections.unmodifiableSet(files);
    }

    public File getDirectory() {
        return this.directory;
    }

    @Override
    public Iterator<ChangedFile> iterator() {
        return getFiles().iterator();
    }

    /**
     * The files that have been changed.
     *
     * @return the changed files
     */
    public Set<ChangedFile> getFiles() {
        return this.files;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof ChangedFiles other) {
            return this.directory.equals(other.directory) && this.files.equals(other.files);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.files.hashCode();
    }

    @Override
    public String toString() {
        return this.directory + " " + this.files;
    }

}