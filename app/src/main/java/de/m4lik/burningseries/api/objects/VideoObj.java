package de.m4lik.burningseries.api.objects;

/**
 * Created by Malik on 01.10.2016.
 */

public class VideoObj {

    private String hoster;
    private String url;
    private String fullurl;
    private Integer part;

    public VideoObj(String hoster, String url, String fullurl, Integer part) {
        this.hoster = hoster;
        this.url = url;
        this.fullurl = fullurl;
        this.part = part;
    }

    public String getHoster() {
        return hoster;
    }

    public String getUrl() {
        return url;
    }

    public String getFullUrl() {
        return fullurl;
    }

    public Integer getPart() {
        return part;
    }
}
