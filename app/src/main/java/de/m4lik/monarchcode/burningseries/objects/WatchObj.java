package de.m4lik.monarchcode.burningseries.objects;

/**
 * Created by Malik on 01.10.2016.
 */

public class WatchObj {

    private String hoster;
    private String url;
    private String fullurl;
    private Integer part;

    public WatchObj(String hoster, String url, String fullurl, Integer part) {
        this.hoster = hoster;
        this.url = url;
        this.fullurl = fullurl;
        this.part = part;
    }

    public String getHoster() {
        return hoster;
    }

    public String getFullUrl() {
        return url;
    }

    public String getFullurl() {
        return fullurl;
    }

    public Integer getPart() {
        return part;
    }
}
