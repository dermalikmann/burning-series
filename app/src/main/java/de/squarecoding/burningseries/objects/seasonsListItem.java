package de.squarecoding.burningseries.objects;

/**
 * Created by Malik on 16.06.2016.
 */
public class seasonsListItem {

    private String url;
    private Integer seasonId;

    public seasonsListItem(String url, Integer seasonId) {
        this.seasonId = seasonId;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public Integer getSeasonId() {
        return seasonId;
    }
}
