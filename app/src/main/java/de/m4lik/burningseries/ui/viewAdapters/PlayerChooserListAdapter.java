package de.m4lik.burningseries.ui.viewAdapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.m4lik.burningseries.R;
import de.m4lik.burningseries.ui.listitems.PlayerChooserListItem;

/**
 * Created by malik on 09.05.17.
 */

public class PlayerChooserListAdapter extends ArrayAdapter<PlayerChooserListItem> {

    private Context context;
    private List<PlayerChooserListItem> players;

    public PlayerChooserListAdapter(Context context, List<PlayerChooserListItem> players) {
        super(context, R.layout.list_item_player, players);
        this.context = context;
        this.players = players;
    }

    @NonNull
    @Override
    public View getView(int pos, View view, @NonNull ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_player, parent, false);
        }

        PlayerChooserListItem current = players.get(pos);

        ((TextView) view.findViewById(R.id.playerLable)).setText(current.getLable());
        ((TextView) view.findViewById(R.id.playerType)).setText(current.getType());
        ((ImageView) view.findViewById(R.id.playerIcon)).setImageDrawable(ContextCompat.getDrawable(context, current.getIcon()));

        return view;
    }
}