
package com.dogbuddy.android.code.test.dogsapp.dogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.dogbuddy.android.code.test.dogsapp.R;
import com.dogbuddy.android.code.test.dogsapp.ScrollChildSwipeRefreshLayout;
import com.dogbuddy.android.code.test.dogsapp.SnackbarMessage;
import com.dogbuddy.android.code.test.dogsapp.data.Dog;
import com.dogbuddy.android.code.test.dogsapp.databinding.DogsFragBinding;
import com.dogbuddy.android.code.test.dogsapp.util.SnackbarUtils;

import java.util.ArrayList;

/**
 * Display a list of {@link Dog}s.
 */
public class DogsFragment extends Fragment {

    private DogsViewModel mDogsViewModel;

    private DogsFragBinding mDogsFragBinding;

    private DogsAdapter mListAdapter;

    public DogsFragment() {
        // Requires empty public constructor
    }

    public static DogsFragment newInstance() {
        return new DogsFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        mDogsViewModel.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mDogsFragBinding = DogsFragBinding.inflate(inflater, container, false);

        mDogsViewModel = DogsActivity.obtainViewModel(getActivity());

        mDogsFragBinding.setViewmodel(mDogsViewModel);

        setHasOptionsMenu(true);

        return mDogsFragBinding.getRoot();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_dog:
                showAddNewDog();
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.dogs_fragment_menu, menu);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupSnackbar();

        setupListAdapter();

        setupRefreshLayout();
    }

    private void setupSnackbar() {
        mDogsViewModel.getSnackbarMessage().observe(this, new SnackbarMessage.SnackbarObserver() {
            @Override
            public void onNewMessage(@StringRes int snackbarMessageResourceId) {
                SnackbarUtils.showSnackbar(getView(), getString(snackbarMessageResourceId));
            }
        });
    }

    private void showAddNewDog() {
        mDogsViewModel.addNewDog();
    }

    private void setupListAdapter() {
        ListView listView =  mDogsFragBinding.dogsList;

        mListAdapter = new DogsAdapter(
                new ArrayList<Dog>(0),
                mDogsViewModel
        );
        listView.setAdapter(mListAdapter);
    }

    private void setupRefreshLayout() {
        ListView listView =  mDogsFragBinding.dogsList;
        final ScrollChildSwipeRefreshLayout swipeRefreshLayout = mDogsFragBinding.refreshLayout;
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.primary),
                ContextCompat.getColor(getActivity(), R.color.accent),
                ContextCompat.getColor(getActivity(), R.color.primary_dark)
        );
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(listView);
    }

}
