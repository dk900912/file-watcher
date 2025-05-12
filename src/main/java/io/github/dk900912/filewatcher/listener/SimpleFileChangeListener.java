package io.github.dk900912.filewatcher.listener;

import io.github.dk900912.filewatcher.model.ChangedFile;
import io.github.dk900912.filewatcher.model.ChangedFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Simple implementation for {@link FileChangeListener}, just logging.
 *
 * @author dukui
 */
public class SimpleFileChangeListener implements FileChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(SimpleFileChangeListener.class);

    @Override
    public void onChange(Set<ChangedFiles> changeSet) {
        for (ChangedFiles changedFiles : changeSet) {
            for (ChangedFile changedFile : changedFiles) {
                logger.info("o=={======> {} <======}==o", changedFile);
            }
        }
    }

}