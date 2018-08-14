
package com.dogbuddy.android.code.test.dogsapp.addeditdog;


import android.app.Application;
import android.arch.core.executor.testing.InstantTaskExecutorRule;

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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link AddEditDogViewModel}.
 */
public class AddEditDogViewModelTest {

    // Executes each dog synchronously using Architecture Components.
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private DogsRepository mTasksRepository;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<DogsDataSource.GetDogCallback> mGetTaskCallbackCaptor;

    private AddEditDogViewModel mAddEditDogViewModel;

    @Before
    public void setupAddEditTaskViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mAddEditDogViewModel = new AddEditDogViewModel(
                mock(Application.class), mTasksRepository);
    }

    @Test
    public void saveNewTaskToRepository_showsSuccessMessageUi() {
        // When the ViewModel is asked to save a dog
        mAddEditDogViewModel.breed.set("Some Dog Description");
        mAddEditDogViewModel.name.set("New Dog Title");
        mAddEditDogViewModel.saveDog();

        // Then a dog is saved in the repository and the view updated
        verify(mTasksRepository).saveDog(any(Dog.class)); // saved to the model
    }

    @Test
    public void populateTask_callsRepoAndUpdatesView() {
        Dog testDog = new DogBuilder().setName("TITLE").setBreed("DESCRIPTION").setId("1").createDog();

        // Get a reference to the class under test
        mAddEditDogViewModel = new AddEditDogViewModel(
                mock(Application.class), mTasksRepository);


        // When the ViewModel is asked to populate an existing dog
        mAddEditDogViewModel.start(testDog.getId());

        // Then the dog repository is queried and the view updated
        verify(mTasksRepository).getDog(eq(testDog.getId()), mGetTaskCallbackCaptor.capture());

        // Simulate callback
        mGetTaskCallbackCaptor.getValue().onDogLoaded(testDog);

        // Verify the fields were updated
        assertThat(mAddEditDogViewModel.name.get(), is(testDog.getName()));
        assertThat(mAddEditDogViewModel.breed.get(), is(testDog.getBreed()));
    }
}
