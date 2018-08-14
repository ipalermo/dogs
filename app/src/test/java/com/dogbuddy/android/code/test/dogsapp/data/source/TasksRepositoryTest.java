
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
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
public class TasksRepositoryTest {

    private final static String DOG_TITLE = "name";

    private final static String DOG_TITLE2 = "title2";

    private final static String DOG_TITLE3 = "title3";

    private static List<Dog> DOGS = Lists.newArrayList(new DogBuilder().setName("Title1").setBreed("Description1").createDog(),
            new DogBuilder().setName("Title2").setBreed("Description2").createDog());

    private DogsRepository mTasksRepository;

    @Mock
    private DogsDataSource mTasksRemoteDataSource;

    @Mock
    private DogsDataSource mTasksLocalDataSource;

    @Mock
    private DogsDataSource.GetDogCallback mGetDogCallback;

    @Mock
    private DogsDataSource.LoadDogsCallback mLoadDogsCallback;

    @Captor
    private ArgumentCaptor<DogsDataSource.LoadDogsCallback> mTasksCallbackCaptor;

    @Captor
    private ArgumentCaptor<DogsDataSource.GetDogCallback> mTaskCallbackCaptor;

    @Before
    public void setupTasksRepository() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mTasksRepository = DogsRepository.getInstance(
                mTasksRemoteDataSource, mTasksLocalDataSource);
    }

    @After
    public void destroyRepositoryInstance() {
        DogsRepository.destroyInstance();
    }

    @Test
    public void getTasks_repositoryCachesAfterFirstApiCall() {
        // Given a setup Captor to capture callbacks
        // When two calls are issued to the dogs repository
        twoTasksLoadCallsToRepository(mLoadDogsCallback);

        // Then dogs were only requested once from Service API
        verify(mTasksRemoteDataSource).getDogs(any(DogsDataSource.LoadDogsCallback.class));
    }

    @Test
    public void getTasks_requestsAllTasksFromLocalDataSource() {
        // When dogs are requested from the tasks repository
        mTasksRepository.getDogs(mLoadDogsCallback);

        // Then tasks are loaded from the local data source
        verify(mTasksLocalDataSource).getDogs(any(DogsDataSource.LoadDogsCallback.class));
    }

    @Test
    public void saveTask_savesTaskToServiceAPI() {
        // Given a stub dog with name and breed
        Dog newDog = new DogBuilder().setName(DOG_TITLE).setBreed("Some Dog Description").createDog();

        // When a dog is saved to the tasks repository
        mTasksRepository.saveDog(newDog);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTasksRemoteDataSource).saveDog(newDog);
        verify(mTasksLocalDataSource).saveDog(newDog);
        assertThat(mTasksRepository.mCachedDogs.size(), is(1));
    }

    @Test
    public void completeTask_completesTaskToServiceAPIUpdatesCache() {
        // Given a stub active dog with name and breed added in the repository
        Dog newDog = new DogBuilder().setName(DOG_TITLE).setBreed("Some Dog Description").createDog();
        mTasksRepository.saveDog(newDog);

        // When a dog is completed to the tasks repository
        mTasksRepository.completeTask(newDog);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTasksRemoteDataSource).completeTask(newDog);
        verify(mTasksLocalDataSource).completeTask(newDog);
        assertThat(mTasksRepository.mCachedDogs.size(), is(1));
        assertThat(mTasksRepository.mCachedDogs.get(newDog.getId()).isActive(), is(false));
    }

    @Test
    public void completeTaskId_completesTaskToServiceAPIUpdatesCache() {
        // Given a stub active dog with name and breed added in the repository
        Dog newDog = new DogBuilder().setName(DOG_TITLE).setBreed("Some Dog Description").createDog();
        mTasksRepository.saveDog(newDog);

        // When a dog is completed using its id to the tasks repository
        mTasksRepository.completeTask(newDog.getId());

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTasksRemoteDataSource).completeTask(newDog);
        verify(mTasksLocalDataSource).completeTask(newDog);
        assertThat(mTasksRepository.mCachedDogs.size(), is(1));
        assertThat(mTasksRepository.mCachedDogs.get(newDog.getId()).isActive(), is(false));
    }

    @Test
    public void activateTask_activatesTaskToServiceAPIUpdatesCache() {
        // Given a stub completed dog with name and breed in the repository
        Dog newDog = new DogBuilder().setName(DOG_TITLE).setBreed("Some Dog Description").setId(true).createDog();
        mTasksRepository.saveDog(newDog);

        // When a completed dog is activated to the tasks repository
        mTasksRepository.activateTask(newDog);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTasksRemoteDataSource).activateTask(newDog);
        verify(mTasksLocalDataSource).activateTask(newDog);
        assertThat(mTasksRepository.mCachedDogs.size(), is(1));
        assertThat(mTasksRepository.mCachedDogs.get(newDog.getId()).isActive(), is(true));
    }

    @Test
    public void activateTaskId_activatesTaskToServiceAPIUpdatesCache() {
        // Given a stub completed dog with name and breed in the repository
        Dog newDog = new DogBuilder().setName(DOG_TITLE).setBreed("Some Dog Description").setId(true).createDog();
        mTasksRepository.saveDog(newDog);

        // When a completed dog is activated with its id to the tasks repository
        mTasksRepository.activateTask(newDog.getId());

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTasksRemoteDataSource).activateTask(newDog);
        verify(mTasksLocalDataSource).activateTask(newDog);
        assertThat(mTasksRepository.mCachedDogs.size(), is(1));
        assertThat(mTasksRepository.mCachedDogs.get(newDog.getId()).isActive(), is(true));
    }

    @Test
    public void getTask_requestsSingleTaskFromLocalDataSource() {
        // When a dog is requested from the tasks repository
        mTasksRepository.getDog(DOG_TITLE, mGetDogCallback);

        // Then the dog is loaded from the database
        verify(mTasksLocalDataSource).getDog(eq(DOG_TITLE), any(
                DogsDataSource.GetDogCallback.class));
    }

    @Test
    public void deleteCompletedTasks_deleteCompletedTasksToServiceAPIUpdatesCache() {
        // Given 2 stub completed tasks and 1 stub active tasks in the repository
        Dog newDog = new DogBuilder().setName(DOG_TITLE).setBreed("Some Dog Description").setId(true).createDog();
        mTasksRepository.saveDog(newDog);
        Dog newDog2 = new DogBuilder().setName(DOG_TITLE2).setBreed("Some Dog Description").createDog();
        mTasksRepository.saveDog(newDog2);
        Dog newDog3 = new DogBuilder().setName(DOG_TITLE3).setBreed("Some Dog Description").setId(true).createDog();
        mTasksRepository.saveDog(newDog3);

        // When a completed tasks are cleared to the tasks repository
        mTasksRepository.clearCompletedTasks();


        // Then the service API and persistent repository are called and the cache is updated
        verify(mTasksRemoteDataSource).clearCompletedTasks();
        verify(mTasksLocalDataSource).clearCompletedTasks();

        assertThat(mTasksRepository.mCachedDogs.size(), is(1));
        assertTrue(mTasksRepository.mCachedDogs.get(newDog2.getId()).isActive());
        assertThat(mTasksRepository.mCachedDogs.get(newDog2.getId()).getName(), is(DOG_TITLE2));
    }

    @Test
    public void deleteAllTasks_deleteTasksToServiceAPIUpdatesCache() {
        // Given 2 stub completed tasks and 1 stub active tasks in the repository
        Dog newDog = new DogBuilder().setName(DOG_TITLE).setBreed("Some Dog Description").setId(true).createDog();
        mTasksRepository.saveDog(newDog);
        Dog newDog2 = new DogBuilder().setName(DOG_TITLE2).setBreed("Some Dog Description").createDog();
        mTasksRepository.saveDog(newDog2);
        Dog newDog3 = new DogBuilder().setName(DOG_TITLE3).setBreed("Some Dog Description").setId(true).createDog();
        mTasksRepository.saveDog(newDog3);

        // When all tasks are deleted to the tasks repository
        mTasksRepository.deleteAllDogs();

        // Verify the data sources were called
        verify(mTasksRemoteDataSource).deleteAllDogs();
        verify(mTasksLocalDataSource).deleteAllDogs();

        assertThat(mTasksRepository.mCachedDogs.size(), is(0));
    }

    @Test
    public void deleteTask_deleteTaskToServiceAPIRemovedFromCache() {
        // Given a dog in the repository
        Dog newDog = new DogBuilder().setName(DOG_TITLE).setBreed("Some Dog Description").setId(true).createDog();
        mTasksRepository.saveDog(newDog);
        assertThat(mTasksRepository.mCachedDogs.containsKey(newDog.getId()), is(true));

        // When deleted
        mTasksRepository.deleteDog(newDog.getId());

        // Verify the data sources were called
        verify(mTasksRemoteDataSource).deleteDog(newDog.getId());
        verify(mTasksLocalDataSource).deleteDog(newDog.getId());

        // Verify it's removed from repository
        assertThat(mTasksRepository.mCachedDogs.containsKey(newDog.getId()), is(false));
    }

    @Test
    public void getTasksWithDirtyCache_tasksAreRetrievedFromRemote() {
        // When calling getDogs in the repository with dirty cache
        mTasksRepository.refreshDogs();
        mTasksRepository.getDogs(mLoadDogsCallback);

        // And the remote data source has data available
        setTasksAvailable(mTasksRemoteDataSource, DOGS);

        // Verify the tasks from the remote data source are returned, not the local
        verify(mTasksLocalDataSource, never()).getDogs(mLoadDogsCallback);
        verify(mLoadDogsCallback).onDogsLoaded(DOGS);
    }

    @Test
    public void getTasksWithLocalDataSourceUnavailable_tasksAreRetrievedFromRemote() {
        // When calling getDogs in the repository
        mTasksRepository.getDogs(mLoadDogsCallback);

        // And the local data source has no data available
        setTasksNotAvailable(mTasksLocalDataSource);

        // And the remote data source has data available
        setTasksAvailable(mTasksRemoteDataSource, DOGS);

        // Verify the tasks from the local data source are returned
        verify(mLoadDogsCallback).onDogsLoaded(DOGS);
    }

    @Test
    public void getTasksWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // When calling getDogs in the repository
        mTasksRepository.getDogs(mLoadDogsCallback);

        // And the local data source has no data available
        setTasksNotAvailable(mTasksLocalDataSource);

        // And the remote data source has no data available
        setTasksNotAvailable(mTasksRemoteDataSource);

        // Verify no data is returned
        verify(mLoadDogsCallback).onDataNotAvailable();
    }

    @Test
    public void getTaskWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // Given a dog id
        final String taskId = "123";

        // When calling getDog in the repository
        mTasksRepository.getDog(taskId, mGetDogCallback);

        // And the local data source has no data available
        setTaskNotAvailable(mTasksLocalDataSource, taskId);

        // And the remote data source has no data available
        setTaskNotAvailable(mTasksRemoteDataSource, taskId);

        // Verify no data is returned
        verify(mGetDogCallback).onDataNotAvailable();
    }

    @Test
    public void getTasks_refreshesLocalDataSource() {
        // Mark cache as dirty to force a reload of data from remote data source.
        mTasksRepository.refreshDogs();

        // When calling getDogs in the repository
        mTasksRepository.getDogs(mLoadDogsCallback);

        // Make the remote data source return data
        setTasksAvailable(mTasksRemoteDataSource, DOGS);

        // Verify that the data fetched from the remote data source was saved in local.
        verify(mTasksLocalDataSource, times(DOGS.size())).saveDog(any(Dog.class));
    }

    /**
     * Convenience method that issues two calls to the tasks repository
     */
    private void twoTasksLoadCallsToRepository(DogsDataSource.LoadDogsCallback callback) {
        // When tasks are requested from repository
        mTasksRepository.getDogs(callback); // First call to API

        // Use the Mockito Captor to capture the callback
        verify(mTasksLocalDataSource).getDogs(mTasksCallbackCaptor.capture());

        // Local data source doesn't have data yet
        mTasksCallbackCaptor.getValue().onDataNotAvailable();


        // Verify the remote data source is queried
        verify(mTasksRemoteDataSource).getDogs(mTasksCallbackCaptor.capture());

        // Trigger callback so tasks are cached
        mTasksCallbackCaptor.getValue().onDogsLoaded(DOGS);

        mTasksRepository.getDogs(callback); // Second call to API
    }

    private void setTasksNotAvailable(DogsDataSource dataSource) {
        verify(dataSource).getDogs(mTasksCallbackCaptor.capture());
        mTasksCallbackCaptor.getValue().onDataNotAvailable();
    }

    private void setTasksAvailable(DogsDataSource dataSource, List<Dog> dogs) {
        verify(dataSource).getDogs(mTasksCallbackCaptor.capture());
        mTasksCallbackCaptor.getValue().onDogsLoaded(dogs);
    }

    private void setTaskNotAvailable(DogsDataSource dataSource, String taskId) {
        verify(dataSource).getDog(eq(taskId), mTaskCallbackCaptor.capture());
        mTaskCallbackCaptor.getValue().onDataNotAvailable();
    }

    private void setTaskAvailable(DogsDataSource dataSource, Dog dog) {
        verify(dataSource).getDog(eq(dog.getId()), mTaskCallbackCaptor.capture());
        mTaskCallbackCaptor.getValue().onDogLoaded(dog);
    }
}
