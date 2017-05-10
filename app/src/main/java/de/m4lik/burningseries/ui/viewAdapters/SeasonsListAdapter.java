package de.m4lik.burningseries.ui.viewAdapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import de.m4lik.burningseries.R;
import de.m4lik.burningseries.ui.listitems.SeasonListItem;

import static de.m4lik.burningseries.services.ThemeHelperService.theme;

/**
 * Created by malik on 09.05.17.
 */

public class SeasonsListAdapter extends ArrayAdapter<SeasonListItem> {

    private Context context;
    private List<SeasonListItem> seasons;

    public SeasonsListAdapter(Context context, List<SeasonListItem> seasons) {
        super(context, R.layout.list_item_seasons, seasons);
        this.context = context;
        this.seasons = seasons;
    }

    @NonNull
    @Override
    public View getView(int pos, View view, @NonNull ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_seasons, parent, false);
        }

        view.findViewById(R.id.listItemContainer).setBackground(ContextCompat.getDrawable(context, theme().listItemBackground));

        SeasonListItem current = seasons.get(pos);

        TextView label = (TextView) view.findViewById(R.id.seasonLabel);
        label.setText(context.getText(R.string.season).toString() + current.getSeasonId().toString());

        TextView urlText = (TextView) view.findViewById(R.id.seasonId);
        urlText.setText(current.getSeasonId().toString());

        return view;
    }
}