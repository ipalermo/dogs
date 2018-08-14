
package com.dogbuddy.android.code.test.dogsapp.util.bindingadapters;

import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.databinding.InverseBindingListener;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

import com.dogbuddy.android.code.test.dogsapp.data.Breed;
import com.dogbuddy.android.code.test.dogsapp.dogs.BreedsAdapter;

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

    @BindingAdapter(value = {"bind:breed", "bind:breedAttrChanged"}, requireAll = false)
    public static void setBreed(final AppCompatSpinner spinner,
                                final String selectedBreed,
                                final InverseBindingListener changeListener) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (changeListener != null) {
                    changeListener.onChange();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                if (changeListener != null) {
                    changeListener.onChange();
                }
            }
        });

        spinner.setSelection(getIndexOfItem(spinner, selectedBreed));
    }

    @InverseBindingAdapter(attribute = "bind:breed",
    event = "bind:breedAttrChanged")
    public static String getBreed(final AppCompatSpinner spinner) {
       return spinner.getSelectedItem().toString();
    }

    private static int getIndexOfItem(AppCompatSpinner spinner, String breedString) {
        if (spinner == null || breedString == null)
            return 0;

        Adapter a = spinner.getAdapter();

        for (int i = 0; i < a.getCount(); i++) {
            Breed breed = (Breed)a.getItem(i);
            if (breedString.equals(breed.getBreed())) {
                return i;
            }
        }
        return 0;
    }
}
