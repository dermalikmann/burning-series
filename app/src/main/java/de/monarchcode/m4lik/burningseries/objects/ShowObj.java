package de.monarchcode.m4lik.burningseries.objects;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Malik on 01.10.2016.
 */

public class ShowObj {

    @SerializedName(value = "name", alternate = {"series"})
    private String name;
    private Integer id;

    public ShowObj(String name, Integer id) {

        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Integer getId() {
        return id;
    }
}
