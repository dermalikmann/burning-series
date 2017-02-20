package de.m4lik.burningseries.api.objects;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Malik on 01.10.2016.
 */

public class FullShowObj {

    private Integer id;
    private String series;
    private String description;
    private Integer movies;
    private Integer seasons;
    private Integer start;
    private Integer end;
    private Data data;

    public FullShowObj(Integer id, String series, String description, Integer movies, Integer seasons, Integer start, Integer end, Data data) {
        this.id = id;
        this.series = series;
        this.description = description;
        this.movies = movies;
        this.seasons = seasons;
        this.start = start;
        this.end = end;
        this.data = data;
    }

    public Integer getId() {
        return id;
    }

    public String getSeriesName() {
        return series;
    }

    public String getDescription() {
        return description;
    }

    public Integer getMovieCount() {
        return movies;
    }

    public Integer getSeasonCount() {
        return seasons;
    }

    public Integer getStartYear() {
        return start;
    }

    public Integer getEndYear() {
        return end;
    }

    public Data getData() {
        return data;
    }

    public class Data {
        private String[] producer;
        private String[] author;
        private String[] genre;
        private String[] director;
        @SerializedName("genre_main")
        private String mainGenre;

        public Data(String[] producer, String[] author, String[] genre, String[] director, String mainGenre) {
            this.producer = producer;
            this.author = author;
            this.genre = genre;
            this.director = director;
            this.mainGenre = mainGenre;
        }

        public String[] getProducer() {
            return producer;
        }

        public String[] getAuthor() {
            return author;
        }

        public String[] getGenre() {
            return genre;
        }

        public String[] getDirector() {
            return director;
        }

        public String getMainGenre() {
            return mainGenre;
        }
    }
}
