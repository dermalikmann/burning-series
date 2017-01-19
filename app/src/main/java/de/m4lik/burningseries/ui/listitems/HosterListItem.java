package de.m4lik.burningseries.ui.listitems;

/**
 * Created by Malik on 16.06.2016.
 */
public class HosterListItem {

    private Integer linkid;
    private String hoster;
    private Integer part;
    private Boolean support;

    public HosterListItem (Integer linkid, String hoster, Integer part, Boolean support) {
        this.linkid = linkid;
        this.hoster = hoster;
        this.part = part;
        this.support = support;
    }

    public HosterListItem(Integer linkid, String hoster, Integer part) {
        this(linkid, hoster, part, false);
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

    public Boolean isSupported() {
        return support;
    }
}
