package de.m4lik.monarchcode.burningseries.objects;

/**
 * Created by Malik on 16.06.2016.
 */
public class HosterListItem {

    private Integer linkid;
    private String hoster;
    private Integer part;

    public HosterListItem(Integer linkid, String hoster, Integer part) {
        this.linkid = linkid;
        this.hoster = hoster;
        this.part = part;
    }

    public Integer getLinkId() {
        return linkid;
    }

    public String getHoster() {
        return hoster;
    }

    public Integer getPart() {
        return part;
    }
}
