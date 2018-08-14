/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dogbuddy.android.code.test.dogsapp.dogdetail;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.dogbuddy.android.code.test.dogsapp.R;
import com.dogbuddy.android.code.test.dogsapp.ViewModelFactory;
import com.dogbuddy.android.code.test.dogsapp.addeditdog.AddEditDogActivity;
import com.dogbuddy.android.code.test.dogsapp.addeditdog.AddEditDogFragment;
import com.dogbuddy.android.code.test.dogsapp.util.ActivityUtils;

import static com.dogbuddy.android.code.test.dogsapp.addeditdog.AddEditDogActivity.ADD_EDIT_RESULT_OK;
import static com.dogbuddy.android.code.test.dogsapp.dogdetail.DogDetailFragment.REQUEST_EDIT_DOG;

/**
 * Displays dog details screen.
 */
public class DogDetailActivity extends AppCompatActivity implements DogDetailNavigator {

    public static final String EXTRA_DOG_ID = "DOG_ID";

    public static final int DELETE_RESULT_OK = RESULT_FIRST_USER + 2;

    public static final int EDIT_RESULT_OK = RESULT_FIRST_USER + 3;

    private DogDetailViewModel mDogViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dogdetail_act);

        setupToolbar();

        DogDetailFragment dogDetailFragment = findOrCreateViewFragment();

        ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(),
                dogDetailFragment, R.id.contentFrame);

        mDogViewModel = obtainViewModel(this);

        subscribeToNavigationChanges(mDogViewModel);
    }

    @NonNull
    private DogDetailFragment findOrCreateViewFragment() {
        // Get the requested dog id
        String dogId = getIntent().getStringExtra(EXTRA_DOG_ID);

        DogDetailFragment dogDetailFragment = (DogDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.contentFrame);

        if (dogDetailFragment == null) {
            dogDetailFragment = DogDetailFragment.newInstance(dogId);
        }
        return dogDetailFragment;
    }

    @NonNull
    public static DogDetailViewModel obtainViewModel(FragmentActivity activity) {
        // Use a Factory to inject dependencies into the ViewModel
        ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());

        return ViewModelProviders.of(activity, factory).get(DogDetailViewModel.class);
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setTitle("");
    }

    private void subscribeToNavigationChanges(DogDetailViewModel viewModel) {
        // The activity observes the navigation commands in the ViewModel
        viewModel.getEditDogCommand().observe(this, new Observer<Void>() {
            @Override
            public void onChanged(@Nullable Void v) {
                DogDetailActivity.this.onStartEditDog();
            }
        });
        viewModel.getDeleteDogCommand().observe(this, new Observer<Void>() {
            @Override
            public void onChanged(@Nullable Void v) {
                DogDetailActivity.this.onDogDeleted();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EDIT_DOG) {
            // If the dog was edited successfully, go back to the list.
            if (resultCode == ADD_EDIT_RESULT_OK) {
                // If the result comes from the add/edit screen, it's an edit.
                setResult(EDIT_RESULT_OK);
                finish();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onDogDeleted() {
        setResult(DELETE_RESULT_OK);
        // If the dog was deleted successfully, go back to the list.
        finish();
    }

    @Override
    public void onStartEditDog() {
        String dogId = getIntent().getStringExtra(EXTRA_DOG_ID);
        Intent intent = new Intent(this, AddEditDogActivity.class);
        intent.putExtra(AddEditDogFragment.ARGUMENT_EDIT_DOG_ID, dogId);
        startActivityForResult(intent, REQUEST_EDIT_DOG);
    }

}
