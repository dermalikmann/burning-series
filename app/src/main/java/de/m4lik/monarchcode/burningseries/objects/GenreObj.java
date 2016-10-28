package de.m4lik.monarchcode.burningseries.objects;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Malik on 01.10.2016.
 */

public class GenreObj {

    private Integer id;
    @SerializedName("series")
    private ShowObj[] shows;

    public GenreObj(Integer id, ShowObj[] shows) {
        this.id = id;
        this.shows = shows;
    }

    public Integer getId() {
        return id;
    }

    public ShowObj[] getShows() {
        return shows;
    }
}
