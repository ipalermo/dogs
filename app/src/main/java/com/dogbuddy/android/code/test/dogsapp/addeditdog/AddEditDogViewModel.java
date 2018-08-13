
package com.dogbuddy.android.code.test.dogsapp.addeditdog;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.databinding.InverseBindingMethod;
import android.databinding.InverseBindingMethods;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableList;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSpinner;

import com.dogbuddy.android.code.test.dogsapp.SingleLiveEvent;
import com.dogbuddy.android.code.test.dogsapp.R;
import com.dogbuddy.android.code.test.dogsapp.SnackbarMessage;
import com.dogbuddy.android.code.test.dogsapp.data.Breed;
import com.dogbuddy.android.code.test.dogsapp.data.Dog;
import com.dogbuddy.android.code.test.dogsapp.data.source.DogsDataSource;
import com.dogbuddy.android.code.test.dogsapp.data.source.DogsRepository;
import com.dogbuddy.android.code.test.dogsapp.statistics.CreditsViewModel;

import java.util.List;

/**
 * ViewModel for the Add/Edit screen.
 * <p>
 * This ViewModel only exposes {@link ObservableField}s, so it doesn't need to extend
 * {@link android.databinding.BaseObservable} and updates are notified automatically. See
 * {@link CreditsViewModel} for
 * how to deal with more complex scenarios.
 */

@InverseBindingMethods({
        @InverseBindingMethod(type = AppCompatSpinner.class, attribute = "android:selectedItemPosition")
})
public class AddEditDogViewModel extends AndroidViewModel implements DogsDataSource.GetDogCallback, DogsDataSource.LoadBreedsCallback {

    public final ObservableField<String> name = new ObservableField<>();

    public final ObservableField<String> breed = new ObservableField<>();

    public final ObservableList<Breed> breeds = new ObservableArrayList<>();

    public final ObservableBoolean dataLoading = new ObservableBoolean(false);

    private final SnackbarMessage mSnackbarText = new SnackbarMessage();

    private final SingleLiveEvent<Void> mDogUpdated = new SingleLiveEvent<>();

    private final DogsRepository mDogsRepository;

    @Nullable
    private String mDogId;

    private boolean mIsNewDog;

    private boolean mIsDataLoaded = false;

    public AddEditDogViewModel(Application context,
                               DogsRepository dogsRepository) {
        super(context);
        mDogsRepository = dogsRepository;
    }

    public void start(String dogId) {
        if (dataLoading.get()) {
            // Already loading, ignore.
            return;
        }
        mDogId = dogId;
        if (dogId == null) {
            // No need to populate, it's a new dog
            mIsNewDog = true;
            return;
        }
        if (mIsDataLoaded) {
            // No need to populate, already have data.
            return;
        }
        mIsNewDog = false;
        dataLoading.set(true);

        mDogsRepository.getDog(dogId, this);
    }

    @Override
    public void onDogLoaded(Dog dog) {
        name.set(dog.getName());
        breed.set(dog.getBreed());

        // Note that there's no need to notify that the values changed because I´m using
        // ObservableFields.

        mDogsRepository.getBreeds(this);
    }

    @Override
    public void onBreedsLoaded(List<Breed> breeds) {
        dataLoading.set(false);
        mIsDataLoaded = true;

        this.breeds.clear();
        this.breeds.addAll(breeds);
    }

    @Override
    public void onDataNotAvailable() {
        dataLoading.set(false);
    }

    // Called when clicking on done icon.
    void saveDog() {
        Dog dog = new Dog(name.get(), breed.get());
        if (dog.isEmpty()) {
            mSnackbarText.setValue(R.string.empty_dog_message);
            return;
        }
        if (isNewDog() || mDogId == null) {
            createDog(dog);
        } else {
            dog = new Dog(name.get(), breed.get());
            updateDog(dog);
        }
    }

    SnackbarMessage getSnackbarMessage() {
        return mSnackbarText;
    }

    SingleLiveEvent<Void> getDogUpdatedEvent() {
        return mDogUpdated;
    }

    private boolean isNewDog() {
        return mIsNewDog;
    }

    private void createDog(Dog newDog) {
        mDogsRepository.saveDog(newDog);
        mDogUpdated.call();
    }

    private void updateDog(Dog dog) {
        if (isNewDog()) {
            throw new RuntimeException("updateDog() was called but dog is new.");
        }
        mDogsRepository.saveDog(dog);
        mDogUpdated.call();
    }
}