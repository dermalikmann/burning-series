package de.monarchcode.m4lik.burningseries.objects;

/**
 * Created by Malik on 12.06.2016.
 */
public class ShowListItem {

    private Integer id;
    private String title;
    private String genre;
    private boolean fav;

    public ShowListItem(String title, Integer id, String genre, boolean fav) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.fav = fav;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public boolean isFav() {
        return fav;
    }
}
