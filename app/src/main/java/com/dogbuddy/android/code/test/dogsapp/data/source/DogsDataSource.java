
package com.dogbuddy.android.code.test.dogsapp.data.source;

import android.support.annotation.NonNull;

import com.dogbuddy.android.code.test.dogsapp.data.Breed;
import com.dogbuddy.android.code.test.dogsapp.data.Dog;

import java.util.List;

/**
 * Main entry point for accessing tasks data.
 */
public interface DogsDataSource {

    interface LoadDogsCallback {

        void onDogsLoaded(List<Dog> dogs);

        void onDataNotAvailable();
    }

    interface GetDogCallback {

        void onDogLoaded(Dog dog);

        void onDataNotAvailable();
    }

    interface LoadBreedsCallback {

        void onBreedsLoaded(List<Breed> breeds);

        void onDataNotAvailable();
    }

    void getDogs(@NonNull LoadDogsCallback callback);

    void getDog(@NonNull String taskId, @NonNull GetDogCallback callback);
    
    void getBreeds(@NonNull LoadBreedsCallback callback);
    
    void saveDog(@NonNull Dog dog);

    void saveBreed(@NonNull Breed breed);

    void refreshDogs();

    void deleteAllDogs();

    void deleteAllBreeds();

    void deleteDog(@NonNull String taskId);
}
