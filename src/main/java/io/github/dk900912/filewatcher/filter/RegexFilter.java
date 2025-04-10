package io.github.dk900912.filewatcher.filter;

import io.github.dk900912.filewatcher.utils.Assert;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

/**
 * @author dukui
 */
public class RegexFilter implements FileFilter {

    private final Pattern pattern;

    public RegexFilter(String regex) {
        Assert.hasLength(regex, "Regex must not be null");
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public boolean accept(File pathname) {
        return pattern.matcher(pathname.getName()).matches();
    }
}