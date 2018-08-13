
package com.dogbuddy.android.code.test.dogsapp.dogs;

import android.app.Application;
import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.Observer;
import android.content.res.Resources;

import com.dogbuddy.android.code.test.dogsapp.R;
import com.dogbuddy.android.code.test.dogsapp.TestUtils;
import com.dogbuddy.android.code.test.dogsapp.addeditdog.AddEditDogActivity;
import com.dogbuddy.android.code.test.dogsapp.data.Dog;
import com.dogbuddy.android.code.test.dogsapp.data.source.DogsDataSource.LoadDogsCallback;
import com.dogbuddy.android.code.test.dogsapp.data.source.DogsRepository;
import com.dogbuddy.android.code.test.dogsapp.dogdetail.DogDetailActivity;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static com.dogbuddy.android.code.test.dogsapp.R.string.successfully_deleted_dog_message;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link DogsViewModel}
 */
public class DogsViewModelTest {

    // Executes each dog synchronously using Architecture Components.
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private static List<Dog> TASKS;

    @Mock
    private DogsRepository mTasksRepository;

    @Mock
    private Application mContext;

    @Captor
    private ArgumentCaptor<LoadDogsCallback> mLoadTasksCallbackCaptor;

    private DogsViewModel mDogsViewModel;

    @Before
    public void setupTasksViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        setupContext();

        // Get a reference to the class under test
        mDogsViewModel = new DogsViewModel(mContext, mTasksRepository);

        // We initialise the tasks to 3, with one active and two completed
        TASKS = Lists.newArrayList(new Dog("Title1", "Description1"),
                new Dog("Title2", "Description2", true), new Dog("Title3", "Description3", true));

        mDogsViewModel.getSnackbarMessage().removeObservers(TestUtils.TEST_OBSERVER);

    }

    private void setupContext() {
        when(mContext.getApplicationContext()).thenReturn(mContext);
        when(mContext.getString(R.string.successfully_saved_dog_message))
                .thenReturn("EDIT_RESULT_OK");
        when(mContext.getString(R.string.successfully_added_dog_message))
                .thenReturn("ADD_EDIT_RESULT_OK");
        when(mContext.getString(successfully_deleted_dog_message))
                .thenReturn("DELETE_RESULT_OK");

        when(mContext.getResources()).thenReturn(mock(Resources.class));
    }

    @Test
    public void loadAllTasksFromRepository_dataLoaded() {
        // Given an initialized DogsViewModel with initialized tasks
        // When loading of Tasks is requested
        mDogsViewModel.setFiltering(TasksFilterType.ALL_TASKS);
        mDogsViewModel.loadDogs(true);

        // Callback is captured and invoked with stubbed tasks
        verify(mTasksRepository).getDogs(mLoadTasksCallbackCaptor.capture());


        // Then progress indicator is shown
        assertTrue(mDogsViewModel.dataLoading.get());
        mLoadTasksCallbackCaptor.getValue().onDogsLoaded(TASKS);

        // Then progress indicator is hidden
        assertFalse(mDogsViewModel.dataLoading.get());

        // And data loaded
        assertFalse(mDogsViewModel.items.isEmpty());
        assertTrue(mDogsViewModel.items.size() == 3);
    }

    @Test
    public void loadActiveTasksFromRepositoryAndLoadIntoView() {
        // Given an initialized DogsViewModel with initialized tasks
        // When loading of Tasks is requested
        mDogsViewModel.setFiltering(TasksFilterType.ACTIVE_TASKS);
        mDogsViewModel.loadDogs(true);

        // Callback is captured and invoked with stubbed tasks
        verify(mTasksRepository).getDogs(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onDogsLoaded(TASKS);

        // Then progress indicator is hidden
        assertFalse(mDogsViewModel.dataLoading.get());

        // And data loaded
        assertFalse(mDogsViewModel.items.isEmpty());
        assertTrue(mDogsViewModel.items.size() == 1);
    }

    @Test
    public void loadCompletedTasksFromRepositoryAndLoadIntoView() {
        // Given an initialized DogsViewModel with initialized tasks
        // When loading of Tasks is requested
        mDogsViewModel.setFiltering(TasksFilterType.COMPLETED_TASKS);
        mDogsViewModel.loadDogs(true);

        // Callback is captured and invoked with stubbed tasks
        verify(mTasksRepository).getDogs(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onDogsLoaded(TASKS);

        // Then progress indicator is hidden
        assertFalse(mDogsViewModel.dataLoading.get());

        // And data loaded
        assertFalse(mDogsViewModel.items.isEmpty());
        assertTrue(mDogsViewModel.items.size() == 2);
    }

    @Test
    public void clickOnFab_ShowsAddTaskUi() {

        Observer<Void> observer = mock(Observer.class);

        mDogsViewModel.getNewDogEvent().observe(TestUtils.TEST_OBSERVER, observer);

        // When adding a new dog
        mDogsViewModel.addNewDog();

        // Then the event is triggered
        verify(observer).onChanged(null);
    }

    @Test
    public void clearCompletedTasks_ClearsTasks() {
        // When completed tasks are cleared
        mDogsViewModel.clearCompletedTasks();

        // Then repository is called and the view is notified
        verify(mTasksRepository).clearCompletedTasks();
        verify(mTasksRepository).getDogs(any(LoadDogsCallback.class));
    }

    @Test
    public void handleActivityResult_editOK() {
        // When DogDetailActivity sends a EDIT_RESULT_OK
        Observer<Integer> observer = mock(Observer.class);

        mDogsViewModel.getSnackbarMessage().observe(TestUtils.TEST_OBSERVER, observer);

        mDogsViewModel.handleActivityResult(
                AddEditDogActivity.REQUEST_CODE, DogDetailActivity.EDIT_RESULT_OK);

        // Then the snackbar shows the correct message
        verify(observer).onChanged(R.string.successfully_saved_dog_message);
    }

    @Test
    public void handleActivityResult_addEditOK() {
        // When DogDetailActivity sends a EDIT_RESULT_OK
        Observer<Integer> observer = mock(Observer.class);

        mDogsViewModel.getSnackbarMessage().observe(TestUtils.TEST_OBSERVER, observer);

        // When AddEditDogActivity sends a ADD_EDIT_RESULT_OK
        mDogsViewModel.handleActivityResult(
                AddEditDogActivity.REQUEST_CODE, AddEditDogActivity.ADD_EDIT_RESULT_OK);

        // Then the snackbar shows the correct message
        verify(observer).onChanged(R.string.successfully_added_dog_message);
    }

    @Test
    public void handleActivityResult_deleteOk() {
        // When DogDetailActivity sends a EDIT_RESULT_OK
        Observer<Integer> observer = mock(Observer.class);

        mDogsViewModel.getSnackbarMessage().observe(TestUtils.TEST_OBSERVER, observer);

        // When AddEditDogActivity sends a ADD_EDIT_RESULT_OK
        mDogsViewModel.handleActivityResult(
                AddEditDogActivity.REQUEST_CODE, DogDetailActivity.DELETE_RESULT_OK);

        // Then the snackbar shows the correct message
        verify(observer).onChanged(R.string.successfully_deleted_dog_message);
    }

    @Test
    public void getTasksAddViewVisible() {
        // When the filter type is ALL_TASKS
        mDogsViewModel.setFiltering(TasksFilterType.ALL_TASKS);

        // Then the "Add dog" action is visible
        assertThat(mDogsViewModel.dogsAddViewVisible.get(), is(true));
    }
}
