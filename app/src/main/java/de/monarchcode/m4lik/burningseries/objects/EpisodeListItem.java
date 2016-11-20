package de.monarchcode.m4lik.burningseries.objects;

/**
 * Created by Malik on 12.06.2016.
 */
public class EpisodeListItem {

    private String titleGer;
    private String title;
    private Integer id;
    private Boolean watched;

    public EpisodeListItem(String titleGer, String title, Integer id, Boolean watched) {
        this.titleGer = titleGer;
        this.title = title;
        this.id = id;
        this.watched = watched;
    }

    public String getTitleGer() {
        return titleGer;
    }

    public String getTitle() {
        return title;
    }

    public Integer getId() {
        return id;
    }

    public Boolean isWatched() {
        return watched;
    }
}
