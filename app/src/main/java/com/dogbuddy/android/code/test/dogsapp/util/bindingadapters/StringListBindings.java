
package com.dogbuddy.android.code.test.dogsapp.util.bindingadapters;

import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.databinding.InverseBindingListener;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

import com.dogbuddy.android.code.test.dogsapp.data.Breed;

/**
 * Contains {@link BindingAdapter}s for the {@link Breed} list.
 */
public class StringListBindings {

    @BindingAdapter(value = {"bind:selectedOption", "bind:selectedOptionAttrChanged"}, requireAll = false)
    public static void setBreed(final AppCompatSpinner spinner,
                                final String selectedOption,
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

        spinner.setSelection(getIndexOfItem(spinner, selectedOption));
    }

    @InverseBindingAdapter(attribute = "bind:selectedOption",
    event = "bind:selectedOptionAttrChanged")
    public static String getSelectedOption(final AppCompatSpinner spinner) {
       return (String)spinner.getSelectedItem();
    }

    private static int getIndexOfItem(AppCompatSpinner spinner, String selectedOption) {
        if (spinner == null || selectedOption == null)
            return 0;

        Adapter a = spinner.getAdapter();

        for (int i = 0; i < a.getCount(); i++) {
            if (selectedOption.equals(a.getItem(i))) {
                return i;
            }
        }
        return 0;
    }
}
