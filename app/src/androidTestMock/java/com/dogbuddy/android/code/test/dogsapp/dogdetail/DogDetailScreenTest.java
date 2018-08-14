
package com.dogbuddy.android.code.test.dogsapp.dogdetail;

import android.app.Activity;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.dogbuddy.android.code.test.dogsapp.Injection;
import com.dogbuddy.android.code.test.dogsapp.R;
import com.dogbuddy.android.code.test.dogsapp.TestUtils;
import com.dogbuddy.android.code.test.dogsapp.data.Dog;
import com.dogbuddy.android.code.test.dogsapp.data.FakeDogsRemoteDataSource;
import com.dogbuddy.android.code.test.dogsapp.data.source.DogsRepository;
import com.dogbuddy.android.code.test.dogsapp.util.EspressoIdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;

/**
 * Tests for the dogs screen, the main screen which contains a list of all dogs.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DogDetailScreenTest {

    private static String DOG_TITLE = "ATSL";

    private static String DOG_DESCRIPTION = "Rocks";

    /**
     * {@link Dog} stub that is added to the fake service API layer.
     */
    private static Dog ACTIVE_DOG = new Dog(DOG_TITLE, DOG_DESCRIPTION, false);

    /**
     * {@link Dog} stub that is added to the fake service API layer.
     */
    private static Dog COMPLETED_DOG = new Dog(DOG_TITLE, DOG_DESCRIPTION, true);

    /**
     * {@link ActivityTestRule} is a JUnit {@link Rule @Rule} to launch your activity under test.
     *
     * <p>
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     *
     * <p>
     * Sometimes an {@link Activity} requires a custom start {@link Intent} to receive data
     * from the source Activity. ActivityTestRule has a feature which let's you lazily start the
     * Activity under test, so you can control the Intent that is used to start the target
     * Activity.
     */
    @Rule
    public ActivityTestRule<TaskDetailActivity> mTaskDetailActivityTestRule =
            new ActivityTestRule<>(TaskDetailActivity.class, true /* Initial touch mode  */,
                    false /* Lazily launch activity */);

    private void loadActiveTask() {
        startActivityWithWithStubbedTask(ACTIVE_DOG);
    }

    private void loadCompletedTask() {
        startActivityWithWithStubbedTask(COMPLETED_DOG);
    }

    /**
     * Setup your test fixture with a fake dog id. The {@link TaskDetailActivity} is started with
     * a particular dog id, which is then loaded from the service API.
     *
     * <p>
     * Note that this test runs hermetically and is fully isolated using a fake implementation of
     * the service API. This is a great way to make your tests more reliable and faster at the same
     * time, since they are isolated from any outside dependencies.
     */
    private void startActivityWithWithStubbedTask(Dog dog) {
        // Add a dog stub to the fake service api layer.
        DogsRepository dogsRepository = Injection.provideTasksRepository(InstrumentationRegistry.getTargetContext());
        dogsRepository.deleteAllDogs();
        FakeDogsRemoteDataSource.getInstance().addDogs(dog);

        // Lazily start the Activity from the ActivityTestRule this time to inject the start Intent
        Intent startIntent = new Intent();
        startIntent.putExtra(TaskDetailActivity.EXTRA_DOG_ID, dog.getId());
        mTaskDetailActivityTestRule.launchActivity(startIntent);
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
    public void activeTaskDetails_DisplayedInUi() throws Exception {
        loadActiveTask();

        // Check that the dog name and breed are displayed
        onView(withId(R.id.dog_detail_title)).check(matches(withText(DOG_TITLE)));
        onView(withId(R.id.dog_detail_description)).check(matches(withText(DOG_DESCRIPTION)));
        onView(withId(R.id.dog_detail_complete)).check(matches(not(isChecked())));
    }

    @Test
    public void completedTaskDetails_DisplayedInUi() throws Exception {
        loadCompletedTask();

        // Check that the dog name and breed are displayed
        onView(withId(R.id.dog_detail_title)).check(matches(withText(DOG_TITLE)));
        onView(withId(R.id.dog_detail_description)).check(matches(withText(DOG_DESCRIPTION)));
        onView(withId(R.id.dog_detail_complete)).check(matches(isChecked()));
    }

    @Test
    public void orientationChange_menuAndTaskPersist() {
        loadActiveTask();

        // Check delete menu item is displayed and is unique
        onView(withId(R.id.menu_delete)).check(matches(isDisplayed()));

        TestUtils.rotateOrientation(mTaskDetailActivityTestRule.getActivity());

        // Check that the dog is shown
        onView(withId(R.id.dog_detail_title)).check(matches(withText(DOG_TITLE)));
        onView(withId(R.id.dog_detail_description)).check(matches(withText(DOG_DESCRIPTION)));

        // Check delete menu item is displayed and is unique
        onView(withId(R.id.menu_delete)).check(matches(isDisplayed()));
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    public void unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }
}
