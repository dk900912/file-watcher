package io.github.dk900912.filewatcher;

/**
 * {@link SnapshotStateRepository} that uses a single static instance.
 *
 * @author dukui
 */
public class StaticSnapshotStateRepository implements SnapshotStateRepository {

    static final StaticSnapshotStateRepository INSTANCE = new StaticSnapshotStateRepository();

    private volatile Object state;

    @Override
    public void save(Object state) {
        this.state = state;
    }

    @Override
    public Object restore() {
        return this.state;
    }

}
