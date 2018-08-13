
package com.dogbuddy.android.code.test.dogsapp.data.source.local;

import static com.google.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.dogbuddy.android.code.test.dogsapp.data.Breed;
import com.dogbuddy.android.code.test.dogsapp.data.Dog;
import com.dogbuddy.android.code.test.dogsapp.data.source.DogsDataSource;
import com.dogbuddy.android.code.test.dogsapp.util.AppExecutors;

import java.util.List;


/**
 * Concrete implementation of a data source as a db.
 */
public class DogsLocalDataSource implements DogsDataSource {

    private static volatile DogsLocalDataSource INSTANCE;

    private DogsDao mDogsDao;

    private AppExecutors mAppExecutors;

    // Prevent direct instantiation.
    private DogsLocalDataSource(@NonNull AppExecutors appExecutors,
                                @NonNull DogsDao dogsDao) {
        mAppExecutors = appExecutors;
        mDogsDao = dogsDao;
    }

    public static DogsLocalDataSource getInstance(@NonNull AppExecutors appExecutors,
                                                  @NonNull DogsDao dogsDao) {
        if (INSTANCE == null) {
            synchronized (DogsLocalDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DogsLocalDataSource(appExecutors, dogsDao);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Note: {@link LoadDogsCallback#onDataNotAvailable()} is fired if the database doesn't exist
     * or the table is empty.
     */
    @Override
    public void getDogs(@NonNull final LoadDogsCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final List<Dog> dogs = mDogsDao.getDogs();
                mAppExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (dogs.isEmpty()) {
                            // This will be called if the table is new or just empty.
                            callback.onDataNotAvailable();
                        } else {
                            callback.onDogsLoaded(dogs);
                        }
                    }
                });
            }
        };

        mAppExecutors.diskIO().execute(runnable);
    }

    /**
     * Note: {@link GetDogCallback#onDataNotAvailable()} is fired if the {@link Dog} isn't
     * found.
     */
    @Override
    public void getDog(@NonNull final String taskId, @NonNull final GetDogCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final Dog dog = mDogsDao.getDogById(taskId);

                mAppExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (dog != null) {
                            callback.onDogLoaded(dog);
                        } else {
                            callback.onDataNotAvailable();
                        }
                    }
                });
            }
        };

        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void saveDog(@NonNull final Dog dog) {
        checkNotNull(dog);
        Runnable saveRunnable = new Runnable() {
            @Override
            public void run() {
                mDogsDao.insertDog(dog);
            }
        };
        mAppExecutors.diskIO().execute(saveRunnable);
    }

    @Override
    public void getBreeds(final @NonNull LoadBreedsCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final List<Breed> breeds = mDogsDao.getBreeds();
                mAppExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (breeds.isEmpty()) {
                            // This will be called if the table is new or just empty.
                            callback.onDataNotAvailable();
                        } else {
                            callback.onBreedsLoaded(breeds);
                        }
                    }
                });
            }
        };

        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void saveBreed(final @NonNull Breed breed) {
        checkNotNull(breed);
        Runnable saveRunnable = new Runnable() {
            @Override
            public void run() {
                mDogsDao.insertBreed(breed);
            }
        };
        mAppExecutors.diskIO().execute(saveRunnable);
    }

    @Override
    public void deleteAllBreeds() {
        Runnable deleteRunnable = new Runnable() {
            @Override
            public void run() {
                mDogsDao.deleteBreeds();
            }
        };

        mAppExecutors.diskIO().execute(deleteRunnable);
    }

    @Override
    public void refreshDogs() {
        // Not required because the {@link DogsRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }

    @Override
    public void deleteAllDogs() {
        Runnable deleteRunnable = new Runnable() {
            @Override
            public void run() {
                mDogsDao.deleteDogs();
            }
        };

        mAppExecutors.diskIO().execute(deleteRunnable);
    }

    @Override
    public void deleteDog(@NonNull final String taskId) {
        Runnable deleteRunnable = new Runnable() {
            @Override
            public void run() {
                mDogsDao.deleteDogById(taskId);
            }
        };

        mAppExecutors.diskIO().execute(deleteRunnable);
    }

    @VisibleForTesting
    static void clearInstance() {
        INSTANCE = null;
    }
}
