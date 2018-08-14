
package com.dogbuddy.android.code.test.dogsapp.dogdetail;


import android.app.Application;
import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.content.res.Resources;

import com.dogbuddy.android.code.test.dogsapp.R;
import com.dogbuddy.android.code.test.dogsapp.SnackbarMessage;
import com.dogbuddy.android.code.test.dogsapp.data.Dog;
import com.dogbuddy.android.code.test.dogsapp.data.DogBuilder;
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

    private static final String NAME_TEST = "name";

    private static final String BREED_TEST = "breed";

    private static final String NO_DATA_STRING = "NO_DATA_STRING";

    private static final String NO_DATA_DESC_STRING = "NO_DATA_DESC_STRING";

    @Mock
    private DogsRepository mDogsRepository;

    @Mock
    private Application mContext;

    @Mock
    private DogsDataSource.GetDogCallback mRepositoryCallback;

    @Mock
    private DogsDataSource.GetDogCallback mViewModelCallback;

    @Captor
    private ArgumentCaptor<DogsDataSource.GetDogCallback> mGetDogCallbackCaptor;

    private DogDetailViewModel mDogDetailViewModel;

    private Dog mDog;

    @Before
    public void setupDogsViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        setupContext();

        mDog = new DogBuilder().setName(NAME_TEST).setBreed(BREED_TEST).createDog();

        // Get a reference to the class under test
        mDogDetailViewModel = new DogDetailViewModel(mContext, mDogsRepository);
    }

    private void setupContext() {
        when(mContext.getApplicationContext()).thenReturn(mContext);
        when(mContext.getString(R.string.no_data)).thenReturn(NO_DATA_STRING);
        when(mContext.getString(R.string.no_data_description)).thenReturn(NO_DATA_DESC_STRING);
        when(mContext.getResources()).thenReturn(mock(Resources.class));
    }

    @Test
    public void getActiveDogFromRepositoryAndLoadIntoView() {
        setupViewModelRepositoryCallback();

        // Then verify that the view was notified
        assertEquals(mDogDetailViewModel.dog.get().getName(), mDog.getName());
        assertEquals(mDogDetailViewModel.dog.get().getBreed(), mDog.getBreed());
    }

    @Test
    public void deleteDog() {
        setupViewModelRepositoryCallback();

        // When the deletion of a dog is requested
        mDogDetailViewModel.deleteDog();

        // Then the repository is notified
        verify(mDogsRepository).deleteDog(mDog.getId());
    }

    @Test
    public void DogDetailViewModel_repositoryError() {
        // Given an initialized ViewModel with an active dog
        mViewModelCallback = mock(DogsDataSource.GetDogCallback.class);

        mDogDetailViewModel.start(mDog.getId());

        // Use a captor to get a reference for the callback.
        verify(mDogsRepository).getDog(eq(mDog.getId()), mGetDogCallbackCaptor.capture());

        // When the repository returns an error
        mGetDogCallbackCaptor.getValue().onDataNotAvailable(); // Trigger callback error

        // Then verify that data is not available
        assertFalse(mDogDetailViewModel.isDataAvailable());
    }

    @Test
    public void DogDetailViewModel_repositoryNull() {
        setupViewModelRepositoryCallback();

        // When the repository returns a null dog
        mGetDogCallbackCaptor.getValue().onDogLoaded(null); // Trigger callback error

        // Then verify that data is not available
        assertFalse(mDogDetailViewModel.isDataAvailable());

        // Then dog detail UI is shown
        assertThat(mDogDetailViewModel.dog.get(), is(nullValue()));
    }

    private void setupViewModelRepositoryCallback() {
        // Given an initialized ViewModel with an active dog
        mViewModelCallback = mock(DogsDataSource.GetDogCallback.class);

        mDogDetailViewModel.start(mDog.getId());

        // Use a captor to get a reference for the callback.
        verify(mDogsRepository).getDog(eq(mDog.getId()), mGetDogCallbackCaptor.capture());

        mGetDogCallbackCaptor.getValue().onDogLoaded(mDog); // Trigger callback
    }

    @Test
    public void updateSnackbar_nullValue() {
        // Before setting the Snackbar text, get its current value
        SnackbarMessage snackbarText = mDogDetailViewModel.getSnackbarMessage();

        // Check that the value is null
        assertThat("Snackbar text does not match", snackbarText.getValue(), is(nullValue()));
    }
}
