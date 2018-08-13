
package com.dogbuddy.android.code.test.dogsapp.data.source.remote;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.dogbuddy.android.code.test.dogsapp.data.Breed;
import com.dogbuddy.android.code.test.dogsapp.data.Dog;
import com.dogbuddy.android.code.test.dogsapp.data.source.DogsDataSource;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of the data source that adds a latency simulating network.
 */
public class DogsRemoteDataSource implements DogsDataSource {

    private static DogsRemoteDataSource INSTANCE;

    private static final int SERVICE_LATENCY_IN_MILLIS = 2000;

    private final static Map<String, Dog> DOGS_SERVICE_DATA;
    private final static Map<String, Breed> BREEDS_SERVICE_DATA;

    static {
        DOGS_SERVICE_DATA = new LinkedHashMap<>(2);
        addDog("Coco", "Labrador Retriever", "0");
        addDog("Lola", "German Shepherd", "1");
        addDog("Pirate", "Golden Retriever", "2");
        addDog("Chester", "French Bulldog", "3");
        addDog("Benson", "Beagle", "4");
        addDog("Fiona", "Poodle", "5");
        addDog("William", "Rottweiler", "6");
        addDog("Kiera", "Yorkshire Terrier", "7");
        addDog("Daisy", "Pointer", "8");
        addDog("Lark", "Boxer", "12");
        addDog("Jane", "Siberian Huskie", "13");
        addDog("Mary", "Dachshunds", "14");
        addDog("William", "Great Danes", "15");
        addDog("Olivia", "Doberman", "16");
        addDog("Wallace", "Miniature Schnauzer", "17");
        addDog("Poppy", "Weimaraner", "18");
    }

    static {
        BREEDS_SERVICE_DATA = new LinkedHashMap<>(2);
        addBreed("Labrador Retriever", "0");
        addBreed("German Shepherd", "1");
        addBreed("Golden Retriever", "2");
        addBreed("French Bulldog", "3");
        addBreed("Beagle", "4");
        addBreed("Poodle", "5");
        addBreed("Rottweiler", "6");
        addBreed("Yorkshire Terrier", "7");
        addBreed("Pointer", "8");
        addBreed("Boxer", "12");
        addBreed("Siberian Huskie", "13");
        addBreed("Dachshunds", "14");
        addBreed("Great Danes", "15");
        addBreed("Doberman", "16");
        addBreed("Miniature Schnauzer", "17");
        addBreed("Weimaraner", "18");
    }

    public static DogsRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DogsRemoteDataSource();
        }
        return INSTANCE;
    }

    // Prevent direct instantiation.
    private DogsRemoteDataSource() {}

    private static void addDog(String name, String breed, String id) {
        Dog newDog = new Dog(name, breed, id);
        DOGS_SERVICE_DATA.put(newDog.getId(), newDog);
    }

    private static void addBreed(String breed, String id) {
        Breed newBreed = new Breed(breed, id);
        BREEDS_SERVICE_DATA.put(newBreed.getId(), newBreed);
    }

    /**
     * Note: {@link LoadDogsCallback#onDataNotAvailable()} is never fired. In a real remote data
     * source implementation, this would be fired if the server can't be contacted or the server
     * returns an error.
     */
    @Override
    public void getDogs(final @NonNull LoadDogsCallback callback) {
        // Simulate network by delaying the execution.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onDogsLoaded(Lists.newArrayList(DOGS_SERVICE_DATA.values()));
            }
        }, SERVICE_LATENCY_IN_MILLIS);
    }

    @Override
    public void getBreeds(final @NonNull LoadBreedsCallback callback) {
        // Simulate network by delaying the execution.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onBreedsLoaded(Lists.newArrayList(BREEDS_SERVICE_DATA.values()));
            }
        }, SERVICE_LATENCY_IN_MILLIS);
    }

    @Override
    public void saveBreed(@NonNull Breed breed) {
        BREEDS_SERVICE_DATA.put(breed.getId(), breed);
    }

    @Override
    public void deleteAllBreeds() {
        BREEDS_SERVICE_DATA.clear();
    }

    /**
     * Note: {@link GetDogCallback#onDataNotAvailable()} is never fired. In a real remote data
     * source implementation, this would be fired if the server can't be contacted or the server
     * returns an error.
     */
    @Override
    public void getDog(@NonNull String taskId, final @NonNull GetDogCallback callback) {
        final Dog dog = DOGS_SERVICE_DATA.get(taskId);

        // Simulate network by delaying the execution.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onDogLoaded(dog);
            }
        }, SERVICE_LATENCY_IN_MILLIS);
    }

    @Override
    public void saveDog(@NonNull Dog dog) {
        DOGS_SERVICE_DATA.put(dog.getId(), dog);
    }

    @Override
    public void refreshDogs() {
        // Not required because the {@link DogsRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }

    @Override
    public void deleteAllDogs() {
        DOGS_SERVICE_DATA.clear();
    }

    @Override
    public void deleteDog(@NonNull String dogId) {
        DOGS_SERVICE_DATA.remove(dogId);
    }
}
