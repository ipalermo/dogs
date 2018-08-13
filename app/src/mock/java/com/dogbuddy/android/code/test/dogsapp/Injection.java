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

package com.dogbuddy.android.code.test.dogsapp;

import android.content.Context;
import android.support.annotation.NonNull;

import com.dogbuddy.android.code.test.dogsapp.data.FakeDogsRemoteDataSource;
import com.dogbuddy.android.code.test.dogsapp.data.source.DogsDataSource;
import com.dogbuddy.android.code.test.dogsapp.data.source.DogsRepository;
import com.dogbuddy.android.code.test.dogsapp.data.source.local.DogBuddyDatabase;
import com.dogbuddy.android.code.test.dogsapp.data.source.local.DogsLocalDataSource;
import com.dogbuddy.android.code.test.dogsapp.util.AppExecutors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Enables injection of mock implementations for
 * {@link DogsDataSource} at compile time. This is useful for testing, since it allows us to use
 * a fake instance of the class to isolate the dependencies and run a test hermetically.
 */
public class Injection {

    public static DogsRepository provideTasksRepository(@NonNull Context context) {
        checkNotNull(context);
        DogBuddyDatabase database = DogBuddyDatabase.getInstance(context);
        return DogsRepository.getInstance(FakeDogsRemoteDataSource.getInstance(),
                DogsLocalDataSource.getInstance(new AppExecutors(),
                        database.dogDao()));
    }
}
