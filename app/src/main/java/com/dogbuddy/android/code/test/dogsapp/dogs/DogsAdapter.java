

package com.dogbuddy.android.code.test.dogsapp.dogs;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.dogbuddy.android.code.test.dogsapp.data.Dog;
import com.dogbuddy.android.code.test.dogsapp.databinding.DogItemBinding;

import java.util.List;


public class DogsAdapter extends BaseAdapter {

    private final DogsViewModel mDogsViewModel;

    private List<Dog> mDogs;

    public DogsAdapter(List<Dog> dogs,
                       DogsViewModel dogsViewModel) {
        mDogsViewModel = dogsViewModel;
        setList(dogs);

    }

    public void replaceData(List<Dog> dogs) {
        setList(dogs);
    }

    @Override
    public int getCount() {
        return mDogs != null ? mDogs.size() : 0;
    }

    @Override
    public Dog getItem(int position) {
        return mDogs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, final View view, final ViewGroup viewGroup) {
        DogItemBinding binding;
        if (view == null) {
            // Inflate
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

            // Create the binding
            binding = DogItemBinding.inflate(inflater, viewGroup, false);
        } else {
            // Recycling view
            binding = DataBindingUtil.getBinding(view);
        }

        DogItemUserActionsListener userActionsListener = new DogItemUserActionsListener() {

            @Override
            public void onDogClicked(Dog task) {
                mDogsViewModel.getOpenDogEvent().setValue(task.getId());
            }
        };

        binding.setDog(mDogs.get(position));

        binding.setListener(userActionsListener);

        binding.executePendingBindings();
        return binding.getRoot();
    }


    private void setList(List<Dog> dogs) {
        mDogs = dogs;
        notifyDataSetChanged();
    }
}
