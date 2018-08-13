
package com.dogbuddy.android.code.test.dogsapp.dogdetail;


import android.app.Application;
import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.content.res.Resources;

import com.dogbuddy.android.code.test.dogsapp.R;
import com.dogbuddy.android.code.test.dogsapp.SnackbarMessage;
import com.dogbuddy.android.code.test.dogsapp.data.Dog;
import com.dogbuddy.android.code.test.dogsapp.data.source.DogsDataSource;
import com.dogbuddy.android.code.test.dogsapp.data.source.DogsRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link DogDetailViewModel}
 */
public class DogDetailViewModelTest {

    // Executes each dog synchronously using Architecture Components.
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private static final String TITLE_TEST = "name";

    private static final String DESCRIPTION_TEST = "breed";

    private static final String NO_DATA_STRING = "NO_DATA_STRING";

    private static final String NO_DATA_DESC_STRING = "NO_DATA_DESC_STRING";

    @Mock
    private DogsRepository mTasksRepository;

    @Mock
    private Application mContext;

    @Mock
    private DogsDataSource.GetDogCallback mRepositoryCallback;

    @Mock
    private DogsDataSource.GetDogCallback mViewModelCallback;

    @Captor
    private ArgumentCaptor<DogsDataSource.GetDogCallback> mGetTaskCallbackCaptor;

    private DogDetailViewModel mTaskDetailViewModel;

    private Dog mDog;

    @Before
    public void setupTasksViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        setupContext();

        mDog = new Dog(TITLE_TEST, DESCRIPTION_TEST);

        // Get a reference to the class under test
        mTaskDetailViewModel = new DogDetailViewModel(mContext, mTasksRepository);
    }

    private void setupContext() {
        when(mContext.getApplicationContext()).thenReturn(mContext);
        when(mContext.getString(R.string.no_data)).thenReturn(NO_DATA_STRING);
        when(mContext.getString(R.string.no_data_description)).thenReturn(NO_DATA_DESC_STRING);
        when(mContext.getResources()).thenReturn(mock(Resources.class));
    }

    @Test
    public void getActiveTaskFromRepositoryAndLoadIntoView() {
        setupViewModelRepositoryCallback();

        // Then verify that the view was notified
        assertEquals(mTaskDetailViewModel.dog.get().getName(), mDog.getName());
        assertEquals(mTaskDetailViewModel.dog.get().getBreed(), mDog.getBreed());
    }

    @Test
    public void deleteTask() {
        setupViewModelRepositoryCallback();

        // When the deletion of a dog is requested
        mTaskDetailViewModel.deleteDog();

        // Then the repository is notified
        verify(mTasksRepository).deleteDog(mDog.getId());
    }

    @Test
    public void completeTask() {
        setupViewModelRepositoryCallback();

        // When the ViewModel is asked to complete the dog
        mTaskDetailViewModel.setCompleted(true);

        // Then a request is sent to the dog repository and the UI is updated
        verify(mTasksRepository).completeTask(mDog);
        assertThat(mTaskDetailViewModel.getSnackbarMessage().getValue(),
                is(R.string.task_marked_complete));
    }

    @Test
    public void activateTask() {
        setupViewModelRepositoryCallback();

        // When the ViewModel is asked to complete the dog
        mTaskDetailViewModel.setCompleted(false);

        // Then a request is sent to the dog repository and the UI is updated
        verify(mTasksRepository).activateTask(mDog);
        assertThat(mTaskDetailViewModel.getSnackbarMessage().getValue(),
                is(R.string.task_marked_active));
    }

    @Test
    public void TaskDetailViewModel_repositoryError() {
        // Given an initialized ViewModel with an active dog
        mViewModelCallback = mock(DogsDataSource.GetDogCallback.class);

        mTaskDetailViewModel.start(mDog.getId());

        // Use a captor to get a reference for the callback.
        verify(mTasksRepository).getDog(eq(mDog.getId()), mGetTaskCallbackCaptor.capture());

        // When the repository returns an error
        mGetTaskCallbackCaptor.getValue().onDataNotAvailable(); // Trigger callback error

        // Then verify that data is not available
        assertFalse(mTaskDetailViewModel.isDataAvailable());
    }

    @Test
    public void TaskDetailViewModel_repositoryNull() {
        setupViewModelRepositoryCallback();

        // When the repository returns a null dog
        mGetTaskCallbackCaptor.getValue().onDogLoaded(null); // Trigger callback error

        // Then verify that data is not available
        assertFalse(mTaskDetailViewModel.isDataAvailable());

        // Then dog detail UI is shown
        assertThat(mTaskDetailViewModel.dog.get(), is(nullValue()));
    }

    private void setupViewModelRepositoryCallback() {
        // Given an initialized ViewModel with an active dog
        mViewModelCallback = mock(DogsDataSource.GetDogCallback.class);

        mTaskDetailViewModel.start(mDog.getId());

        // Use a captor to get a reference for the callback.
        verify(mTasksRepository).getDog(eq(mDog.getId()), mGetTaskCallbackCaptor.capture());

        mGetTaskCallbackCaptor.getValue().onDogLoaded(mDog); // Trigger callback
    }

    @Test
    public void updateSnackbar_nullValue() {
        // Before setting the Snackbar text, get its current value
        SnackbarMessage snackbarText = mTaskDetailViewModel.getSnackbarMessage();

        // Check that the value is null
        assertThat("Snackbar text does not match", snackbarText.getValue(), is(nullValue()));
    }
}
