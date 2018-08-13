
package com.dogbuddy.android.code.test.dogsapp.dogs;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableList;

import com.dogbuddy.android.code.test.dogsapp.SingleLiveEvent;
import com.dogbuddy.android.code.test.dogsapp.R;
import com.dogbuddy.android.code.test.dogsapp.SnackbarMessage;
import com.dogbuddy.android.code.test.dogsapp.addeditdog.AddEditDogActivity;
import com.dogbuddy.android.code.test.dogsapp.data.Dog;
import com.dogbuddy.android.code.test.dogsapp.data.source.DogsDataSource;
import com.dogbuddy.android.code.test.dogsapp.data.source.DogsRepository;
import com.dogbuddy.android.code.test.dogsapp.dogdetail.DogDetailActivity;

import java.util.List;


/**
 * Exposes the data to be used in the dog list screen.
 * <p>
 * {@link BaseObservable} implements a listener registration mechanism which is notified when a
 * property changes. This is done by assigning a {@link Bindable} annotation to the property's
 * getter method.
 */
public class DogsViewModel extends AndroidViewModel {

    // These observable fields will update Views automatically
    public final ObservableList<Dog> items = new ObservableArrayList<>();

    public final ObservableBoolean dataLoading = new ObservableBoolean(false);

    public final ObservableField<String> noDogsLabel = new ObservableField<>();

    public final ObservableBoolean empty = new ObservableBoolean(false);

    public final ObservableBoolean dogsAddViewVisible = new ObservableBoolean();

    private final SnackbarMessage mSnackbarText = new SnackbarMessage();

    private final DogsRepository mDogsRepository;

    private final ObservableBoolean mIsDataLoadingError = new ObservableBoolean(false);

    private final SingleLiveEvent<String> mOpenDogEvent = new SingleLiveEvent<>();

    private final Context mContext; // To avoid leaks, this must be an Application Context.

    private final SingleLiveEvent<Void> mNewDogEvent = new SingleLiveEvent<>();

    public DogsViewModel(
            Application context,
            DogsRepository repository) {
        super(context);
        mContext = context.getApplicationContext(); // Force use of Application Context.
        mDogsRepository = repository;

        noDogsLabel.set(mContext.getResources().getString(R.string.no_dogs_yet));
        dogsAddViewVisible.set(true);
    }

    public void start() {
        loadDogs(false);
    }

    public void loadDogs(boolean forceUpdate) {
        loadDogs(forceUpdate, true);
    }

    SnackbarMessage getSnackbarMessage() {
        return mSnackbarText;
    }

    SingleLiveEvent<String> getOpenDogEvent() {
        return mOpenDogEvent;
    }

    SingleLiveEvent<Void> getNewDogEvent() {
        return mNewDogEvent;
    }

    private void showSnackbarMessage(Integer message) {
        mSnackbarText.setValue(message);
    }

    /**
     * Called by the Data Binding library and the menu item.
     */
    public void addNewDog() {
        mNewDogEvent.call();
    }

    void handleActivityResult(int requestCode, int resultCode) {
        if (AddEditDogActivity.REQUEST_CODE == requestCode) {
            switch (resultCode) {
                case DogDetailActivity.EDIT_RESULT_OK:
                    mSnackbarText.setValue(R.string.successfully_saved_dog_message);
                    break;
                case AddEditDogActivity.ADD_EDIT_RESULT_OK:
                    mSnackbarText.setValue(R.string.successfully_added_dog_message);
                    break;
                case DogDetailActivity.DELETE_RESULT_OK:
                    mSnackbarText.setValue(R.string.successfully_deleted_dog_message);
                    break;
            }
        }
    }

    /**
     * @param forceUpdate   Pass in true to refresh the data in the {@link DogsDataSource}
     * @param showLoadingUI Pass in true to display a loading icon in the UI
     */
    private void loadDogs(boolean forceUpdate, final boolean showLoadingUI) {
        if (showLoadingUI) {
            dataLoading.set(true);
        }
        if (forceUpdate) {

            mDogsRepository.refreshDogs();
        }

        mDogsRepository.getDogs(new DogsDataSource.LoadDogsCallback() {
            @Override
            public void onDogsLoaded(List<Dog> dogs) {
                if (showLoadingUI) {
                    dataLoading.set(false);
                }
                mIsDataLoadingError.set(false);

                items.clear();
                items.addAll(dogs);
                empty.set(items.isEmpty());
            }

            @Override
            public void onDataNotAvailable() {
                mIsDataLoadingError.set(true);
            }
        });
    }
}
