package com.dogbuddy.android.code.test.dogsapp.credits;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Context;

public class CreditsViewModel extends AndroidViewModel {

    private final Context mContext;


    public CreditsViewModel(Application context) {
        super(context);
        mContext = context;
    }

    public void start() {
    }

}
