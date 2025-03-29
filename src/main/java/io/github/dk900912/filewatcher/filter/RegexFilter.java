package io.github.dk900912.filewatcher.filter;

import io.github.dk900912.filewatcher.utils.Assert;

import java.io.File;
import java.io.FileFilter;

/**
 * @author dk900912
 */
public class RegexFilter implements FileFilter {
    private final RegexMatchingStrategy strategy;

    public RegexFilter(String pattern) {
        Assert.hasLength(pattern, "Pattern must not be null or empty");
        this.strategy = MatchingStrategyFactory.forRegex()
                .withPattern(pattern)
                .build();
    }

    @Override
    public boolean accept(File pathname) {
        return strategy.matches(pathname.getName());
    }
}