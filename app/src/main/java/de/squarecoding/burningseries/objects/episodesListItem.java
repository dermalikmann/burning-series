package de.squarecoding.burningseries.objects;

/**
 * Created by Malik on 12.06.2016.
 */
public class episodesListItem {

    private String titleGer;
    private String title;
    private String url;

    public episodesListItem(String titleGer, String title, String url) {
        this.titleGer = titleGer;
        this.title = title;
        this.url = url;
    }

    public String getTitleGer() {
        return titleGer;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}
