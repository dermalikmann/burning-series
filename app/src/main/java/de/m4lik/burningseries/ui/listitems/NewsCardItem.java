package de.m4lik.burningseries.ui.listitems;

/**
 * Created by Malik on 20.02.2017
 *
 * @author Malik Mann
 */

public class NewsCardItem {

    private String title;
    private String date;
    private String content;
    private Integer id;

    public NewsCardItem(Integer id, String title, String date, String content) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.content = content;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }
}
