package de.m4lik.burningseries.ui.viewAdapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import de.m4lik.burningseries.R;
import de.m4lik.burningseries.databinding.ListItemGenresBinding;
import de.m4lik.burningseries.ui.listitems.GenreListItem;

import static de.m4lik.burningseries.services.ThemeHelperService.theme;

/**
 * Created by Malik on 10.05.2017
 *
 * @author Malik Mann
 */
public class GenresRecyclerAdapter extends RecyclerView.Adapter<GenresRecyclerAdapter.GenresViewHolder> {

    private List<GenreListItem> list;
    private Context context;

    public GenresRecyclerAdapter(Context context, List<GenreListItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public GenresRecyclerAdapter.GenresViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        ListItemGenresBinding binding = ListItemGenresBinding.inflate(layoutInflater, parent, false);
        return new GenresViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(GenresRecyclerAdapter.GenresViewHolder holder, int position) {
        GenreListItem current = list.get(position);
        holder.bind(current);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class GenresViewHolder extends RecyclerView.ViewHolder {

        private final ListItemGenresBinding binding;

        GenresViewHolder(ListItemGenresBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(GenreListItem item) {
            binding.setGenre(item);
            binding.getRoot().findViewById(R.id.listItemContainer).setBackground(ContextCompat.getDrawable(context, theme().listItemBackground));
            binding.executePendingBindings();
        }
    }
}