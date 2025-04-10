package io.github.dk900912.filewatcher.filter;

import java.io.File;
import java.io.FileFilter;

/**
 * @author dukui
 */
public class AnyFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        return true;
    }
}
