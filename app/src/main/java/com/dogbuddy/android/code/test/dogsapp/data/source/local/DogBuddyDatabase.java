/*
 * Copyright 2017, The Android Open Source Project
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

package com.dogbuddy.android.code.test.dogsapp.data.source.local;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.dogbuddy.android.code.test.dogsapp.data.Breed;
import com.dogbuddy.android.code.test.dogsapp.data.Dog;

/**
 * The Room Database that contains the app tables.
 */
@Database(entities = {Dog.class, Breed.class}, version = 1, exportSchema = false)
public abstract class DogBuddyDatabase extends RoomDatabase {

    private static DogBuddyDatabase INSTANCE;

    public abstract DogsDao dogDao();

    private static final Object sLock = new Object();

    public static DogBuddyDatabase getInstance(Context context) {
        synchronized (sLock) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        DogBuddyDatabase.class, "DogBuddy.db")
                        .build();
            }
            return INSTANCE;
        }
    }

}
