
package com.dogbuddy.android.code.test.dogsapp.data.source;

import com.dogbuddy.android.code.test.dogsapp.data.Dog;
import com.dogbuddy.android.code.test.dogsapp.data.DogBuilder;
import com.google.common.collect.Lists;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
public class DogRepositoryTest {

    private final static String DOG_NAME = "name";

    private final static String DOG_NAME2 = "name2";

    private final static String DOG_NAME3 = "name3";

    private static List<Dog> DOGS = Lists.newArrayList(new DogBuilder().setName("Name").setBreed("Breed1").createDog(),
            new DogBuilder().setName("Name2").setBreed("Breed2").createDog());

    private DogsRepository mDogsRepository;

    @Mock
    private DogsDataSource mDogsRemoteDataSource;

    @Mock
    private DogsDataSource mDogsLocalDataSource;

    @Mock
    private DogsDataSource.GetDogCallback mGetDogCallback;

    @Mock
    private DogsDataSource.LoadDogsCallback mLoadDogsCallback;

    @Captor
    private ArgumentCaptor<DogsDataSource.LoadDogsCallback> mDogsCallbackCaptor;

    @Captor
    private ArgumentCaptor<DogsDataSource.GetDogCallback> mDogCallbackCaptor;

    @Before
    public void setupDogsRepository() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mDogsRepository = DogsRepository.getInstance(
                mDogsRemoteDataSource, mDogsLocalDataSource);
    }

    @After
    public void destroyRepositoryInstance() {
        DogsRepository.destroyInstance();
    }

    @Test
    public void getDogs_repositoryCachesAfterFirstApiCall() {
        // Given a setup Captor to capture callbacks
        // When two calls are issued to the dogs repository
        twoDogsLoadCallsToRepository(mLoadDogsCallback);

        // Then dogs were only requested once from Service API
        verify(mDogsRemoteDataSource).getDogs(any(DogsDataSource.LoadDogsCallback.class));
    }

    @Test
    public void getDogs_requestsAllDogsFromLocalDataSource() {
        // When dogs are requested from the dogs repository
        mDogsRepository.getDogs(mLoadDogsCallback);

        // Then dogs are loaded from the local data source
        verify(mDogsLocalDataSource).getDogs(any(DogsDataSource.LoadDogsCallback.class));
    }

    @Test
    public void saveDog_savesDogToServiceAPI() {
        // Given a stub dog with name and breed
        Dog newDog = new DogBuilder().setName(DOG_NAME).setBreed("Some Dog Breed").createDog();

        // When a dog is saved to the dogs repository
        mDogsRepository.saveDog(newDog);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mDogsRemoteDataSource).saveDog(newDog);
        verify(mDogsLocalDataSource).saveDog(newDog);
        assertThat(mDogsRepository.mCachedDogs.size(), is(1));
    }

    @Test
    public void getDog_requestsSingleDogFromLocalDataSource() {
        // When a dog is requested from the dogs repository
        mDogsRepository.getDog(DOG_NAME, mGetDogCallback);

        // Then the dog is loaded from the database
        verify(mDogsLocalDataSource).getDog(eq(DOG_NAME), any(
                DogsDataSource.GetDogCallback.class));
    }
    
    @Test
    public void deleteAllDogs_deleteDogsToServiceAPIUpdatesCache() {
        // Given 2 stub completed dogs and 1 stub active dogs in the repository
        Dog newDog = new DogBuilder().setName(DOG_NAME).setBreed("Some Dog Breed").createDog();
        mDogsRepository.saveDog(newDog);
        Dog newDog2 = new DogBuilder().setName(DOG_NAME2).setBreed("Some Dog Breed").createDog();
        mDogsRepository.saveDog(newDog2);
        Dog newDog3 = new DogBuilder().setName(DOG_NAME3).setBreed("Some Dog Breed").createDog();
        mDogsRepository.saveDog(newDog3);

        // When all dogs are deleted to the dogs repository
        mDogsRepository.deleteAllDogs();

        // Verify the data sources were called
        verify(mDogsRemoteDataSource).deleteAllDogs();
        verify(mDogsLocalDataSource).deleteAllDogs();

        assertThat(mDogsRepository.mCachedDogs.size(), is(0));
    }

    @Test
    public void deleteDog_deleteDogToServiceAPIRemovedFromCache() {
        // Given a dog in the repository
        Dog newDog = new DogBuilder().setName(DOG_NAME).setBreed("Some Dog Breed").createDog();
        mDogsRepository.saveDog(newDog);
        assertThat(mDogsRepository.mCachedDogs.containsKey(newDog.getId()), is(true));

        // When deleted
        mDogsRepository.deleteDog(newDog.getId());

        // Verify the data sources were called
        verify(mDogsRemoteDataSource).deleteDog(newDog.getId());
        verify(mDogsLocalDataSource).deleteDog(newDog.getId());

        // Verify it's removed from repository
        assertThat(mDogsRepository.mCachedDogs.containsKey(newDog.getId()), is(false));
    }

    @Test
    public void getDogsWithDirtyCache_dogsAreRetrievedFromRemote() {
        // When calling getDogs in the repository with dirty cache
        mDogsRepository.refreshDogs();
        mDogsRepository.getDogs(mLoadDogsCallback);

        // And the remote data source has data available
        setDogsAvailable(mDogsRemoteDataSource, DOGS);

        // Verify the dogs from the remote data source are returned, not the local
        verify(mDogsLocalDataSource, never()).getDogs(mLoadDogsCallback);
        verify(mLoadDogsCallback).onDogsLoaded(DOGS);
    }

    @Test
    public void getDogsWithLocalDataSourceUnavailable_dogsAreRetrievedFromRemote() {
        // When calling getDogs in the repository
        mDogsRepository.getDogs(mLoadDogsCallback);

        // And the local data source has no data available
        setDogsNotAvailable(mDogsLocalDataSource);

        // And the remote data source has data available
        setDogsAvailable(mDogsRemoteDataSource, DOGS);

        // Verify the dogs from the local data source are returned
        verify(mLoadDogsCallback).onDogsLoaded(DOGS);
    }

    @Test
    public void getDogsWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // When calling getDogs in the repository
        mDogsRepository.getDogs(mLoadDogsCallback);

        // And the local data source has no data available
        setDogsNotAvailable(mDogsLocalDataSource);

        // And the remote data source has no data available
        setDogsNotAvailable(mDogsRemoteDataSource);

        // Verify no data is returned
        verify(mLoadDogsCallback).onDataNotAvailable();
    }

    @Test
    public void getDogWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // Given a dog id
        final String dogId = "123";

        // When calling getDog in the repository
        mDogsRepository.getDog(dogId, mGetDogCallback);

        // And the local data source has no data available
        setDogNotAvailable(mDogsLocalDataSource, dogId);

        // And the remote data source has no data available
        setDogNotAvailable(mDogsRemoteDataSource, dogId);

        // Verify no data is returned
        verify(mGetDogCallback).onDataNotAvailable();
    }

    @Test
    public void getDogs_refreshesLocalDataSource() {
        // Mark cache as dirty to force a reload of data from remote data source.
        mDogsRepository.refreshDogs();

        // When calling getDogs in the repository
        mDogsRepository.getDogs(mLoadDogsCallback);

        // Make the remote data source return data
        setDogsAvailable(mDogsRemoteDataSource, DOGS);

        // Verify that the data fetched from the remote data source was saved in local.
        verify(mDogsLocalDataSource, times(DOGS.size())).saveDog(any(Dog.class));
    }

    /**
     * Convenience method that issues two calls to the dogs repository
     */
    private void twoDogsLoadCallsToRepository(DogsDataSource.LoadDogsCallback callback) {
        // When dogs are requested from repository
        mDogsRepository.getDogs(callback); // First call to API

        // Use the Mockito Captor to capture the callback
        verify(mDogsLocalDataSource).getDogs(mDogsCallbackCaptor.capture());

        // Local data source doesn't have data yet
        mDogsCallbackCaptor.getValue().onDataNotAvailable();


        // Verify the remote data source is queried
        verify(mDogsRemoteDataSource).getDogs(mDogsCallbackCaptor.capture());

        // Trigger callback so dogs are cached
        mDogsCallbackCaptor.getValue().onDogsLoaded(DOGS);

        mDogsRepository.getDogs(callback); // Second call to API
    }

    private void setDogsNotAvailable(DogsDataSource dataSource) {
        verify(dataSource).getDogs(mDogsCallbackCaptor.capture());
        mDogsCallbackCaptor.getValue().onDataNotAvailable();
    }

    private void setDogsAvailable(DogsDataSource dataSource, List<Dog> dogs) {
        verify(dataSource).getDogs(mDogsCallbackCaptor.capture());
        mDogsCallbackCaptor.getValue().onDogsLoaded(dogs);
    }

    private void setDogNotAvailable(DogsDataSource dataSource, String dogId) {
        verify(dataSource).getDog(eq(dogId), mDogCallbackCaptor.capture());
        mDogCallbackCaptor.getValue().onDataNotAvailable();
    }

    private void setDogAvailable(DogsDataSource dataSource, Dog dog) {
        verify(dataSource).getDog(eq(dog.getId()), mDogCallbackCaptor.capture());
        mDogCallbackCaptor.getValue().onDogLoaded(dog);
    }
}
