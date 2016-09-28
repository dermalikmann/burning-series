package de.squarecoding.burningseries.mainFragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import de.squarecoding.burningseries.MainActivity;
import de.squarecoding.burningseries.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavsFragment extends Fragment {


    public FavsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_series, container, false);

        MenuItem menuItem = MainActivity.menu.findItem(R.id.action_search);
        menuItem.setVisible(false);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(null);

        return rootView;
    }

}

