package com.dogbuddy.android.code.test.dogsapp;

import android.content.Context;
import android.support.annotation.NonNull;

import com.dogbuddy.android.code.test.dogsapp.data.source.DogsRepository;
import com.dogbuddy.android.code.test.dogsapp.data.source.local.DogsLocalDataSource;
import com.dogbuddy.android.code.test.dogsapp.data.source.local.DogBuddyDatabase;
import com.dogbuddy.android.code.test.dogsapp.data.source.remote.DogsRemoteDataSource;
import com.dogbuddy.android.code.test.dogsapp.util.AppExecutors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Enables injection of production implementations for
 * {@link DogsDataSource} at compile time.
 */
public class Injection {

    public static DogsRepository provideDogsRepository(@NonNull Context context) {
        checkNotNull(context);
        DogBuddyDatabase database = DogBuddyDatabase.getInstance(context);
        return DogsRepository.getInstance(DogsRemoteDataSource.getInstance(),
                DogsLocalDataSource.getInstance(new AppExecutors(),
                        database.dogDao()));
    }
}
