package com.dogbuddy.android.code.test.dogsapp.data;

import java.util.UUID;

public class DogBuilder {
    private String name;
    private String breed;
    private String gender;
    private String id = UUID.randomUUID().toString();

    public DogBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public DogBuilder setBreed(String breed) {
        this.breed = breed;
        return this;
    }

    public DogBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public DogBuilder setGender(String gender) {
        this.gender = gender;
        return this;
    }

    public Dog createDog() {
        return new Dog(name, breed, id, gender);
    }
}