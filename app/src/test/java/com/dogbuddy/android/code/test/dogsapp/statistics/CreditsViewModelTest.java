
package com.dogbuddy.android.code.test.dogsapp.statistics;


import android.app.Application;
import android.arch.core.executor.testing.InstantTaskExecutorRule;

import com.dogbuddy.android.code.test.dogsapp.data.Dog;
import com.dogbuddy.android.code.test.dogsapp.data.source.DogsDataSource;
import com.dogbuddy.android.code.test.dogsapp.data.source.DogsRepository;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link CreditsViewModel}
 */
public class CreditsViewModelTest {

    // Executes each dog synchronously using Architecture Components.
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private static List<Dog> TASKS;

    @Mock
    private DogsRepository mTasksRepository;

    @Captor
    private ArgumentCaptor<DogsDataSource.LoadDogsCallback> mLoadTasksCallbackCaptor;

    private CreditsViewModel mCreditsViewModel;

    @Before
    public void setupStatisticsViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mCreditsViewModel = new CreditsViewModel(mock(Application.class));

        // We initialise the tasks to 3, with one active and two completed
        TASKS = Lists.newArrayList(new Dog("Title1", "Description1"),
                new Dog("Title2", "Description2", true), new Dog("Title3", "Description3", true));
    }

    @Test
    public void loadEmptyTasksFromRepository_EmptyResults() {
        // Given an initialized CreditsViewModel with no tasks
        TASKS.clear();

        // When loading of Tasks is requested
        mCreditsViewModel.loadStatistics();

        // Callback is captured and invoked with stubbed tasks
        verify(mTasksRepository).getDogs(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onDogsLoaded(TASKS);

        // Then the results are empty
        assertThat(mCreditsViewModel.empty.get(), is(true));
    }

    @Test
    public void loadNonEmptyTasksFromRepository_NonEmptyResults() {
        // When loading of Tasks is requested
        mCreditsViewModel.loadStatistics();

        // Callback is captured and invoked with stubbed tasks
        verify(mTasksRepository).getDogs(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onDogsLoaded(TASKS);

        // Then the results are empty
        assertThat(mCreditsViewModel.empty.get(), is(false));
    }


    @Test
    public void loadStatisticsWhenTasksAreUnavailable_CallErrorToDisplay() {
        // When statistics are loaded
        mCreditsViewModel.loadStatistics();

        // And tasks data isn't available
        verify(mTasksRepository).getDogs(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onDataNotAvailable();

        // Then an error message is shown
        assertEquals(mCreditsViewModel.empty.get(), true);
        assertEquals(mCreditsViewModel.error.get(), true);
    }
}
