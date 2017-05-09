package de.m4lik.burningseries.ui.listitems;

/**
 * Created by Malik on 12.06.2016.
 * List item for history fragment
 */
public class HistoryListItem {

    private Integer showId;
    private Integer seasonId;
    private Integer episodeId;
    private String showName;
    private String episodeName;

    public HistoryListItem(Integer showId, Integer seasonId, Integer episodeId, String showName, String episodeName) {
        this.showId = showId;
        this.seasonId = seasonId;
        this.episodeId = episodeId;
        this.showName = showName;
        this.episodeName = episodeName;
    }

    public Integer getShowId() {
        return showId;
    }

    public Integer getSeasonId() {
        return seasonId;
    }

    public Integer getEpisodeId() {
        return episodeId;
    }

    public String getShowName() {
        return showName;
    }

    public String getEpisodeName() {
        return episodeName;
    }
}
