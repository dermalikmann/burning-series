package de.monarchcode.m4lik.burningseries.objects;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Malik on 01.10.2016.
 */

public class EpisodeObj {

    private String series;
    @SerializedName("epi")
    private Episode episode;
    @SerializedName("links")
    private Hoster[] hoster;

    public EpisodeObj(String series, Episode episode, Hoster[] hoster) {
        this.series = series;
        this.episode = episode;
        this.hoster = hoster;
    }

    public String getSeries() {
        return series;
    }

    public Episode getEpisode() {
        return episode;
    }

    public Hoster[] getHoster() {
        return hoster;
    }

    public class Episode {

        private String german;
        private String english;
        private String description;
        private Integer id;

        public Episode(String german, String english, String description, Integer id) {
            this.german = german;
            this.english = english;
            this.description = description;
            this.id = id;
        }

        public String getGerman() {
            return german;
        }

        public String getEnglish() {
            return english;
        }

        public String getDescription() {
            return description;
        }

        public Integer getEpisodeId() {
            return id;
        }
    }

    public class Hoster {

        private String hoster;
        private Integer part;
        private Integer id;

        public Hoster(String hoster, Integer part, Integer id) {
            this.hoster = hoster;
            this.part = part;
            this.id = id;
        }

        public String getHoster() {
            return hoster;
        }

        public Integer getPart() {
            return part;
        }

        public Integer getLinkId() {
            return id;
        }
    }

}
