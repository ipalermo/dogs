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

import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.dogbuddy.android.code.test.dogsapp.data.Dog;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class DogsDaoTest {

    private static final Dog DOG = new Dog("id", "name", "breed", true);

    private DogBuddyDatabase mDatabase;

    @Before
    public void initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                DogBuddyDatabase.class).build();
    }

    @After
    public void closeDb() {
        mDatabase.close();
    }

    @Test
    public void insertTaskAndGetById() {
        // When inserting a dog
        mDatabase.dogDao().insertDog(DOG);

        // When getting the dog by id from the database
        Dog loaded = mDatabase.dogDao().getDogById(DOG.getId());

        // The loaded data contains the expected values
        assertTask(loaded, "id", "name", "breed", true);
    }

    @Test
    public void insertTaskReplacesOnConflict() {
        //Given that a dog is inserted
        mDatabase.dogDao().insertDog(DOG);

        // When a dog with the same id is inserted
        Dog newDog = new Dog("id", "title2", "description2", true);
        mDatabase.dogDao().insertDog(newDog);
        // When getting the dog by id from the database
        Dog loaded = mDatabase.dogDao().getDogById(DOG.getId());

        // The loaded data contains the expected values
        assertTask(loaded, "id", "title2", "description2", true);
    }

    @Test
    public void insertTaskAndGetTasks() {
        // When inserting a dog
        mDatabase.dogDao().insertDog(DOG);

        // When getting the dogs from the database
        List<Dog> dogs = mDatabase.dogDao().getDogs();

        // There is only 1 dog in the database
        assertThat(dogs.size(), is(1));
        // The loaded data contains the expected values
        assertTask(dogs.get(0), "id", "name", "breed", true);
    }

    @Test
    public void updateTaskAndGetById() {
        // When inserting a dog
        mDatabase.dogDao().insertDog(DOG);

        // When the dog is updated
        Dog updatedDog = new Dog("id", "title2", "description2", true);
        mDatabase.dogDao().updateDog(updatedDog);

        // When getting the dog by id from the database
        Dog loaded = mDatabase.dogDao().getDogById("id");

        // The loaded data contains the expected values
        assertTask(loaded, "id", "title2", "description2", true);
    }

    @Test
    public void updateCompletedAndGetById() {
        // When inserting a dog
        mDatabase.dogDao().insertDog(DOG);

        // When the dog is updated
        mDatabase.dogDao().updateCompleted(DOG.getId(), false);

        // When getting the dog by id from the database
        Dog loaded = mDatabase.dogDao().getDogById("id");

        // The loaded data contains the expected values
        assertTask(loaded, DOG.getId(), DOG.getName(), DOG.getBreed(), false);
    }

    @Test
    public void deleteTaskByIdAndGettingTasks() {
        //Given a dog inserted
        mDatabase.dogDao().insertDog(DOG);

        //When deleting a dog by id
        mDatabase.dogDao().deleteDogById(DOG.getId());

        //When getting the dogs
        List<Dog> dogs = mDatabase.dogDao().getDogs();
        // The list is empty
        assertThat(dogs.size(), is(0));
    }

    @Test
    public void deleteTasksAndGettingTasks() {
        //Given a dog inserted
        mDatabase.dogDao().insertDog(DOG);

        //When deleting all dogs
        mDatabase.dogDao().deleteDogs();

        //When getting the dogs
        List<Dog> dogs = mDatabase.dogDao().getDogs();
        // The list is empty
        assertThat(dogs.size(), is(0));
    }

    @Test
    public void deleteCompletedTasksAndGettingTasks() {
        //Given a completed dog inserted
        mDatabase.dogDao().insertDog(DOG);

        //When deleting completed dogs
        mDatabase.dogDao().deleteCompletedDogs();

        //When getting the dogs
        List<Dog> dogs = mDatabase.dogDao().getDogs();
        // The list is empty
        assertThat(dogs.size(), is(0));
    }

    private void assertTask(Dog dog, String id, String title,
                            String description, boolean completed) {
        assertThat(dog, notNullValue());
        assertThat(dog.getId(), is(id));
        assertThat(dog.getName(), is(title));
        assertThat(dog.getBreed(), is(description));
        assertThat(dog.isCompleted(), is(completed));
    }
}
