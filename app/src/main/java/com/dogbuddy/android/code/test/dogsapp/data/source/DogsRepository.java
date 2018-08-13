
package com.dogbuddy.android.code.test.dogsapp.data.source;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dogbuddy.android.code.test.dogsapp.data.Breed;
import com.dogbuddy.android.code.test.dogsapp.data.Dog;
import com.dogbuddy.android.code.test.dogsapp.util.EspressoIdlingResource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Concrete implementation to load tasks from the data sources into a cache.
 * <p>
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 *
 * //TODO: Implement this class using LiveData.
 */
public class DogsRepository implements DogsDataSource {

    private volatile static DogsRepository INSTANCE = null;

    private final DogsDataSource mDogsRemoteDataSource;

    private final DogsDataSource mDogsLocalDataSource;

    /**
     * This variables have package local visibility so it can be accessed from tests.
     */
    Map<String, Dog> mCachedDogs;
    Map<String, Breed> mCachedBreeds;

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    private boolean mCacheIsDirty = false;

    // Prevent direct instantiation.
    private DogsRepository(@NonNull DogsDataSource dogsRemoteDataSource,
                           @NonNull DogsDataSource dogsLocalDataSource) {
        mDogsRemoteDataSource = checkNotNull(dogsRemoteDataSource);
        mDogsLocalDataSource = checkNotNull(dogsLocalDataSource);
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @param dogsRemoteDataSource the backend data source
     * @param tasksLocalDataSource  the device storage data source
     * @return the {@link DogsRepository} instance
     */
    public static DogsRepository getInstance(DogsDataSource dogsRemoteDataSource,
                                             DogsDataSource tasksLocalDataSource) {
        if (INSTANCE == null) {
            synchronized (DogsRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DogsRepository(dogsRemoteDataSource, tasksLocalDataSource);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(DogsDataSource, DogsDataSource)} to create a new instance
     * next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    /**
     * Gets tasks from cache, local data source (SQLite) or remote data source, whichever is
     * available first.
     * <p>
     * Note: {@link LoadDogsCallback#onDataNotAvailable()} is fired if all data sources fail to
     * get the data.
     */
    @Override
    public void getDogs(@NonNull final LoadDogsCallback callback) {
        checkNotNull(callback);

        // Respond immediately with cache if available and not dirty
        if (mCachedDogs != null && !mCacheIsDirty) {
            callback.onDogsLoaded(new ArrayList<>(mCachedDogs.values()));
            return;
        }

        EspressoIdlingResource.increment(); // App is busy until further notice

        if (mCacheIsDirty) {
            // If the cache is dirty we need to fetch new data from the network.
            getDogsFromRemoteDataSource(callback);
        } else {
            // Query the local storage if available. If not, query the network.
            mDogsLocalDataSource.getDogs(new LoadDogsCallback() {
                @Override
                public void onDogsLoaded(List<Dog> dogs) {
                    refreshDogsCache(dogs);

                    EspressoIdlingResource.decrement(); // Set app as idle.
                    callback.onDogsLoaded(new ArrayList<>(mCachedDogs.values()));
                }

                @Override
                public void onDataNotAvailable() {
                    getDogsFromRemoteDataSource(callback);
                }
            });
        }
    }

    @Override
    public void saveDog(@NonNull Dog dog) {
        checkNotNull(dog);
        mDogsRemoteDataSource.saveDog(dog);
        mDogsLocalDataSource.saveDog(dog);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedDogs == null) {
            mCachedDogs = new LinkedHashMap<>();
        }
        mCachedDogs.put(dog.getId(), dog);
    }

    @Override
    public void getBreeds(@NonNull final LoadBreedsCallback callback) {
        checkNotNull(callback);

        // Respond immediately with cache if available
        if (mCachedBreeds != null) {
            callback.onBreedsLoaded(new ArrayList<>(mCachedBreeds.values()));
            return;
        }

        EspressoIdlingResource.increment(); // App is busy until further notice

        // Query the local storage if available. If not, query the network.
        mDogsLocalDataSource.getBreeds(new LoadBreedsCallback() {
            @Override
            public void onBreedsLoaded(List<Breed> breeds) {
                refreshBreedsCache(breeds);

                EspressoIdlingResource.decrement(); // Set app as idle.
                callback.onBreedsLoaded(new ArrayList<>(mCachedBreeds.values()));
            }

            @Override
            public void onDataNotAvailable() {
                getBreedsFromRemoteDataSource(callback);
            }
        });
    }

    @Override
    public void saveBreed(@NonNull Breed breed) {
        checkNotNull(breed);
        mDogsRemoteDataSource.saveBreed(breed);
        mDogsLocalDataSource.saveBreed(breed);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedBreeds == null) {
            mCachedBreeds = new LinkedHashMap<>();
        }
        mCachedBreeds.put(breed.getId(), breed);
    }


    /**
     * Gets tasks from local data source (sqlite) unless the table is new or empty. In that case it
     * uses the network data source. This is done to simplify the sample.
     * <p>
     * Note: {@link GetDogCallback#onDataNotAvailable()} is fired if both data sources fail to
     * get the data.
     */
    @Override
    public void getDog(@NonNull final String taskId, @NonNull final GetDogCallback callback) {
        checkNotNull(taskId);
        checkNotNull(callback);

        Dog cachedDog = getDogWithId(taskId);

        // Respond immediately with cache if available
        if (cachedDog != null) {
            callback.onDogLoaded(cachedDog);
            return;
        }

        EspressoIdlingResource.increment(); // App is busy until further notice

        // Load from server/persisted if needed.

        // Is the dog in the local data source? If not, query the network.
        mDogsLocalDataSource.getDog(taskId, new GetDogCallback() {
            @Override
            public void onDogLoaded(Dog dog) {
                // Do in memory cache update to keep the app UI up to date
                if (mCachedDogs == null) {
                    mCachedDogs = new LinkedHashMap<>();
                }
                mCachedDogs.put(dog.getId(), dog);

                EspressoIdlingResource.decrement(); // Set app as idle.

                callback.onDogLoaded(dog);
            }

            @Override
            public void onDataNotAvailable() {
                mDogsRemoteDataSource.getDog(taskId, new GetDogCallback() {
                    @Override
                    public void onDogLoaded(Dog dog) {
                        if (dog == null) {
                            onDataNotAvailable();
                            return;
                        }
                        // Do in memory cache update to keep the app UI up to date
                        if (mCachedDogs == null) {
                            mCachedDogs = new LinkedHashMap<>();
                        }
                        mCachedDogs.put(dog.getId(), dog);
                        EspressoIdlingResource.decrement(); // Set app as idle.

                        callback.onDogLoaded(dog);
                    }

                    @Override
                    public void onDataNotAvailable() {
                        EspressoIdlingResource.decrement(); // Set app as idle.

                        callback.onDataNotAvailable();
                    }
                });
            }
        });
    }

    @Override
    public void refreshDogs() {
        mCacheIsDirty = true;
    }

    @Override
    public void deleteAllDogs() {
        mDogsRemoteDataSource.deleteAllDogs();
        mDogsLocalDataSource.deleteAllDogs();

        if (mCachedDogs == null) {
            mCachedDogs = new LinkedHashMap<>();
        }
        mCachedDogs.clear();
    }

    @Override
    public void deleteAllBreeds() {
        mDogsRemoteDataSource.deleteAllBreeds();
        mDogsLocalDataSource.deleteAllBreeds();

        if (mCachedBreeds == null) {
            mCachedBreeds = new LinkedHashMap<>();
        }
        mCachedBreeds.clear();
    }

    @Override
    public void deleteDog(@NonNull String taskId) {
        mDogsRemoteDataSource.deleteDog(checkNotNull(taskId));
        mDogsLocalDataSource.deleteDog(checkNotNull(taskId));

        mCachedDogs.remove(taskId);
    }

    private void getDogsFromRemoteDataSource(@NonNull final LoadDogsCallback callback) {
        mDogsRemoteDataSource.getDogs(new LoadDogsCallback() {
            @Override
            public void onDogsLoaded(List<Dog> dogs) {
                refreshDogsCache(dogs);
                refreshDogsLocalDataSource(dogs);

                EspressoIdlingResource.decrement(); // Set app as idle.
                callback.onDogsLoaded(new ArrayList<>(mCachedDogs.values()));
            }

            @Override
            public void onDataNotAvailable() {

                EspressoIdlingResource.decrement(); // Set app as idle.
                callback.onDataNotAvailable();
            }
        });
    }

    private void getBreedsFromRemoteDataSource(@NonNull final LoadBreedsCallback callback) {
        mDogsRemoteDataSource.getBreeds(new LoadBreedsCallback() {
            @Override
            public void onBreedsLoaded(List<Breed> breeds) {
                refreshBreedsCache(breeds);
                refreshBreedsLocalDataSource(breeds);

                EspressoIdlingResource.decrement(); // Set app as idle.
                callback.onBreedsLoaded(new ArrayList<>(mCachedBreeds.values()));
            }

            @Override
            public void onDataNotAvailable() {

                EspressoIdlingResource.decrement(); // Set app as idle.
                callback.onDataNotAvailable();
            }
        });
    }

    private void refreshDogsCache(List<Dog> dogs) {
        if (mCachedDogs == null) {
            mCachedDogs = new LinkedHashMap<>();
        }
        mCachedDogs.clear();
        for (Dog dog : dogs) {
            mCachedDogs.put(dog.getId(), dog);
        }
        mCacheIsDirty = false;
    }

    private void refreshBreedsCache(List<Breed> breeds) {
        if (mCachedBreeds == null) {
            mCachedBreeds = new LinkedHashMap<>();
        }
        mCachedBreeds.clear();
        for (Breed breed : breeds) {
            mCachedBreeds.put(breed.getId(), breed);
        }
    }

    private void refreshDogsLocalDataSource(List<Dog> dogs) {
        mDogsLocalDataSource.deleteAllDogs();
        for (Dog dog : dogs) {
            mDogsLocalDataSource.saveDog(dog);
        }
    }

    private void refreshBreedsLocalDataSource(List<Breed> breeds) {
        mDogsLocalDataSource.deleteAllBreeds();
        for (Breed breed : breeds) {
            mDogsLocalDataSource.saveBreed(breed);
        }
    }

    @Nullable
    private Dog getDogWithId(@NonNull String id) {
        checkNotNull(id);
        if (mCachedDogs == null || mCachedDogs.isEmpty()) {
            return null;
        } else {
            return mCachedDogs.get(id);
        }
    }
}
