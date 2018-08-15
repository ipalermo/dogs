
package com.dogbuddy.android.code.test.dogsapp.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import java.util.Calendar;
import java.util.UUID;


/**
 * Immutable model class for a Dog.
 */
@Entity(tableName = "dogs")
public final class Dog {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "entryid")
    private final String mId;

    @NonNull
    @ColumnInfo(name = "name")
    private final String mName;

    @NonNull
    @ColumnInfo(name = "breed")
    private final String mBreed;

    @Nullable
    @ColumnInfo(name = "gender")
    private final String mGender;

    @ColumnInfo(name = "birthYear")
    private final Integer mBirthYear;

    @Nullable
    @ColumnInfo(name = "size")
    private final String mSize;

    /**
     * Use this constructor to create a new Dog.
     *
     * @param name       name of the dog
     * @param breed breed of the dog
     */
    @Ignore
    public Dog(@NonNull String name, @NonNull String breed) {
        this(UUID.randomUUID().toString(), name, breed);
    }

    @Ignore
    public Dog(@NonNull String name, @NonNull String breed, @NonNull String id) {
        this(id, name, breed, "");
    }

    @Ignore
    public Dog(@NonNull String id, @NonNull String name, @NonNull String breed, @NonNull String gender) {
        this(id, name, breed, gender, Calendar.getInstance().get(Calendar.YEAR), "");
    }

    /**
     * Use this constructor to create a Dog if the Dog already has an id (copy of another
     * Dog).
     * @param id        id of the dog
     * @param name      name of the dog
     * @param breed     breed of the dog
     * @param gender    gender of the dog
     */
    public Dog(@NonNull String id, @NonNull String name, @NonNull String breed, @Nullable String gender,
               @NonNull Integer birthYear, @NonNull String size) {
        this.mName = name;
        this.mBreed = breed;
        this.mId = id;
        this.mGender = gender;
        this.mBirthYear = birthYear;
        this.mSize = size;
    }

    @NonNull
    public String getId() {
        return mId;
    }

    @NonNull
    public String getName() {
        return mName;
    }

    @NonNull
    public String getBreed() {
        return mBreed;
    }

    @Nullable
    public String getGender() {
        return mGender;
    }

    @Nullable
    public String getSize() {
        return mSize;
    }

    @NonNull
    public Integer getBirthYear() {
        return mBirthYear;
    }

    public boolean isRequiredInfoMissing() {
        return Strings.isNullOrEmpty(mName) ||
               Strings.isNullOrEmpty(mBreed) ||
               Strings.isNullOrEmpty(mGender) ||
               mBirthYear == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dog dog = (Dog) o;
        return Objects.equal(mId, dog.mId) &&
               Objects.equal(mName, dog.mName) &&
               Objects.equal(mBreed, dog.mBreed);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mId, mName, mBreed);
    }

    @Override
    public String toString() {
        return "Dog with name " + mName;
    }
}
