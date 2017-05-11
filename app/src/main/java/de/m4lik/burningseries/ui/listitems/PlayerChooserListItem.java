package de.m4lik.burningseries.ui.listitems;

import android.support.annotation.DrawableRes;

/**
 * Created by malik on 11.05.17.
 */

public class PlayerChooserListItem {

    private String lable;
    private String id;
    @DrawableRes private int icon;

    public PlayerChooserListItem(String lable, String id, int icon) {
        this.lable = lable;
        this.id = id;
        this.icon = icon;
    }

    public String getLable() {
        return lable;
    }

    public String getId() {
        return id;
    }

    public int getIcon() {
        return icon;
    }
}
