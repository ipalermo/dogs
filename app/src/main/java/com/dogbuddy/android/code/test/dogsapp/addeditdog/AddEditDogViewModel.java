
package com.dogbuddy.android.code.test.dogsapp.addeditdog;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Context;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableList;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;

import com.dogbuddy.android.code.test.dogsapp.R;
import com.dogbuddy.android.code.test.dogsapp.SingleLiveEvent;
import com.dogbuddy.android.code.test.dogsapp.SnackbarMessage;
import com.dogbuddy.android.code.test.dogsapp.credits.CreditsViewModel;
import com.dogbuddy.android.code.test.dogsapp.data.Breed;
import com.dogbuddy.android.code.test.dogsapp.data.Dog;
import com.dogbuddy.android.code.test.dogsapp.data.DogBuilder;
import com.dogbuddy.android.code.test.dogsapp.data.source.DogsDataSource;
import com.dogbuddy.android.code.test.dogsapp.data.source.DogsRepository;

import java.util.List;

/**
 * ViewModel for the Add/Edit screen.
 * <p>
 * This ViewModel only exposes {@link ObservableField}s, so it doesn't need to extend
 * {@link android.databinding.BaseObservable} and updates are notified automatically. See
 * {@link CreditsViewModel} for
 * how to deal with more complex scenarios.
 */

public class AddEditDogViewModel extends AndroidViewModel implements DogsDataSource.GetDogCallback, DogsDataSource.LoadBreedsCallback {

    public final ObservableField<String> name = new ObservableField<>();
    public final ObservableField<String> breed = new ObservableField<>();
    public final ObservableField<String> gender = new ObservableField<>();

    public final ObservableList<Breed> breeds = new ObservableArrayList<>();
    public final ObservableField<ArrayAdapter> genderAdapter = new ObservableField<>();

    public final ObservableBoolean dataLoading = new ObservableBoolean(false);

    private final SnackbarMessage mSnackbarText = new SnackbarMessage();

    private final SingleLiveEvent<Void> mDogUpdated = new SingleLiveEvent<>();

    private final Context mContext; // To avoid leaks, this must be an Application Context.

    private final DogsRepository mDogsRepository;

    @Nullable
    private String mDogId;

    private boolean mIsNewDog;

    private boolean mIsDataLoaded = false;

    public AddEditDogViewModel(Application context,
                               DogsRepository dogsRepository) {
        super(context);
        mContext = context.getApplicationContext();
        mDogsRepository = dogsRepository;
        setupSpinnerAdapters();
    }

    public void start(String dogId) {
        if (dataLoading.get()) {
            // Already loading, ignore.
            return;
        }
        mDogId = dogId;
        mIsNewDog = dogId == null;

        if (mIsDataLoaded) {
            // No need to populate, already have dog data.
            return;
        }
        dataLoading.set(true);
        mDogsRepository.getBreeds(this);
    }

    @Override
    public void onDogLoaded(Dog dog) {
        name.set(dog.getName());
        breed.set(dog.getBreed());
        gender.set(dog.getGender());

        mIsDataLoaded = true;
        dataLoading.set(false);

        // Note that there's no need to notify that the values changed because I´m using
        // ObservableFields.
    }

    @Override
    public void onBreedsLoaded(List<Breed> breeds) {
        this.breeds.clear();
        this.breeds.addAll(breeds);
        dataLoading.set(false);

        if (!mIsNewDog) {
            dataLoading.set(true);
            mDogsRepository.getDog(mDogId, this);
        }
    }

    @Override
    public void onDataNotAvailable() {
        dataLoading.set(false);
    }

    // Called when clicking on done icon.
    void saveDog() {
        Dog dog = new DogBuilder()
                .setName(name.get())
                .setBreed(breed.get())
                .setGender(gender.get())
                .createDog();
        if (dog.isRequiredInfoMissing()) {
            mSnackbarText.setValue(R.string.empty_dog_message);
            return;
        }
        if (isNewDog() || mDogId == null) {
            createDog(dog);
        } else {
            dog = new DogBuilder()
                    .setName(name.get())
                    .setBreed(breed.get())
                    .setId(mDogId)
                    .setGender(gender.get())
                    .createDog();
            updateDog(dog);
        }
    }

    SnackbarMessage getSnackbarMessage() {
        return mSnackbarText;
    }

    SingleLiveEvent<Void> getDogUpdatedEvent() {
        return mDogUpdated;
    }

    private void setupSpinnerAdapters() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext, R.array.genders, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderAdapter.set(adapter);
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
