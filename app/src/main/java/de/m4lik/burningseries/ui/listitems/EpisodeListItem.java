package de.m4lik.burningseries.ui.listitems;

/**
 * Created by Malik on 12.06.2016.
 */
public class EpisodeListItem {

    private String titleGer;
    private String title;
    private Integer id;
    private Boolean watched;

    public EpisodeListItem(String titleGer, String title, Integer id, Boolean watched) {
        this.titleGer = titleGer;
        this.title = title;
        this.id = id;
        this.watched = watched;
    }

    public String getTitleGer() {
        return titleGer;
    }

    public String getTitle() {
        return title;
    }

    public Integer getId() {
        return id;
    }

    public Boolean isWatched() {
        return watched;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EpisodeListItem that = (EpisodeListItem) o;

        return id != null ? id.equals(that.id) : that.id == null;

    }

    public int compareTo(EpisodeListItem item) {
        return this.getId().compareTo(item.getId());
    }

    @Override
    public int hashCode() {
        int result = titleGer != null ? titleGer.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (watched != null ? watched.hashCode() : 0);
        return result;
    }
}
