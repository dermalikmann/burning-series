package de.m4lik.burningseries.ui.viewAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.m4lik.burningseries.R;
import de.m4lik.burningseries.ui.listitems.StatsListItem;

/**
 * Created by Malik on 10.05.2017
 *
 * @author Malik Mann
 */

public class StatsListAdapter extends ArrayAdapter<StatsListItem> {

    List<StatsListItem> statsList = new ArrayList<>();
    Context context;

    public StatsListAdapter(Context context, List<StatsListItem> statsList) {
        super(context, R.layout.list_item_stats, statsList);
        this.statsList = statsList;
        this.context = context;
    }

    @Override
    public View getView(int pos, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_stats, parent, false);
        }

        StatsListItem current = statsList.get(pos);

        ((TextView) view.findViewById(R.id.statsKey)).setText(current.getKey());
        ((TextView) view.findViewById(R.id.statsValue)).setText(current.getValue());

        return view;
    }
}