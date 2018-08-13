
package com.dogbuddy.android.code.test.dogsapp.data;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.dogbuddy.android.code.test.dogsapp.data.source.DogsDataSource;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
public class FakeDogsRemoteDataSource implements DogsDataSource {

    private static FakeDogsRemoteDataSource INSTANCE;

    private static final Map<String, Dog> DOGS_SERVICE_DATA = new LinkedHashMap<>();
    private static final Map<String, Breed> BREEDS_SERVICE_DATA = new LinkedHashMap<>();

    // Prevent direct instantiation.
    private FakeDogsRemoteDataSource() {}

    public static FakeDogsRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FakeDogsRemoteDataSource();
        }
        return INSTANCE;
    }

    @Override
    public void getDogs(@NonNull LoadDogsCallback callback) {
        callback.onDogsLoaded(Lists.newArrayList(DOGS_SERVICE_DATA.values()));
    }

    @Override
    public void getDog(@NonNull String taskId, @NonNull GetDogCallback callback) {
        Dog dog = DOGS_SERVICE_DATA.get(taskId);
        callback.onDogLoaded(dog);
    }

    @Override
    public void getBreeds(@NonNull LoadBreedsCallback callback) {
        callback.onBreedsLoaded(Lists.newArrayList(BREEDS_SERVICE_DATA.values()));
    }

    @Override
    public void saveDog(@NonNull Dog dog) {
        DOGS_SERVICE_DATA.put(dog.getId(), dog);
    }

    @Override
    public void saveBreed(@NonNull Breed breed) {
        BREEDS_SERVICE_DATA.put(breed.getId(), breed);
    }

    public void refreshDogs() {
        // Not required because the {@link DogsRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }

    @Override
    public void deleteDog(@NonNull String taskId) {
        DOGS_SERVICE_DATA.remove(taskId);
    }

    @Override
    public void deleteAllDogs() {
        DOGS_SERVICE_DATA.clear();
    }

    @Override
    public void deleteAllBreeds() {
        BREEDS_SERVICE_DATA.clear();
    }

    @VisibleForTesting
    public void addDogs(Dog... dogs) {
        if (dogs != null) {
            for (Dog dog : dogs) {
                DOGS_SERVICE_DATA.put(dog.getId(), dog);
            }
        }
    }
}
