package de.m4lik.burningseries.ui.showFragments;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import de.m4lik.burningseries.R;
import de.m4lik.burningseries.ShowActivity;
import de.m4lik.burningseries.ui.listitems.SeasonListItem;

import static de.m4lik.burningseries.services.ThemeHelperService.theme;

public class SeasonsFragment extends Fragment {

    View rootview;

    ListView seasonsListView;
    ArrayList<SeasonListItem> seasonsList = new ArrayList<>();

    Boolean loaded = false;

    public SeasonsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_seasons, container, false);


        seasonsListView = (ListView) rootview.findViewById(R.id.seasonsListView);

        LinearLayout seasonscontainer = (LinearLayout) rootview.findViewById(R.id.seasonscontainer);
        seasonscontainer.setVisibility(View.VISIBLE);

        String description = ((ShowActivity) getActivity()).getDescription();
        Integer count = ((ShowActivity) getActivity()).getSeasonCount();
        Boolean withSpecials = ((ShowActivity) getActivity()).withSpecials();

        TextView descriptionView = (TextView) rootview.findViewById(R.id.descriptionTV);
        descriptionView.setText(description);
        for (int i = withSpecials ? 0 : 1 ; i <= count; i++) {
            seasonsList.add(new SeasonListItem(i));
        }

        if (!loaded) {
            refreshList();
            loaded = true;
        }

        return rootview;
    }


    private void refreshList() {
        ArrayAdapter<SeasonListItem> adapter = new seasonsListAdapter();
        seasonsListView.setAdapter(adapter);

        Integer numOfItems = adapter.getCount();

        Integer totalItemsHeigt = 0;
        for (int pos = 0; pos < numOfItems; pos++) {
            View item = adapter.getView(pos, null, seasonsListView);
            item.measure(0, 0);
            totalItemsHeigt += item.getMeasuredHeight();
        }

        Integer totalDividersHeight = seasonsListView.getDividerHeight() * (numOfItems - 1);
        ViewGroup.LayoutParams params = seasonsListView.getLayoutParams();
        params.height = totalItemsHeigt + totalDividersHeight;
        seasonsListView.setLayoutParams(params);
        seasonsListView.requestLayout();

        seasonsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView idView = (TextView) view.findViewById(R.id.seasonId);
                showSeason(Integer.parseInt(idView.getText().toString()));
            }
        });
    }

    class seasonsListAdapter extends ArrayAdapter<SeasonListItem> {

        public seasonsListAdapter() {
            super(getActivity().getApplicationContext(), R.layout.list_item_seasons, seasonsList);
        }

        @Override
        public View getView(int pos, View view, ViewGroup parent) {
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.list_item_seasons, parent, false);
            }

            view.findViewById(R.id.listItemContainer).setBackground(getResources().getDrawable(theme().listItemBackground));

            SeasonListItem current = seasonsList.get(pos);

            TextView label = (TextView) view.findViewById(R.id.seasonLabel);
            if (current.getSeasonId() == 0)
                label.setText("Filme/Specials");
            else
                label.setText(getString(R.string.season) + current.getSeasonId());

            TextView urlText = (TextView) view.findViewById(R.id.seasonId);
            urlText.setText(current.getSeasonId().toString());

            return view;
        }
    }

    private void showSeason(Integer id) {
        ((ShowActivity) getActivity()).setSelectedSeason(id);
        ((ShowActivity) getActivity()).switchSeasonsToEpisodes();
    }
}
