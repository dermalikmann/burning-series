package de.m4lik.burningseries.ui.showFragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.m4lik.burningseries.R;
import de.m4lik.burningseries.api.API;
import de.m4lik.burningseries.api.APIInterface;
import de.m4lik.burningseries.api.objects.SeasonObj;
import de.m4lik.burningseries.ui.ShowActivity;
import de.m4lik.burningseries.ui.listitems.SeasonListItem;
import de.m4lik.burningseries.ui.viewAdapters.SeasonsListAdapter;
import de.m4lik.burningseries.util.Settings;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeasonsFragment extends Fragment implements Callback<SeasonObj> {

    View rootView;

    @BindView(R.id.seasonsListView)
    ListView seasonsListView;

    @BindView(R.id.descriptionTV)
    TextView descriptionView;

    List<SeasonListItem> seasonsList = new ArrayList<>();

    Boolean loaded = false;

    public SeasonsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_seasons, container, false);
        ButterKnife.bind(this, rootView);

        seasonsListView = (ListView) rootView.findViewById(R.id.seasonsListView);

        LinearLayout seasonsContainer = (LinearLayout) rootView.findViewById(R.id.seasonscontainer);
        seasonsContainer.setVisibility(View.VISIBLE);

        Integer selectedShow = ((ShowActivity) getActivity()).getSelectedShow();

        String userSession = Settings.of(getActivity().getApplicationContext())
                .getUserSession();

        API api = new API();
        api.setSession(userSession);
        api.generateToken("series/" + selectedShow + "/1");
        APIInterface apii = api.getInterface();
        Call<SeasonObj> call = apii.getSeason(api.getToken(), api.getUserAgent(), selectedShow, 1, api.getSession());
        call.enqueue(this);

        return rootView;
    }

    @Override
    public void onResponse(Call<SeasonObj> call, Response<SeasonObj> response) {
        SeasonObj show = response.body();

        descriptionView.setText(show.getSeries().getDescription());
        Integer seasonCount = show.getSeries().getSeasonCount();
        Boolean withSpecials = show.getSeries().getMovieCount() != 0;

        for (int i = withSpecials ? 0 : 1; i <= seasonCount; i++) {
            seasonsList.add(new SeasonListItem(i));
        }

        if (!loaded) {
            refreshList();
            loaded = true;
        }
    }

    @Override
    public void onFailure(Call<SeasonObj> call, Throwable t) {

        Snackbar.make(rootView.findViewById(android.R.id.content), "Fehler beim Laden der Seriendetails", Snackbar.LENGTH_SHORT);
    }


    private void refreshList() {
        ArrayAdapter<SeasonListItem> adapter = new SeasonsListAdapter(getActivity(), seasonsList);
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

        seasonsListView.setOnItemClickListener((parent, view, position, id) -> {
            TextView idView = (TextView) view.findViewById(R.id.seasonId);
            showSeason(Integer.parseInt(idView.getText().toString()));
        });
    }

    private void showSeason(Integer id) {
        ((ShowActivity) getActivity()).setSelectedSeason(id);
        ((ShowActivity) getActivity()).switchSeasonsToEpisodes();
    }
}
