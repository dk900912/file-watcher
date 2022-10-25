package io.github.dk900912.filewatcher.model;

import io.github.dk900912.filewatcher.utils.Assert;
import io.github.dk900912.filewatcher.utils.StringUtil;

import java.io.File;

/**
 * A single file that has changed.
 *
 * @author dukui
 * @see ChangedFiles
 */
public final class ChangedFile {

    private final File sourceDirectory;

    private final File file;

    private final Type type;

    /**
     * Create a new {@link ChangedFile} instance.
     * @param sourceDirectory the source directory
     * @param file the file
     * @param type the type of change
     */
    public ChangedFile(File sourceDirectory, File file, Type type) {
        Assert.notNull(sourceDirectory, "SourceDirectory must not be null");
        Assert.notNull(file, "File must not be null");
        Assert.notNull(type, "Type must not be null");
        this.sourceDirectory = sourceDirectory;
        this.file = file;
        this.type = type;
    }

    public File getFile() {
        return this.file;
    }

    public Type getType() {
        return this.type;
    }

    /**
     * Return the name of the file relative to the source directory.
     * @return the relative name
     */
    public String getRelativeName() {
        File directory = this.sourceDirectory.getAbsoluteFile();
        File file = this.file.getAbsoluteFile();
        String directoryName = StringUtil.cleanPath(directory.getPath());
        String fileName = StringUtil.cleanPath(file.getPath());
        Assert.state(fileName.startsWith(directoryName),
                () -> "The file " + fileName + " is not contained in the source directory " + directoryName);
        return fileName.substring(directoryName.length() + 1);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof ChangedFile) {
            ChangedFile other = (ChangedFile) obj;
            return this.file.equals(other.file) && this.type.equals(other.type);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.file.hashCode() * 31 + this.type.hashCode();
    }

    @Override
    public String toString() {
        return this.file + " (" + this.type + ")";
    }

    /**
     * Change types.
     */
    public enum Type {

        /**
         * A new file has been added.
         */
        ADD,

        /**
         * An existing file has been modified.
         */
        MODIFY,

        /**
         * An existing file has been deleted.
         */
        DELETE

    }

}