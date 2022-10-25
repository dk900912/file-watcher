package io.github.dk900912.filewatcher;

/**
 * Repository used by {@link FileSystemWatcher} to save file/directory snapshots.
 *
 * @author dukui
 */
public interface SnapshotStateRepository {

    /**
     * A No-op {@link SnapshotStateRepository} that does not save state.
     */
    SnapshotStateRepository NONE = new SnapshotStateRepository() {

        @Override
        public void save(Object state) {
        }

        @Override
        public Object restore() {
            return null;
        }

    };

    /**
     * A {@link SnapshotStateRepository} that uses a static instance to keep state.
     */
    SnapshotStateRepository STATIC = StaticSnapshotStateRepository.INSTANCE;

    /**
     * Save the given state in the repository.
     * @param state the state to save
     */
    void save(Object state);

    /**
     * Restore any previously saved state.
     * @return the previously saved state or {@code null}
     */
    Object restore();

}