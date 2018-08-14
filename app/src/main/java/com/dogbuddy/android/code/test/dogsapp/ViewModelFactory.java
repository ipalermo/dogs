

package com.dogbuddy.android.code.test.dogsapp;

import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.VisibleForTesting;

import com.dogbuddy.android.code.test.dogsapp.addeditdog.AddEditDogViewModel;
import com.dogbuddy.android.code.test.dogsapp.credits.CreditsViewModel;
import com.dogbuddy.android.code.test.dogsapp.data.source.DogsRepository;
import com.dogbuddy.android.code.test.dogsapp.dogdetail.DogDetailViewModel;
import com.dogbuddy.android.code.test.dogsapp.dogs.DogsViewModel;

/**
 * A creator is used to inject the product ID into the ViewModel
 * <p>
 * This creator is to showcase how to inject dependencies into ViewModels. It's not
 * actually necessary in this case, as the product ID can be passed in a public method.
 */
public class ViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    @SuppressLint("StaticFieldLeak")
    private static volatile ViewModelFactory INSTANCE;

    private final Application mApplication;

    private final DogsRepository mDogsRepository;

    public static ViewModelFactory getInstance(Application application) {

        if (INSTANCE == null) {
            synchronized (ViewModelFactory.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ViewModelFactory(application,
                            Injection.provideDogsRepository(application.getApplicationContext()));
                }
            }
        }
        return INSTANCE;
    }

    @VisibleForTesting
    public static void destroyInstance() {
        INSTANCE = null;
    }

    private ViewModelFactory(Application application, DogsRepository repository) {
        mApplication = application;
        mDogsRepository = repository;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CreditsViewModel.class)) {
            //noinspection unchecked
            return (T) new CreditsViewModel(mApplication);
        } else if (modelClass.isAssignableFrom(DogDetailViewModel.class)) {
            //noinspection unchecked
            return (T) new DogDetailViewModel(mApplication, mDogsRepository);
        } else if (modelClass.isAssignableFrom(AddEditDogViewModel.class)) {
            //noinspection unchecked
            return (T) new AddEditDogViewModel(mApplication, mDogsRepository);
        } else if (modelClass.isAssignableFrom(DogsViewModel.class)) {
            //noinspection unchecked
            return (T) new DogsViewModel(mApplication, mDogsRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
