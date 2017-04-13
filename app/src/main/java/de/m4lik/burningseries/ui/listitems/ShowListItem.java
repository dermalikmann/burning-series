package de.m4lik.burningseries.ui.listitems;

/**
 * Created by Malik on 12.06.2016.
 */
public class ShowListItem {

    private Integer id;
    public Boolean loaded = false;
    private String title;
    private String genre;
    private boolean fav;

    public ShowListItem(String title, Integer id, String genre, boolean fav) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.fav = fav;
    }

    public int compareTo(ShowListItem item) {
        return this.getTitle().compareTo(item.getTitle());
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public boolean isFav() {
        return fav;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShowListItem that = (ShowListItem) o;

        if (fav != that.fav) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        return genre != null ? genre.equals(that.genre) : that.genre == null;

    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (genre != null ? genre.hashCode() : 0);
        result = 31 * result + (fav ? 1 : 0);
        return result;
    }
}
