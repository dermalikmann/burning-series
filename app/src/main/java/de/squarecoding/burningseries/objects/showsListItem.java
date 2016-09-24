package de.squarecoding.burningseries.objects;

/**
 * Created by Malik on 12.06.2016.
 */
public class showsListItem {

    private String title;
    private String url;
    private String genre;

    public showsListItem(String title, String url, String genre) {
        this.title = title;
        this.url = url;
        this.genre = genre;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getGenre() {
        return genre;
    }
}
