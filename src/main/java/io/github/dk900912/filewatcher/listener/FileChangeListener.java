package io.github.dk900912.filewatcher.listener;

import io.github.dk900912.filewatcher.model.ChangedFiles;

import java.util.Set;

/**
 * Callback interface when file changes are detected.
 *
 * @author dukui
 */
@FunctionalInterface
public interface FileChangeListener {

    /**
     * Called when files have been changed.
     *
     * @param changeSet a set of the {@link ChangedFiles}
     */
    void onChange(Set<ChangedFiles> changeSet);

}