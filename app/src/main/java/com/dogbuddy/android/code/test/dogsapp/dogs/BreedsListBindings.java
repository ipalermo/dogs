
package com.dogbuddy.android.code.test.dogsapp.dogs;

import android.databinding.BindingAdapter;
import android.support.v7.widget.AppCompatSpinner;
import android.widget.ListView;

import com.dogbuddy.android.code.test.dogsapp.data.Breed;
import com.dogbuddy.android.code.test.dogsapp.data.Dog;

import java.util.List;

/**
 * Contains {@link BindingAdapter}s for the {@link Breed} list.
 */
public class BreedsListBindings {

    @SuppressWarnings("unchecked")
    @BindingAdapter("android:entries")
    public static void setItems(AppCompatSpinner spinner, List<Breed> items) {
        BreedsAdapter adapter = (BreedsAdapter) spinner.getAdapter();
        if (adapter != null)
        {
            adapter.replaceData(items);
        }
    }
}
