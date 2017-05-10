package de.m4lik.burningseries.ui.viewAdapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.m4lik.burningseries.R;
import de.m4lik.burningseries.databinding.ListItemEpisodesBinding;
import de.m4lik.burningseries.ui.listitems.EpisodeListItem;
import de.m4lik.burningseries.util.Settings;

import static de.m4lik.burningseries.services.ThemeHelperService.theme;

/**
 * Created by malik on 09.05.17.
 */

public class EpisodesRecyclerAdapter extends RecyclerView.Adapter<EpisodesRecyclerAdapter.EpisodesViewHolder> {

    private Context context;
    private List<EpisodeListItem> list = new ArrayList<>();

    public EpisodesRecyclerAdapter(Context context, List<EpisodeListItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public EpisodesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        ListItemEpisodesBinding binding = ListItemEpisodesBinding.inflate(layoutInflater, parent, false);
        return new EpisodesViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(EpisodesViewHolder holder, int position) {
        EpisodeListItem current = list.get(position);
        holder.bind(current);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class EpisodesViewHolder extends RecyclerView.ViewHolder {

        ListItemEpisodesBinding binding;

        EpisodesViewHolder(ListItemEpisodesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(EpisodeListItem item) {
            binding.setEpisode(item);

            View root = binding.getRoot();

            boolean isDark = Settings.of(context).isDarkTheme();

            root.findViewById(R.id.listItemContainer)
                    .setBackground(ContextCompat.getDrawable(context, theme().listItemBackground));

            if (!isDark) {
                ((TextView) root.findViewById(R.id.episodeTitleGer))
                        .setTextColor(ContextCompat.getColor(context, item.isWatched() ?
                                android.R.color.darker_gray : android.R.color.black));
            } else {
                ((TextView) root.findViewById(R.id.episodeTitleGer))
                        .setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
                ((TextView) root.findViewById(R.id.episodeTitle))
                        .setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
            }


            if (item.isWatched())
                ((ImageView) root.findViewById(R.id.watchedImageView))
                        .setImageDrawable(ContextCompat.getDrawable(context, isDark ?
                                R.drawable.ic_watched_white : R.drawable.ic_watched));
            else
                ((ImageView) root.findViewById(R.id.watchedImageView))
                        .setImageDrawable(null);

            binding.executePendingBindings();
        }
    }
}