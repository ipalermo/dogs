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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dogbuddy.android.code.test.dogsapp.R;
import com.dogbuddy.android.code.test.dogsapp.SnackbarMessage;
import com.dogbuddy.android.code.test.dogsapp.databinding.DogdetailFragBinding;
import com.dogbuddy.android.code.test.dogsapp.util.SnackbarUtils;


/**
 * Main UI for the dog detail screen.
 */
public class DogDetailFragment extends Fragment {

    public static final String ARGUMENT_DOG_ID = "DOG_ID";

    public static final int REQUEST_EDIT_DOG = 1;

    private DogDetailViewModel mViewModel;

    public static DogDetailFragment newInstance(String dogId) {
        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENT_DOG_ID, dogId);
        DogDetailFragment fragment = new DogDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupFab();

        setupSnackbar();
    }

    private void setupSnackbar() {
        mViewModel.getSnackbarMessage().observe(this, new SnackbarMessage.SnackbarObserver() {
            @Override
            public void onNewMessage(@StringRes int snackbarMessageResourceId) {
                SnackbarUtils.showSnackbar(getView(), getString(snackbarMessageResourceId));

            }
        });
    }

    private void setupFab() {
        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_edit_dog);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.editDog();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.start(getArguments().getString(ARGUMENT_DOG_ID));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dogdetail_frag, container, false);

        DogdetailFragBinding viewDataBinding = DogdetailFragBinding.bind(view);

        mViewModel = DogDetailActivity.obtainViewModel(getActivity());

        viewDataBinding.setViewmodel(mViewModel);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                mViewModel.deleteDog();
                return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.dogdetail_fragment_menu, menu);
    }
}
