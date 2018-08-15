package com.dogbuddy.android.code.test.dogsapp.data;

import java.util.UUID;

public class DogBuilder {
    private String name;
    private String breed;
    private String gender;
    private String size;
    private Integer birthYear;
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

    public DogBuilder setBirthYear(Integer birthYear) {
        this.birthYear = birthYear;
        return this;
    }

    public DogBuilder setSize(String size) {
        this.size = size;
        return this;
    }

    public Dog createDog() {
        return new Dog(id, name, breed, gender, birthYear, size);
    }
}