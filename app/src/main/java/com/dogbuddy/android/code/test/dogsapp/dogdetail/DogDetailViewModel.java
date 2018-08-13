
package com.dogbuddy.android.code.test.dogsapp.dogdetail;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.dogbuddy.android.code.test.dogsapp.SingleLiveEvent;
import com.dogbuddy.android.code.test.dogsapp.R;
import com.dogbuddy.android.code.test.dogsapp.SnackbarMessage;
import com.dogbuddy.android.code.test.dogsapp.data.Dog;
import com.dogbuddy.android.code.test.dogsapp.data.source.DogsDataSource;
import com.dogbuddy.android.code.test.dogsapp.data.source.DogsRepository;
import com.dogbuddy.android.code.test.dogsapp.dogs.DogsFragment;


/**
 * Listens to user actions from the list item in ({@link DogsFragment}) and redirects them to the
 * Fragment's actions listener.
 */
public class DogDetailViewModel extends AndroidViewModel implements DogsDataSource.GetDogCallback {

    public final ObservableField<Dog> dog = new ObservableField<>();

    private final SingleLiveEvent<Void> mEditDogCommand = new SingleLiveEvent<>();

    private final SingleLiveEvent<Void> mDeleteDogCommand = new SingleLiveEvent<>();

    private final DogsRepository mDogsRepository;

    private final SnackbarMessage mSnackbarText = new SnackbarMessage();

    private boolean mIsDataLoading;

    public DogDetailViewModel(Application context, DogsRepository tasksRepository) {
        super(context);
        mDogsRepository = tasksRepository;
    }

    public void deleteDog() {
        if (dog.get() != null) {
            mDogsRepository.deleteDog(dog.get().getId());
            mDeleteDogCommand.call();
        }
    }

    public void editDog() {
        mEditDogCommand.call();
    }

    public SnackbarMessage getSnackbarMessage() {
        return mSnackbarText;
    }

    public SingleLiveEvent<Void> getEditDogCommand() {
        return mEditDogCommand;
    }

    public SingleLiveEvent<Void> getDeleteDogCommand() {
        return mDeleteDogCommand;
    }

    public void start(String dogId) {
        if (dogId != null) {
            mIsDataLoading = true;
            mDogsRepository.getDog(dogId, this);
        }
    }

    public void setDog(Dog dog) {
        this.dog.set(dog);
    }

    public boolean isDataAvailable() {
        return dog.get() != null;
    }

    public boolean isDataLoading() {
        return mIsDataLoading;
    }

    @Override
    public void onDogLoaded(Dog dog) {
        setDog(dog);
        mIsDataLoading = false;
    }

    @Override
    public void onDataNotAvailable() {
        dog.set(null);
        mIsDataLoading = false;
    }

    public void onRefresh() {
        if (dog.get() != null) {
            start(dog.get().getId());
        }
    }

    @Nullable
    protected String getDogId() {
        return dog.get().getId();
    }

    private void showSnackbarMessage(@StringRes Integer message) {
        mSnackbarText.setValue(message);
    }
}
