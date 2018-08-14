
package com.dogbuddy.android.code.test.dogsapp.credits;


import android.app.Application;
import android.arch.core.executor.testing.InstantTaskExecutorRule;

import com.dogbuddy.android.code.test.dogsapp.data.Dog;
import com.dogbuddy.android.code.test.dogsapp.data.source.DogsDataSource;
import com.dogbuddy.android.code.test.dogsapp.data.source.DogsRepository;

import org.junit.Before;
import org.junit.Rule;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * Unit tests for the implementation of {@link CreditsViewModel}
 */
public class CreditsViewModelTest {

    // Executes each dog synchronously using Architecture Components.
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private static List<Dog> DOGS;

    @Mock
    private DogsRepository mDogsRepository;

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
    }

}
