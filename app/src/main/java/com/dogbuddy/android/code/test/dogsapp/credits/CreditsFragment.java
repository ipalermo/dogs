
package com.dogbuddy.android.code.test.dogsapp.credits;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dogbuddy.android.code.test.dogsapp.R;
import com.dogbuddy.android.code.test.dogsapp.databinding.CreditsFragBinding;

/**
 * Main UI for the statistics screen.
 */
public class CreditsFragment extends Fragment {

    private CreditsFragBinding mViewDataBinding;

    private CreditsViewModel mCreditsViewModel;

    public static CreditsFragment newInstance() {
        return new CreditsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mViewDataBinding = DataBindingUtil.inflate(
                inflater, R.layout.credits_frag, container, false);
        return mViewDataBinding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCreditsViewModel = CreditsActivity.obtainViewModel(getActivity());
        mViewDataBinding.setStats(mCreditsViewModel);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCreditsViewModel.start();
    }

    public boolean isActive() {
        return isAdded();
    }
}
