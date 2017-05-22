package de.m4lik.burningseries.ui.viewAdapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

import de.m4lik.burningseries.R;
import de.m4lik.burningseries.databinding.ListItemSeriesBinding;
import de.m4lik.burningseries.ui.listitems.ShowListItem;
import de.m4lik.burningseries.util.Settings;

import static de.m4lik.burningseries.services.ThemeHelperService.theme;

/**
 * Created by Malik on 09.05.2017
 *
 * @author Malik Mann
 */

public class SortedSeriesRecyclerAdapter extends RecyclerView.Adapter<SortedSeriesRecyclerAdapter.SeriesViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private final SortedList<ShowListItem> showListItemSortedList = new SortedList<ShowListItem>(ShowListItem.class, new SortedList.Callback<ShowListItem>() {

        @Override
        public void onInserted(int position, int count) {
            notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemRangeChanged(position, count);
        }

        @Override
        public int compare(ShowListItem o1, ShowListItem o2) {
            return o1.compareTo(o2);
        }

        @Override
        public boolean areContentsTheSame(ShowListItem oldItem, ShowListItem newItem) {
            return oldItem.getTitle().equals(newItem.getTitle());
        }

        @Override
        public boolean areItemsTheSame(ShowListItem item1, ShowListItem item2) {
            return item1.getId().equals(item2.getId());
        }
    });
    private Context context;

    public SortedSeriesRecyclerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public SortedSeriesRecyclerAdapter.SeriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        ListItemSeriesBinding binding = ListItemSeriesBinding.inflate(layoutInflater, parent, false);
        return new SeriesViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(SortedSeriesRecyclerAdapter.SeriesViewHolder holder, int position) {
        ShowListItem current = showListItemSortedList.get(position);
        holder.bind(current);
    }

    @Override
    public int getItemCount() {
        return showListItemSortedList.size();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return String.valueOf(showListItemSortedList.get(position).getTitle().charAt(0));
    }

    public void add(ShowListItem model) {
        showListItemSortedList.add(model);
    }

    public void remove(ShowListItem model) {
        showListItemSortedList.remove(model);
    }

    public void add(List<ShowListItem> models) {
        showListItemSortedList.addAll(models);
    }

    public void remove(List<ShowListItem> models) {
        showListItemSortedList.beginBatchedUpdates();
        models.forEach(showListItemSortedList::remove);
        showListItemSortedList.endBatchedUpdates();
    }

    public void replaceAll(List<ShowListItem> models) {
        showListItemSortedList.beginBatchedUpdates();
        for (int i = showListItemSortedList.size() - 1; i >= 0; i--) {
            final ShowListItem model = showListItemSortedList.get(i);
            if (!models.contains(model)) {
                showListItemSortedList.remove(model);
            }
        }
        showListItemSortedList.addAll(models);
        showListItemSortedList.endBatchedUpdates();
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
            if (!Settings.of(context).isDarkTheme())
                ((ImageView) binding.getRoot().findViewById(R.id.favImageView))
                        .setImageDrawable(ContextCompat.getDrawable(context,
                                item.isFav() ? R.drawable.ic_star : R.drawable.ic_star_border));
            else
                ((ImageView) binding.getRoot().findViewById(R.id.favImageView))
                        .setImageDrawable(ContextCompat.getDrawable(context,
                                item.isFav() ? R.drawable.ic_star_white : R.drawable.ic_star_border_white));
            binding.executePendingBindings();
        }
    }
}