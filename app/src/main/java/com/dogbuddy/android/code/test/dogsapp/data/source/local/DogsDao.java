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

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.dogbuddy.android.code.test.dogsapp.data.Breed;
import com.dogbuddy.android.code.test.dogsapp.data.Dog;

import java.util.List;

/**
 * Data Access Object for the dogs table.
 */
@Dao
public interface DogsDao {

    /**
     * Select all dogs from the dogs table.
     *
     * @return all dogs.
     */
    @Query("SELECT * FROM dogs")
    List<Dog> getDogs();

    /**
     * Select all breeds from the breeds table.
     *
     * @return all breeds.
     */
    @Query("SELECT * FROM breeds")
    List<Breed> getBreeds();

    /**
     * Select a dog by id.
     *
     * @param dogId the dog id.
     * @return the dog with dogId.
     */
    @Query("SELECT * FROM dogs WHERE entryid = :dogId")
    Dog getDogById(String dogId);

    /**
     * Insert a dog in the database. If the dog already exists, replace it.
     *
     * @param dog the dog to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDog(Dog dog);

    /**
     * Insert a breed in the database. If the breed already exists, replace it.
     *
     * @param breed the breed to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBreed(Breed breed);

    /**
     * Update a dog.
     *
     * @param dog dog to be updated
     * @return the number of dogs updated. This should always be 1.
     */
    @Update
    int updateDog(Dog dog);

    /**
     * Update the complete status of a dog
     *
     * @param dogId    id of the dog
     * @param completed status to be updated
     */
    @Query("UPDATE dogs SET completed = :completed WHERE entryid = :dogId")
    void updateCompleted(String dogId, boolean completed);

    /**
     * Delete a dog by id.
     *
     * @return the number of dogs deleted. This should always be 1.
     */
    @Query("DELETE FROM dogs WHERE entryid = :dogId")
    int deleteDogById(String dogId);

    /**
     * Delete all dogs.
     */
    @Query("DELETE FROM dogs")
    void deleteDogs();

    /**
     * Delete all breeds.
     */
    @Query("DELETE FROM breeds")
    void deleteBreeds();

    /**
     * Delete all completed dogs from the table.
     *
     * @return the number of dogs deleted.
     */
    @Query("DELETE FROM dogs WHERE completed = 1")
    int deleteCompletedDogs();
}
