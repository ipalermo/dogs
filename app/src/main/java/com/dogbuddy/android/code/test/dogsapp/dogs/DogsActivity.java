
package com.dogbuddy.android.code.test.dogsapp.dogs;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.dogbuddy.android.code.test.dogsapp.R;
import com.dogbuddy.android.code.test.dogsapp.ViewModelFactory;
import com.dogbuddy.android.code.test.dogsapp.addeditdog.AddEditDogActivity;
import com.dogbuddy.android.code.test.dogsapp.credits.CreditsActivity;
import com.dogbuddy.android.code.test.dogsapp.dogdetail.DogDetailActivity;
import com.dogbuddy.android.code.test.dogsapp.util.ActivityUtils;


public class DogsActivity extends AppCompatActivity implements DogItemNavigator, ItemsNavigator {

    private DrawerLayout mDrawerLayout;

    private DogsViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dogs_act);

        setupToolbar();

        setupNavigationDrawer();

        setupViewFragment();

        mViewModel = obtainViewModel(this);

        // Subscribe to "open dog" event
        mViewModel.getOpenDogEvent().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String taskId) {
                if (taskId != null) {
                    openTaskDetails(taskId);
                }
            }
        });

        // Subscribe to "new dog" event
        mViewModel.getNewDogEvent().observe(this, new Observer<Void>() {
            @Override
            public void onChanged(@Nullable Void v) {
                addNewTask();
            }
        });
    }

    public static DogsViewModel obtainViewModel(FragmentActivity activity) {
        // Use a Factory to inject dependencies into the ViewModel
        ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());

        DogsViewModel viewModel =
                ViewModelProviders.of(activity, factory).get(DogsViewModel.class);

        return viewModel;
    }

    private void setupViewFragment() {
        DogsFragment dogsFragment =
                (DogsFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (dogsFragment == null) {
            // Create the fragment
            dogsFragment = DogsFragment.newInstance();
            ActivityUtils.replaceFragmentInActivity(
                    getSupportFragmentManager(), dogsFragment, R.id.contentFrame);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void setupNavigationDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.primary_dark);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Open the navigation drawer when the home icon is selected from the toolbar.
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.list_navigation_menu_item:
                                // Do nothing, we're already on that screen
                                break;
                            case R.id.statistics_navigation_menu_item:
                                Intent intent =
                                        new Intent(DogsActivity.this, CreditsActivity.class);
                                startActivity(intent);
                                break;
                            default:
                                break;
                        }
                        // Close the navigation drawer when an item is selected.
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mViewModel.handleActivityResult(requestCode, resultCode);
    }

    @Override
    public void openTaskDetails(String taskId) {
        Intent intent = new Intent(this, DogDetailActivity.class);
        intent.putExtra(DogDetailActivity.EXTRA_DOG_ID, taskId);
        startActivityForResult(intent, AddEditDogActivity.REQUEST_CODE);

    }

    @Override
    public void addNewTask() {
        Intent intent = new Intent(this, AddEditDogActivity.class);
        startActivityForResult(intent, AddEditDogActivity.REQUEST_CODE);
    }
}
