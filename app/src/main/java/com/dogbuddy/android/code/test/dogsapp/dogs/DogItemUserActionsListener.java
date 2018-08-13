

package com.dogbuddy.android.code.test.dogsapp.dogs;


import android.view.View;

import com.dogbuddy.android.code.test.dogsapp.data.Dog;

/**
 * Listener used with data binding to process user actions.
 */
public interface DogItemUserActionsListener {

    void onDogClicked(Dog dog);
}
