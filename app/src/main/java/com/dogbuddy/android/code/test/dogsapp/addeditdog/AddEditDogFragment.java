
package com.dogbuddy.android.code.test.dogsapp.addeditdog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dogbuddy.android.code.test.dogsapp.R;
import com.dogbuddy.android.code.test.dogsapp.SnackbarMessage;
import com.dogbuddy.android.code.test.dogsapp.data.Breed;
import com.dogbuddy.android.code.test.dogsapp.databinding.AddDogFragBinding;
import com.dogbuddy.android.code.test.dogsapp.dogs.BreedsAdapter;
import com.dogbuddy.android.code.test.dogsapp.util.SnackbarUtils;

import java.util.ArrayList;

/**
 * Main UI for the add dog screen. Users can enter a dog name and breed.
 */
public class AddEditDogFragment extends Fragment {

    public static final String ARGUMENT_EDIT_DOG_ID = "EDIT_DOG_ID";

    private AddEditDogViewModel mViewModel;

    private BreedsAdapter mBreedsAdapter;

    private AddDogFragBinding mViewDataBinding;

    public static AddEditDogFragment newInstance() {
        return new AddEditDogFragment();
    }

    public AddEditDogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupSnackbar();

        setupActionBar();

        setupSpinners();

        loadData();
    }

    private void loadData() {
        // Add or edit an existing dog?
        if (getArguments() != null) {
            mViewModel.start(getArguments().getString(ARGUMENT_EDIT_DOG_ID));
        } else {
            mViewModel.start(null);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.add_dog_frag, container, false);
        if (mViewDataBinding == null) {
            mViewDataBinding = AddDogFragBinding.bind(root);
        }

        mViewModel = AddEditDogActivity.obtainViewModel(getActivity());

        mViewDataBinding.setViewmodel(mViewModel);

        setHasOptionsMenu(true);
        setRetainInstance(false);

        return mViewDataBinding.getRoot();
    }

    private void setupSnackbar() {
        mViewModel.getSnackbarMessage().observe(this, new SnackbarMessage.SnackbarObserver() {
            @Override
            public void onNewMessage(@StringRes int snackbarMessageResourceId) {
                SnackbarUtils.showSnackbar(getView(), getString(snackbarMessageResourceId));
            }
        });
    }

    private void setupActionBar() {
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        if (getArguments() != null && getArguments().get(ARGUMENT_EDIT_DOG_ID) != null) {
            actionBar.setTitle(R.string.edit_dog);
        } else {
            actionBar.setTitle(R.string.add_dog);
        }
    }

    private void setupSpinners() {
        setupBreedsSpinnerAdapter();
        setupGenderSpinnerAdapter();
    }

    private void setupBreedsSpinnerAdapter() {
        AppCompatSpinner breedsSpinner =  mViewDataBinding.breed;

        mBreedsAdapter = new BreedsAdapter(
                new ArrayList<Breed>(0),
                mViewModel
        );
        breedsSpinner.setAdapter(mBreedsAdapter);
    }

    private void setupGenderSpinnerAdapter() {
        AppCompatSpinner genderSpinner =  mViewDataBinding.gender;
        genderSpinner.setAdapter(mViewModel.genderAdapter.get());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_done:
                mViewModel.saveDog();
                return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_edit_dog_fragment_menu, menu);
    }
}
