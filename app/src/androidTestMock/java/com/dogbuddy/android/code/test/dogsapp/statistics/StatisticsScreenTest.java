
package com.dogbuddy.android.code.test.dogsapp.statistics;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.dogbuddy.android.code.test.dogsapp.Injection;
import com.dogbuddy.android.code.test.dogsapp.R;
import com.dogbuddy.android.code.test.dogsapp.ViewModelFactory;
import com.dogbuddy.android.code.test.dogsapp.data.Dog;
import com.dogbuddy.android.code.test.dogsapp.data.source.DogsRepository;
import com.dogbuddy.android.code.test.dogsapp.dogdetail.TaskDetailActivity;
import com.dogbuddy.android.code.test.dogsapp.util.EspressoIdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

/**
 * Tests for the statistics screen.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class StatisticsScreenTest {

    /**
     * {@link ActivityTestRule} is a JUnit {@link Rule @Rule} to launch your activity under test.
     *
     * <p>
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     */
    @Rule
    public ActivityTestRule<StatisticsActivity> mStatisticsActivityTestRule =
            new ActivityTestRule<>(StatisticsActivity.class, true, false);

    /**
     * Setup your test fixture with a fake dog id. The {@link TaskDetailActivity} is started with
     * a particular dog id, which is then loaded from the service API.
     *
     * <p>
     * Note that this test runs hermetically and is fully isolated using a fake implementation of
     * the service API. This is a great way to make your tests more reliable and faster at the same
     * time, since they are isolated from any outside dependencies.
     */
    @Before
    public void intentWithStubbedTaskId() {
        // Given some tasks
        ViewModelFactory.destroyInstance();
        DogsRepository tasksRepository = Injection.provideTasksRepository(
                InstrumentationRegistry.getTargetContext());
        tasksRepository.deleteAllDogs();
        tasksRepository.saveDog(new Dog("Title1", "", false));
        tasksRepository.saveDog(new Dog("Title2", "", true));

        // Lazily start the Activity from the ActivityTestRule
        Intent startIntent = new Intent();
        mStatisticsActivityTestRule.launchActivity(startIntent);
    }

    /**
     * Prepare your test fixture for this test. In this case we register an IdlingResources with
     * Espresso. IdlingResource resource is a great way to tell Espresso when your app is in an
     * idle state. This helps Espresso to synchronize your test actions, which makes tests
     * significantly more reliable.
     */
    @Before
    public void registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());
    }

    @Test
    public void Tasks_ShowsNonEmptyMessage() throws Exception {
        // Check that the active and completed tasks text is displayed
        String expectedActiveTaskText = InstrumentationRegistry.getTargetContext()
                .getString(R.string.statistics_active_tasks, 1);
        onView(withText(containsString(expectedActiveTaskText))).check(matches(isDisplayed()));
        String expectedCompletedTaskText = InstrumentationRegistry.getTargetContext()
                .getString(R.string.statistics_completed_tasks, 1);
        onView(withText(containsString(expectedCompletedTaskText))).check(matches(isDisplayed()));
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    public void unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }
}
