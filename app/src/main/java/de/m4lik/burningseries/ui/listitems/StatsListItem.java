package de.m4lik.burningseries.ui.listitems;

/**
 * Created by Malik on 16.06.2016.
 */
public class StatsListItem {

    private String key;

    private String value;

    public StatsListItem(String key, String value) {

        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
