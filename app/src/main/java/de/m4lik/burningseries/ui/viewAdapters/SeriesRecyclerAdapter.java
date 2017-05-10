package de.m4lik.burningseries.ui.viewAdapters;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.m4lik.burningseries.R;
import de.m4lik.burningseries.databinding.ListItemSeriesBinding;
import de.m4lik.burningseries.ui.listitems.ShowListItem;
import de.m4lik.burningseries.util.Settings;

import static de.m4lik.burningseries.services.ThemeHelperService.theme;

/**
 * Created by Malik on 10.05.2017
 *
 * @author Malik Mann
 */
public class SeriesRecyclerAdapter extends RecyclerView.Adapter<SeriesRecyclerAdapter.SeriesViewHolder> {

    private List<ShowListItem> list;
    private Context context;
    
    public SeriesRecyclerAdapter(Context context, List<ShowListItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public SeriesRecyclerAdapter.SeriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        ListItemSeriesBinding binding = ListItemSeriesBinding.inflate(layoutInflater, parent, false);
        return new SeriesViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(SeriesRecyclerAdapter.SeriesViewHolder holder, int position) {
        ShowListItem current = list.get(position);
        holder.bind(current);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class SeriesViewHolder extends RecyclerView.ViewHolder {

        private final ListItemSeriesBinding binding;

        SeriesViewHolder(ListItemSeriesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ShowListItem item) {
            binding.setShow(item);
            binding.getRoot().findViewById(R.id.listItemContainer).setBackground(ContextCompat.getDrawable(context, theme().listItemBackground));

            if (!item.loaded && Settings.of(context).showCovers())
                Glide.with(context)
                        .load(Uri.parse("https://bs.to/public/img/cover/" + item.getId() + ".jpg"))
                        .into((ImageView) binding.getRoot().findViewById(R.id.coverImage));

            if (!Settings.of(context).showCovers()) {
                binding.getRoot().findViewById(R.id.coverImage).setVisibility(View.GONE);
            }

            item.loaded = true;

            binding.getRoot().findViewById(R.id.favImageView).setVisibility(View.GONE);

            binding.executePendingBindings();
        }
    }
}