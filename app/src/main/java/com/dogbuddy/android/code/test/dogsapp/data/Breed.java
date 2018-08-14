
package com.dogbuddy.android.code.test.dogsapp.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import java.util.UUID;

/**
 * Immutable model class that holds dog breeds.
 */
@Entity(tableName = "breeds")
public final class Breed {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "entryid")
    private final String mId;

    @Nullable
    @ColumnInfo(name = "breed")
    private final String mBreed;

    /**
     * Use this constructor to create a new Breed.
     *
     * @param breed breed of the dog
     */
    @Ignore
    public Breed(@Nullable String breed) {
        this(breed, UUID.randomUUID().toString());
    }

    /**
     * Use this constructor to create an active Dog if the Dog already has an id (copy of another
     * Dog).
     *
     * @param breed breed of the dog
     * @param id    id of the dog
     */
    public Breed(@Nullable String breed, @NonNull String id) {
        this.mId = id;
        this.mBreed = breed;
    }

    @NonNull
    public String getId() {
        return mId;
    }

    @Nullable
    public String getBreed() {
        return mBreed;
    }

    public boolean isEmpty() {
        return Strings.isNullOrEmpty(mBreed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Breed breed = (Breed) o;
        return Objects.equal(mId, breed.mId) &&
               Objects.equal(mBreed, breed.mBreed);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mId, mBreed);
    }

    @Override
    public String toString() {
        return mBreed;
    }
}
