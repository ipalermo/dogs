
package com.dogbuddy.android.code.test.dogsapp.data.source.local;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.dogbuddy.android.code.test.dogsapp.data.Dog;
import com.dogbuddy.android.code.test.dogsapp.data.source.DogsDataSource;
import com.dogbuddy.android.code.test.dogsapp.util.SingleExecutors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Integration test for the {@link DogsDataSource}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TasksLocalDataSourceTest {

    private final static String TITLE = "name";

    private final static String TITLE2 = "title2";

    private final static String TITLE3 = "title3";

    private DogsLocalDataSource mLocalDataSource;

    private DogBuddyDatabase mDatabase;

    @Before
    public void setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                DogBuddyDatabase.class)
                .build();
        DogsDao dogsDao = mDatabase.dogDao();

        // Make sure that we're not keeping a reference to the wrong instance.
        DogsLocalDataSource.clearInstance();
        mLocalDataSource = DogsLocalDataSource.getInstance(new SingleExecutors(), dogsDao);
    }

    @After
    public void cleanUp() {
        mDatabase.close();
        DogsLocalDataSource.clearInstance();
    }

    @Test
    public void testPreConditions() {
        assertNotNull(mLocalDataSource);
    }

    @Test
    public void saveTask_retrievesTask() {
        // Given a new dog
        final Dog newDog = new Dog(TITLE, "");

        // When saved into the persistent repository
        mLocalDataSource.saveDog(newDog);

        // Then the dog can be retrieved from the persistent repository
        mLocalDataSource.getDog(newDog.getId(), new DogsDataSource.GetDogCallback() {
            @Override
            public void onDogLoaded(Dog dog) {
                assertThat(dog, is(newDog));
            }

            @Override
            public void onDataNotAvailable() {
                fail("Callback error");
            }
        });
    }

    @Test
    public void completeTask_retrievedTaskIsComplete() {
        // Initialize mock for the callback.
        DogsDataSource.GetDogCallback callback = mock(DogsDataSource.GetDogCallback.class);
        // Given a new dog in the persistent repository
        final Dog newDog = new Dog(TITLE, "");
        mLocalDataSource.saveDog(newDog);

        // When completed in the persistent repository
        mLocalDataSource.completeTask(newDog);

        // Then the dog can be retrieved from the persistent repository and is complete
        mLocalDataSource.getDog(newDog.getId(), new DogsDataSource.GetDogCallback() {
            @Override
            public void onDogLoaded(Dog dog) {
                assertThat(dog, is(newDog));
                assertThat(dog.isCompleted(), is(true));
            }

            @Override
            public void onDataNotAvailable() {
                fail("Callback error");
            }
        });
    }

    @Test
    public void activateTask_retrievedTaskIsActive() {
        // Initialize mock for the callback.
        DogsDataSource.GetDogCallback callback = mock(DogsDataSource.GetDogCallback.class);

        // Given a new completed dog in the persistent repository
        final Dog newDog = new Dog(TITLE, "");
        mLocalDataSource.saveDog(newDog);
        mLocalDataSource.completeTask(newDog);

        // When activated in the persistent repository
        mLocalDataSource.activateTask(newDog);

        // Then the dog can be retrieved from the persistent repository and is active
        mLocalDataSource.getDog(newDog.getId(), callback);

        verify(callback, never()).onDataNotAvailable();
        verify(callback).onDogLoaded(newDog);

        assertThat(newDog.isCompleted(), is(false));
    }

    @Test
    public void clearCompletedTask_taskNotRetrievable() {
        // Initialize mocks for the callbacks.
        DogsDataSource.GetDogCallback callback1 = mock(DogsDataSource.GetDogCallback.class);
        DogsDataSource.GetDogCallback callback2 = mock(DogsDataSource.GetDogCallback.class);
        DogsDataSource.GetDogCallback callback3 = mock(DogsDataSource.GetDogCallback.class);

        // Given 2 new completed tasks and 1 active dog in the persistent repository
        final Dog newDog1 = new Dog(TITLE, "");
        mLocalDataSource.saveDog(newDog1);
        mLocalDataSource.completeTask(newDog1);
        final Dog newDog2 = new Dog(TITLE2, "");
        mLocalDataSource.saveDog(newDog2);
        mLocalDataSource.completeTask(newDog2);
        final Dog newDog3 = new Dog(TITLE3, "");
        mLocalDataSource.saveDog(newDog3);

        // When completed tasks are cleared in the repository
        mLocalDataSource.clearCompletedTasks();

        // Then the completed tasks cannot be retrieved and the active one can
        mLocalDataSource.getDog(newDog1.getId(), callback1);

        verify(callback1).onDataNotAvailable();
        verify(callback1, never()).onDogLoaded(newDog1);

        mLocalDataSource.getDog(newDog2.getId(), callback2);

        verify(callback2).onDataNotAvailable();
        verify(callback2, never()).onDogLoaded(newDog2);

        mLocalDataSource.getDog(newDog3.getId(), callback3);

        verify(callback3, never()).onDataNotAvailable();
        verify(callback3).onDogLoaded(newDog3);
    }

    @Test
    public void deleteAllTasks_emptyListOfRetrievedTask() {
        // Given a new dog in the persistent repository and a mocked callback
        Dog newDog = new Dog(TITLE, "");
        mLocalDataSource.saveDog(newDog);
        DogsDataSource.LoadDogsCallback callback = mock(DogsDataSource.LoadDogsCallback.class);

        // When all tasks are deleted
        mLocalDataSource.deleteAllDogs();

        // Then the retrieved tasks is an empty list
        mLocalDataSource.getDogs(callback);

        verify(callback).onDataNotAvailable();
        verify(callback, never()).onDogsLoaded(anyList());
    }

    @Test
    public void getTasks_retrieveSavedTasks() {
        // Given 2 new tasks in the persistent repository
        final Dog newDog1 = new Dog(TITLE, "");
        mLocalDataSource.saveDog(newDog1);
        final Dog newDog2 = new Dog(TITLE, "");
        mLocalDataSource.saveDog(newDog2);

        // Then the tasks can be retrieved from the persistent repository
        mLocalDataSource.getDogs(new DogsDataSource.LoadDogsCallback() {
            @Override
            public void onDogsLoaded(List<Dog> dogs) {
                assertNotNull(dogs);
                assertTrue(dogs.size() >= 2);

                boolean newTask1IdFound = false;
                boolean newTask2IdFound = false;
                for (Dog dog : dogs) {
                    if (dog.getId().equals(newDog1.getId())) {
                        newTask1IdFound = true;
                    }
                    if (dog.getId().equals(newDog2.getId())) {
                        newTask2IdFound = true;
                    }
                }
                assertTrue(newTask1IdFound);
                assertTrue(newTask2IdFound);
            }

            @Override
            public void onDataNotAvailable() {
                fail();
            }
        });
    }
}
