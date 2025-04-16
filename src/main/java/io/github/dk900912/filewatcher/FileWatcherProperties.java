package io.github.dk900912.filewatcher;


import io.github.dk900912.filewatcher.filter.MatchingStrategy;
import io.github.dk900912.filewatcher.utils.Assert;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dukui
 */
public class FileWatcherProperties {

    // Daemon thread by default
    private Boolean daemon = true;

    // Thread name by default
    private String name = "File Watcher";

    // Scan forever by default
    private AtomicInteger remainingScans = new AtomicInteger(-1);

    private Boolean snapshotEnabled = false;

    private List<String> directories;

    private MatchingStrategy acceptedStrategy = MatchingStrategy.ANY;

    private Set<String> acceptedStrategyPatterns;

    private Duration pollInterval = Duration.ofMillis(1000);

    private Duration quietPeriod = Duration.ofMillis(400);

    public Boolean getDaemon() {
        return daemon;
    }

    public void setDaemon(Boolean daemon) {
        this.daemon = daemon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AtomicInteger getRemainingScans() {
        return remainingScans;
    }

    public void setRemainingScans(AtomicInteger remainingScans) {
        this.remainingScans = remainingScans;
    }

    public Boolean getSnapshotEnabled() {
        return snapshotEnabled;
    }

    public void setSnapshotEnabled(Boolean snapshotEnabled) {
        this.snapshotEnabled = snapshotEnabled;
    }

    public List<String> getDirectories() {
        return directories;
    }

    public void setDirectories(List<String> directories) {
        this.directories = directories;
    }

    public MatchingStrategy getAcceptedStrategy() {
        return acceptedStrategy;
    }

    public void setAcceptedStrategy(MatchingStrategy acceptedStrategy) {
        this.acceptedStrategy = acceptedStrategy;
    }

    public Set<String> getAcceptedStrategyPatterns() {
        return acceptedStrategyPatterns;
    }

    public void setAcceptedStrategyPatterns(Set<String> acceptedStrategyPatterns) {
        this.acceptedStrategyPatterns = acceptedStrategyPatterns;
    }

    public Duration getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(Duration pollInterval) {
        Assert.state(pollInterval.toMillis() > 0, "PollInterval must be positive");
        Assert.state(pollInterval.toMillis() > getQuietPeriod().toMillis(), "PollInterval must be greater than QuietPeriod");
        this.pollInterval = pollInterval;
    }

    public Duration getQuietPeriod() {
        return quietPeriod;
    }

    public void setQuietPeriod(Duration quietPeriod) {
        Assert.state(quietPeriod.toMillis() > 0, "QuietPeriod must be positive");
        Assert.state(getPollInterval().toMillis() > quietPeriod.toMillis(), "PollInterval must be greater than QuietPeriod");
        this.quietPeriod = quietPeriod;
    }
}

