package de.monarchcode.m4lik.burningseries.objects;

/**
 * Created by Malik on 12.06.2016.
 */
public class GenreListItem {

    private Integer id;
    private String lable;

    public GenreListItem(Integer id, String lable) {
        this.id = id;
        this.lable = lable;
    }

    public Integer getId() {
        return id;
    }

    public String getLable() {
        return lable;
    }
}
