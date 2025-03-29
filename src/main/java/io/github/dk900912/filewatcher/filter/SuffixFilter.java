package io.github.dk900912.filewatcher.filter;

import io.github.dk900912.filewatcher.utils.Assert;

import java.io.File;
import java.io.FileFilter;
import java.util.Set;

/**
 * @author dk900912
 */
public class SuffixFilter implements FileFilter {
    private final SuffixMatchingStrategy strategy;

    public SuffixFilter(Set<String> suffixes) {
        Assert.state(suffixes != null && !suffixes.isEmpty(), "Suffixes must not be null");
        this.strategy = MatchingStrategyFactory.forSuffix()
                .withSuffixes(suffixes)
                .build();
    }

    @Override
    public boolean accept(File pathname) {
        return strategy.matches(pathname.getName());
    }
}