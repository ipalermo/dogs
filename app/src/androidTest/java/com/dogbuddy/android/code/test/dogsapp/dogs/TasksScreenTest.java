
package com.dogbuddy.android.code.test.dogsapp.dogs;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.filters.SdkSuppress;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import com.dogbuddy.android.code.test.dogsapp.Injection;
import com.dogbuddy.android.code.test.dogsapp.R;
import com.dogbuddy.android.code.test.dogsapp.TestUtils;
import com.dogbuddy.android.code.test.dogsapp.ViewModelFactory;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.dogbuddy.android.code.test.dogsapp.TestUtils.getCurrentActivity;
import static com.google.common.base.Preconditions.checkArgument;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.IsNot.not;

/**
 * Tests for the dogs screen, the main screen which contains a list of all dogs.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TasksScreenTest {

    private final static String TITLE1 = "TITLE1";

    private final static String DESCRIPTION = "DESCR";

    private final static String TITLE2 = "TITLE2";

    /**
     * {@link ActivityTestRule} is a JUnit {@link Rule @Rule} to launch your activity under test.
     * <p>
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     */
    @Rule
    public ActivityTestRule<DogsActivity> mTasksActivityTestRule =
            new ActivityTestRule<>(DogsActivity.class);

    @Before
    public void resetState() {
        ViewModelFactory.destroyInstance();
        Injection.provideTasksRepository(InstrumentationRegistry.getTargetContext())
                .deleteAllDogs();
    }

    /**
     * A custom {@link Matcher} which matches an item in a {@link ListView} by its text.
     * <p>
     * View constraints:
     * <ul>
     * <li>View must be a child of a {@link ListView}
     * <ul>
     *
     * @param itemText the text to match
     * @return Matcher that matches text in the given view
     */
    private Matcher<View> withItemText(final String itemText) {
        checkArgument(!TextUtils.isEmpty(itemText), "itemText cannot be null or empty");
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View item) {
                return allOf(
                        isDescendantOfA(isAssignableFrom(ListView.class)),
                        withText(itemText)).matches(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is isDescendantOfA LV with text " + itemText);
            }
        };
    }

    @Test
    public void clickAddTaskButton_opensAddTaskUi() {
        // Click on the add dog button
        onView(withId(R.id.fab_add_dog)).perform(click());

        // Check if the add dog screen is displayed
        onView(withId(R.id.add_dog_name)).check(matches(isDisplayed()));
    }

    @Test
    public void editTask() throws Exception {
        // First add a dog
        createTask(TITLE1, DESCRIPTION);

        // Click on the dog on the list
        onView(withText(TITLE1)).perform(click());

        // Click on the edit dog button
        onView(withId(R.id.fab_edit_dog)).perform(click());

        String editTaskTitle = TITLE2;
        String editTaskDescription = "New Description";

        // Edit dog name and breed
        onView(withId(R.id.add_dog_name))
                .perform(replaceText(editTaskTitle), closeSoftKeyboard()); // Type new dog name
        onView(withId(R.id.add_dog_description)).perform(replaceText(editTaskDescription),
                closeSoftKeyboard()); // Type new dog breed and close the keyboard

        // Save the dog
        onView(withId(R.id.fab_edit_dog_done)).perform(click());

        // Verify dog is displayed on screen in the dog list.
        onView(withItemText(editTaskTitle)).check(matches(isDisplayed()));

        // Verify previous dog is not displayed
        onView(withItemText(TITLE1)).check(doesNotExist());
    }

    @Test
    public void addTaskToTasksList() throws Exception {
        createTask(TITLE1, DESCRIPTION);

        // Verify dog is displayed on screen
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
    }

    @Test
    public void markTaskAsComplete() {
        viewAllTasks();

        // Add active dog
        createTask(TITLE1, DESCRIPTION);

        // Mark the dog as complete
        clickCheckBoxForTask(TITLE1);

        // Verify dog is shown as complete
        viewAllTasks();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        viewActiveTasks();
        onView(withItemText(TITLE1)).check(matches(not(isDisplayed())));
        viewCompletedTasks();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
    }

    @Test
    public void markTaskAsActive() {
        viewAllTasks();

        // Add completed dog
        createTask(TITLE1, DESCRIPTION);
        clickCheckBoxForTask(TITLE1);

        // Mark the dog as active
        clickCheckBoxForTask(TITLE1);

        // Verify dog is shown as active
        viewAllTasks();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        viewActiveTasks();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        viewCompletedTasks();
        onView(withItemText(TITLE1)).check(matches(not(isDisplayed())));
    }

    @Test
    public void showAllTasks() {
        // Add 2 active dogs
        createTask(TITLE1, DESCRIPTION);
        createTask(TITLE2, DESCRIPTION);

        //Verify that all our dogs are shown
        viewAllTasks();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        onView(withItemText(TITLE2)).check(matches(isDisplayed()));
    }

    @Test
    public void showActiveTasks() {
        // Add 2 active dogs
        createTask(TITLE1, DESCRIPTION);
        createTask(TITLE2, DESCRIPTION);

        //Verify that all our dogs are shown
        viewActiveTasks();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        onView(withItemText(TITLE2)).check(matches(isDisplayed()));
    }

    @Test
    public void showCompletedTasks() {
        // Add 2 completed dogs
        createTask(TITLE1, DESCRIPTION);
        clickCheckBoxForTask(TITLE1);
        createTask(TITLE2, DESCRIPTION);
        clickCheckBoxForTask(TITLE2);

        // Verify that all our dogs are shown
        viewCompletedTasks();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        onView(withItemText(TITLE2)).check(matches(isDisplayed()));
    }

    @Test
    public void clearCompletedTasks() {
        viewAllTasks();

        // Add 2 complete dogs
        createTask(TITLE1, DESCRIPTION);
        clickCheckBoxForTask(TITLE1);
        createTask(TITLE2, DESCRIPTION);
        clickCheckBoxForTask(TITLE2);

        // Click clear completed in menu
        openActionBarOverflowOrOptionsMenu(getTargetContext());
        onView(withText(R.string.menu_clear)).perform(click());

        //Verify that completed dogs are not shown
        onView(withItemText(TITLE1)).check(matches(not(isDisplayed())));
        onView(withItemText(TITLE2)).check(matches(not(isDisplayed())));
    }

    @Test
    public void createOneTask_deleteTask() {
        viewAllTasks();

        // Add active dog
        createTask(TITLE1, DESCRIPTION);

        // Open it in details view
        onView(withText(TITLE1)).perform(click());

        // Click delete dog in menu
        onView(withId(R.id.menu_delete)).perform(click());

        // Verify it was deleted
        viewAllTasks();
        onView(withText(TITLE1)).check(matches(not(isDisplayed())));
    }

    @Test
    public void createTwoTasks_deleteOneTask() {
        // Add 2 active dogs
        createTask(TITLE1, DESCRIPTION);
        createTask(TITLE2, DESCRIPTION);

        // Open the second dog in details view
        onView(withText(TITLE2)).perform(click());

        // Click delete dog in menu
        onView(withId(R.id.menu_delete)).perform(click());

        // Verify only one dog was deleted
        viewAllTasks();
        onView(withText(TITLE1)).check(matches(isDisplayed()));
        onView(withText(TITLE2)).check(doesNotExist());
    }

    @Test
    public void markTaskAsCompleteOnDetailScreen_dogIsCompleteInList() {
        viewAllTasks();

        // Add 1 active dog
        createTask(TITLE1, DESCRIPTION);

        // Click on the dog on the list
        onView(withText(TITLE1)).perform(click());

        // Click on the checkbox in dog details screen
        onView(withId(R.id.dog_detail_complete)).perform(click());

        // Click on the navigation up button to go back to the list
        onView(withContentDescription(getToolbarNavigationContentDescription())).perform(click());

        // Check that the dog is marked as completed
        onView(allOf(withId(R.id.complete),
                hasSibling(withText(TITLE1)))).check(matches(isChecked()));
    }

    @Test
    public void markTaskAsActiveOnDetailScreen_dogIsActiveInList() {
        viewAllTasks();

        // Add 1 completed dog
        createTask(TITLE1, DESCRIPTION);
        clickCheckBoxForTask(TITLE1);

        // Click on the dog on the list
        onView(withText(TITLE1)).perform(click());

        // Click on the checkbox in dog details screen
        onView(withId(R.id.dog_detail_complete)).perform(click());

        // Click on the navigation up button to go back to the list
        onView(withContentDescription(getToolbarNavigationContentDescription())).perform(click());

        // Check that the dog is marked as active
        onView(allOf(withId(R.id.complete),
                hasSibling(withText(TITLE1)))).check(matches(not(isChecked())));
    }

    @Test
    public void markTaskAsAcompleteAndActiveOnDetailScreen_dogIsActiveInList() {
        viewAllTasks();

        // Add 1 active dog
        createTask(TITLE1, DESCRIPTION);

        // Click on the dog on the list
        onView(withText(TITLE1)).perform(click());

        // Click on the checkbox in dog details screen
        onView(withId(R.id.dog_detail_complete)).perform(click());

        // Click again to restore it to original state
        onView(withId(R.id.dog_detail_complete)).perform(click());

        // Click on the navigation up button to go back to the list
        onView(withContentDescription(getToolbarNavigationContentDescription())).perform(click());

        // Check that the dog is marked as active
        onView(allOf(withId(R.id.complete),
                hasSibling(withText(TITLE1)))).check(matches(not(isChecked())));
    }

    @Test
    public void markTaskAsActiveAndCompleteOnDetailScreen_dogIsCompleteInList() {
        viewAllTasks();

        // Add 1 completed dog
        createTask(TITLE1, DESCRIPTION);
        clickCheckBoxForTask(TITLE1);

        // Click on the dog on the list
        onView(withText(TITLE1)).perform(click());

        // Click on the checkbox in dog details screen
        onView(withId(R.id.dog_detail_complete)).perform(click());

        // Click again to restore it to original state
        onView(withId(R.id.dog_detail_complete)).perform(click());

        // Click on the navigation up button to go back to the list
        onView(withContentDescription(getToolbarNavigationContentDescription())).perform(click());

        // Check that the dog is marked as active
        onView(allOf(withId(R.id.complete),
                hasSibling(withText(TITLE1)))).check(matches(isChecked()));
    }

    @Test
    public void orientationChange_FilterActivePersists() {

        // Add a completed dog
        createTask(TITLE1, DESCRIPTION);
        clickCheckBoxForTask(TITLE1);

        // when switching to active dogs
        viewActiveTasks();

        // then no dogs should appear
        onView(withText(TITLE1)).check(matches(not(isDisplayed())));

        // when rotating the screen
        TestUtils.rotateOrientation(mTasksActivityTestRule.getActivity());

        // then nothing changes
        onView(withText(TITLE1)).check(doesNotExist());
    }

    @Test
    public void orientationChange_FilterCompletedPersists() {

        // Add a completed dog
        createTask(TITLE1, DESCRIPTION);
        clickCheckBoxForTask(TITLE1);

        // when switching to completed dogs
        viewCompletedTasks();

        // the completed dog should be displayed
        onView(withText(TITLE1)).check(matches(isDisplayed()));

        // when rotating the screen
        TestUtils.rotateOrientation(mTasksActivityTestRule.getActivity());

        // then nothing changes
        onView(withText(TITLE1)).check(matches(isDisplayed()));
        onView(withText(R.string.label_completed)).check(matches(isDisplayed()));
    }

    @Test
    @SdkSuppress(minSdkVersion = 21) // Blinking cursor after rotation breaks this in API 19
    public void orientationChange_DuringEdit_ChangePersists() throws Throwable {
        // Add a completed dog
        createTask(TITLE1, DESCRIPTION);

        // Open the dog in details view
        onView(withText(TITLE1)).perform(click());

        // Click on the edit dog button
        onView(withId(R.id.fab_edit_dog)).perform(click());

        // Change dog name (but don't save)
        onView(withId(R.id.add_dog_name))
                .perform(replaceText(TITLE2), closeSoftKeyboard()); // Type new dog name

        // Rotate the screen
        TestUtils.rotateOrientation(getCurrentActivity());

        // Verify dog name is restored
        onView(withId(R.id.add_dog_name)).check(matches(withText(TITLE2)));
    }

    @Test
    @SdkSuppress(minSdkVersion = 21) // Blinking cursor after rotation breaks this in API 19
    public void orientationChange_DuringEdit_NoDuplicate() throws IllegalStateException {
        // Add a completed dog
        createTask(TITLE1, DESCRIPTION);

        // Open the dog in details view
        onView(withText(TITLE1)).perform(click());

        // Click on the edit dog button
        onView(withId(R.id.fab_edit_dog)).perform(click());

        // Rotate the screen
        TestUtils.rotateOrientation(getCurrentActivity());

        // Edit dog name and breed
        onView(withId(R.id.add_dog_name))
                .perform(replaceText(TITLE2), closeSoftKeyboard()); // Type new dog name
        onView(withId(R.id.add_dog_description)).perform(replaceText(DESCRIPTION),
                closeSoftKeyboard()); // Type new dog breed and close the keyboard

        // Save the dog
        onView(withId(R.id.fab_edit_dog_done)).perform(click());

        // Verify dog is displayed on screen in the dog list.
        onView(withItemText(TITLE2)).check(matches(isDisplayed()));

        // Verify previous dog is not displayed
        onView(withItemText(TITLE1)).check(doesNotExist());
    }

    @Test
    public void noTasks_AllTasksFilter_AddTaskViewVisible() {
        // Given an empty list of dogs, make sure "All dogs" filter is on
        viewAllTasks();

        // Add dog View should be displayed
        onView(withId(R.id.noTasksAdd)).check(matches(isDisplayed()));
    }

    @Test
    public void noTasks_CompletedTasksFilter_AddTaskViewNotVisible() {
        // Given an empty list of dogs, make sure "All dogs" filter is on
        viewCompletedTasks();

        // Add dog View should be displayed
        onView(withId(R.id.noTasksAdd)).check(matches(not(isDisplayed())));
    }

    @Test
    public void noTasks_ActiveTasksFilter_AddTaskViewNotVisible() {
        // Given an empty list of dogs, make sure "All dogs" filter is on
        viewActiveTasks();

        // Add dog View should be displayed
        onView(withId(R.id.noTasksAdd)).check(matches(not(isDisplayed())));
    }

    private void viewAllTasks() {
        onView(withId(R.id.menu_add_dog)).perform(click());
        onView(withText(R.string.nav_all)).perform(click());
    }

    private void viewActiveTasks() {
        onView(withId(R.id.menu_add_dog)).perform(click());
        onView(withText(R.string.nav_active)).perform(click());
    }

    private void viewCompletedTasks() {
        onView(withId(R.id.menu_add_dog)).perform(click());
        onView(withText(R.string.nav_completed)).perform(click());
    }

    private void createTask(String title, String description) {
        // Click on the add dog button
        onView(withId(R.id.fab_add_dog)).perform(click());

        // Add dog name and breed
        onView(withId(R.id.add_dog_name)).perform(typeText(title),
                closeSoftKeyboard()); // Type new dog name
        onView(withId(R.id.add_dog_description)).perform(typeText(description),
                closeSoftKeyboard()); // Type new dog breed and close the keyboard

        // Save the dog
        onView(withId(R.id.fab_edit_dog_done)).perform(click());
    }

    private void clickCheckBoxForTask(String title) {
        onView(allOf(withId(R.id.complete), hasSibling(withText(title)))).perform(click());
    }

    private String getText(int stringId) {
        return mTasksActivityTestRule.getActivity().getResources().getString(stringId);
    }

    private String getToolbarNavigationContentDescription() {
        return TestUtils.getToolbarNavigationContentDescription(
                mTasksActivityTestRule.getActivity(), R.id.toolbar);
    }
}
