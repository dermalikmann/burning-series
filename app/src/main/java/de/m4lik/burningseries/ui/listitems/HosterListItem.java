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

    @Override
    public int hashCode() {
        int result = linkid != null ? linkid.hashCode() : 0;
        result = 31 * result + (hoster != null ? hoster.hashCode() : 0);
        result = 31 * result + (part != null ? part.hashCode() : 0);
        return result;
    }

    public int compareTo(HosterListItem item) {
        int compare = this.isSupported().compareTo(item.isSupported());
        if (compare == 0) {
            compare = this.getHoster().compareTo(item.getHoster());
        }
        return compare;
    }
}