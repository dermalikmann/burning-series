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
import de.m4lik.burningseries.databinding.ListItemHosterBinding;
import de.m4lik.burningseries.ui.listitems.HosterListItem;
import de.m4lik.burningseries.util.Settings;

import static de.m4lik.burningseries.services.ThemeHelperService.theme;

/**
 * Created by malik on 09.05.17.
 */

public class HosterRecyclerAdapter extends RecyclerView.Adapter<HosterRecyclerAdapter.HosterViewHolder> {

    private Context context;
    private List<HosterListItem> list = new ArrayList<>();

    public HosterRecyclerAdapter(Context context, List<HosterListItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public HosterRecyclerAdapter.HosterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        ListItemHosterBinding binding = ListItemHosterBinding.inflate(layoutInflater, parent, false);
        return new HosterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(HosterViewHolder holder, int position) {
        HosterListItem current = list.get(position);
        holder.bind(current);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class HosterViewHolder extends RecyclerView.ViewHolder {

        ListItemHosterBinding binding;

        HosterViewHolder(ListItemHosterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(HosterListItem item) {
            binding.setHoster(item);

            View root = binding.getRoot();
            boolean isDark = Settings.of(context).isDarkTheme();

            root.findViewById(R.id.listItemContainer).setBackground(ContextCompat.getDrawable(context, theme().listItemBackground));

            if (isDark)
                ((TextView) root.findViewById(R.id.hosterLabel))
                        .setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));

            if (item.isSupported())
                ((ImageView) root.findViewById(R.id.supImgView))
                        .setImageDrawable(ContextCompat.getDrawable(context, isDark ?
                                R.drawable.ic_ondemand_video_white : R.drawable.ic_ondemand_video));
            else
                ((ImageView) root.findViewById(R.id.supImgView))
                        .setImageDrawable(ContextCompat.getDrawable(context, isDark ?
                                R.drawable.ic_public_white : R.drawable.ic_public));

            binding.executePendingBindings();
        }
    }
}