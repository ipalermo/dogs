
package com.dogbuddy.android.code.test.dogsapp.util.bindingadapters;

import android.databinding.BindingAdapter;
import android.widget.ListView;

import com.dogbuddy.android.code.test.dogsapp.data.Dog;
import com.dogbuddy.android.code.test.dogsapp.dogs.DogsAdapter;

import java.util.List;

/**
 * Contains {@link BindingAdapter}s for the {@link Dog} list.
 */
public class DogsListBindings {

    @SuppressWarnings("unchecked")
    @BindingAdapter("app:items")
    public static void setItems(ListView listView, List<Dog> items) {
        DogsAdapter adapter = (DogsAdapter) listView.getAdapter();
        if (adapter != null)
        {
            adapter.replaceData(items);
        }
    }
}
