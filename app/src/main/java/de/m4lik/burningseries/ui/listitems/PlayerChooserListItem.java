package de.m4lik.burningseries.ui.listitems;

import android.support.annotation.DrawableRes;

/**
 * Created by malik on 11.05.17.
 */

public class PlayerChooserListItem {

    private String lable;
    private String type;
    @DrawableRes private int icon;

    public PlayerChooserListItem(String lable, String type, int icon) {
        this.lable = lable;
        this.type = type;
        this.icon = icon;
    }

    public String getLable() {
        return lable;
    }

    public String getType() {
        return type;
    }

    public int getIcon() {
        return icon;
    }
}
