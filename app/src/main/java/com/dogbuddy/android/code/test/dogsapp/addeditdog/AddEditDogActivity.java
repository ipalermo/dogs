
package com.dogbuddy.android.code.test.dogsapp.addeditdog;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.dogbuddy.android.code.test.dogsapp.R;
import com.dogbuddy.android.code.test.dogsapp.ViewModelFactory;
import com.dogbuddy.android.code.test.dogsapp.util.ActivityUtils;

/**
 * Displays an add or edit dog screen.
 */
public class AddEditDogActivity extends AppCompatActivity implements AddEditDogNavigator {

    public static final int REQUEST_CODE = 1;

    public static final int ADD_EDIT_RESULT_OK = RESULT_FIRST_USER + 1;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onDogSaved() {
        setResult(ADD_EDIT_RESULT_OK);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adddog_act);

        // Set up the toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        AddEditDogFragment addEditDogFragment = obtainViewFragment();

        ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(),
                addEditDogFragment, R.id.contentFrame);

        subscribeToNavigationChanges();
    }

    private void subscribeToNavigationChanges() {
        AddEditDogViewModel viewModel = obtainViewModel(this);

        // The activity observes the navigation events in the ViewModel
        viewModel.getDogUpdatedEvent().observe(this, new Observer<Void>() {
            @Override
            public void onChanged(@Nullable Void v) {
                AddEditDogActivity.this.onDogSaved();
            }
        });
    }

    public static AddEditDogViewModel obtainViewModel(FragmentActivity activity) {
        // Use a Factory to inject dependencies into the ViewModel
        ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());

        return ViewModelProviders.of(activity, factory).get(AddEditDogViewModel.class);
    }

    @NonNull
    private AddEditDogFragment obtainViewFragment() {
        // View Fragment
        AddEditDogFragment addEditDogFragment = (AddEditDogFragment) getSupportFragmentManager()
                .findFragmentById(R.id.contentFrame);

        if (addEditDogFragment == null) {
            addEditDogFragment = AddEditDogFragment.newInstance();

            // Send the dog ID to the fragment
            Bundle bundle = new Bundle();
            bundle.putString(AddEditDogFragment.ARGUMENT_EDIT_TASK_ID,
                    getIntent().getStringExtra(AddEditDogFragment.ARGUMENT_EDIT_TASK_ID));
            addEditDogFragment.setArguments(bundle);
        }
        return addEditDogFragment;
    }
}
