

package com.dogbuddy.android.code.test.dogsapp.dogs;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.dogbuddy.android.code.test.dogsapp.addeditdog.AddEditDogViewModel;
import com.dogbuddy.android.code.test.dogsapp.data.Breed;
import com.dogbuddy.android.code.test.dogsapp.databinding.BreedSpinnerItemBinding;

import java.util.List;


public class BreedsAdapter extends BaseAdapter {

    private final AddEditDogViewModel mAddEditDogViewModel;

    private List<Breed> mBreeds;

    public BreedsAdapter(List<Breed> breeds,
                         AddEditDogViewModel addEditDogViewModel) {
        mAddEditDogViewModel = addEditDogViewModel;
        setList(breeds);

    }

    public void replaceData(List<Breed> breeds) {
        setList(breeds);
    }

    @Override
    public int getCount() {
        return mBreeds != null ? mBreeds.size() : 0;
    }

    @Override
    public Breed getItem(int position) {
        return mBreeds.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, final View view, final ViewGroup viewGroup) {
        BreedSpinnerItemBinding binding;
        if (view == null) {
            // Inflate
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

            // Create the binding
            binding = BreedSpinnerItemBinding.inflate(inflater, viewGroup, false);
        } else {
            // Recycling view
            binding = DataBindingUtil.getBinding(view);
        }

        BreedItemUserActionsListener userActionsListener = new BreedItemUserActionsListener() {
            @Override
            public void onBreedClicked(Breed breed) {
                mAddEditDogViewModel.breed.set(breed.getBreed());
            }
        };

        binding.setBreed(mBreeds.get(position));

        binding.setListener(userActionsListener);

        binding.executePendingBindings();
        return binding.getRoot();
    }


    private void setList(List<Breed> breeds) {
        mBreeds = breeds;
        notifyDataSetChanged();
    }
}
