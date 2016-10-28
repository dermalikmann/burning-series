package de.m4lik.monarchcode.burningseries.objects;

/**
 * Created by Malik on 12.06.2016.
 */
public class EpisodeListItem {

    private String titleGer;
    private String title;
    private Integer id;

    public EpisodeListItem(String titleGer, String title, Integer id) {
        this.titleGer = titleGer;
        this.title = title;
        this.id = id;
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
}
