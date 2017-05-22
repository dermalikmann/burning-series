package de.m4lik.burningseries.util;

import android.content.Context;

import de.m4lik.burningseries.api.APIUtils;
import de.m4lik.burningseries.database.DatabaseUtils;

/**
 * Created by malik on 09.05.17.
 */

public class ShowUtils {

    public static boolean isFav(Context context, int showID) {
        return DatabaseUtils.with(context)
                .getFavorites()
                .contains(showID);
    }

    public static void addToFavorites(Context context, int showID) {
        DatabaseUtils.with(context)
                .addToFavorites(showID);

        if (!Settings.of(context).getUserSession().equals(""))
            APIUtils.with(context)
                    .sendFavorites(DatabaseUtils.with(context)
                            .getFavorites());
    }

    public static void removeFromFavorites(Context context, int showID) {
        DatabaseUtils.with(context)
                .removeFromFavorites(showID);

        if (!Settings.of(context).getUserSession().equals(""))
            APIUtils.with(context)
                    .sendFavorites(DatabaseUtils.with(context)
                            .getFavorites());
    }
}
