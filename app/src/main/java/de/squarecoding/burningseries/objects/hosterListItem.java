package de.squarecoding.burningseries.objects;

/**
 * Created by Malik on 16.06.2016.
 */
public class hosterListItem {

    private String url;
    private String hosterLable;
    private String partLable;

    public hosterListItem(String url, String hosterLable, String partLable) {
        this.url = url;
        this.hosterLable = hosterLable;
        this.partLable = partLable;
    }

    public String getUrl() {
        return url;
    }

    public String getHosterLable() {
        return hosterLable;
    }

    public String getPartLable() {
        return partLable;
    }
}
