package de.squarecoding.burningseries.objects;

/**
 * Created by Malik on 16.06.2016.
 */
public class hosterListItem {

    private String url;
    private String hosterLable;
    //private Integer part;

    public hosterListItem(String url, String hosterLable/*, Integer part*/) {
        this.url = url;
        this.hosterLable = hosterLable;
        //this.part = part;
    }

    public String getUrl() {
        return url;
    }

    public String getHosterLable() {
        return hosterLable;
    }

    /*public Integer getPart() {
        return part;
    }*/
}
