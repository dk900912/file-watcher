package io.github.dk900912.filewatcher.filter;

import io.github.dk900912.filewatcher.utils.Assert;

import java.io.File;
import java.io.FileFilter;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author dukui
 */
public class RegexFilter implements FileFilter {

    private final Set<Pattern> patterns;

    public RegexFilter(Set<String> regexes) {
        Assert.isTrue(regexes != null && !regexes.isEmpty(), "Regexes must not be empty");
        this.patterns = regexes.stream()
                .map(Pattern::compile)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean accept(File pathname) {
        return patterns.stream()
                .anyMatch(pattern -> pattern.matcher(pathname.getName()).matches());
    }
}