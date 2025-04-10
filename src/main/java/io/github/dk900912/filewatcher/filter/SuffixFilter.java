package io.github.dk900912.filewatcher.filter;

import io.github.dk900912.filewatcher.utils.Assert;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author dukui
 */
public class SuffixFilter implements FileFilter {

    private final Set<String> suffixes;

    public SuffixFilter(Set<String> suffixes) {
        Assert.state(suffixes != null && !suffixes.isEmpty(), "Suffixes must not be empty");
        this.suffixes = new LinkedHashSet<>(suffixes);
    }

    @Override
    public boolean accept(File pathname) {
        String fileName = pathname.getName();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return false;
        }
        String actualSuffix = fileName.substring(lastDotIndex + 1);
        return suffixes.stream()
                .map(String::toLowerCase)
                .map(suffix -> {
                    if (suffix.lastIndexOf('.') == -1) {
                        return suffix;
                    }
                    return suffix.substring(suffix.lastIndexOf('.') + 1);
                })
                .anyMatch(suffix -> suffix.equalsIgnoreCase(actualSuffix));
    }
}