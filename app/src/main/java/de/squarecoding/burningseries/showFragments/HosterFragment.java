package de.squarecoding.burningseries.showFragments;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import de.squarecoding.burningseries.R;
import de.squarecoding.burningseries.ShowActivity;
import de.squarecoding.burningseries.objects.episodesListItem;
import de.squarecoding.burningseries.objects.hosterListItem;

/**
 * A simple {@link Fragment} subclass.
 */
public class HosterFragment extends Fragment {

    View rootview;

    Document webDoc;
    Document hosterWebDoc;

    String url;

    ListView hostersListView;
    ArrayList<hosterListItem> hostersList = new ArrayList<>();


    public HosterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootview =  inflater.inflate(R.layout.fragment_hoster, container, false);

        url = ((ShowActivity) getActivity()).getSelectedEpisode();

        hostersListView = (ListView) rootview.findViewById(R.id.hosterListView);

        new getHosters(url).execute();

        return rootview;
    }



    private class getHosters extends AsyncTask<Void, Void, Void> {

        private String url;
        private String debug;

        public getHosters(String url) {
            this.url = url;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();/*
            progressDialog = new ProgressDialog(HostersActivity.this);
            progressDialog.setMessage("Folgen werden aufgelistet.\nBitte warten...");

            progressDialog.show();
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);*/

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {

                webDoc = Jsoup.connect(url).get();

                webDoc.select("ul.pages").remove();

                String url, lable;

                Elements hosterList = webDoc.select("#sp_left ul li");

                for (Element hoster : hosterList) {
                    Element link = hoster.select("a").first();

                    url = link.attr("abs:href").toString();
                    String tmpLable = link.text();
                    if (tmpLable.toLowerCase().contains("vivo")) {
                        lable = "Vivo";
                    } else if (tmpLable.toLowerCase().contains("openload")) {
                        lable = "OpenLoad";
                    } else if (tmpLable.toLowerCase().contains("flashx")) {
                        lable = "FlashX";
                    } else if (tmpLable.toLowerCase().contains("powerwatch")) {
                        lable = "Powerwatch";
                    } else if (tmpLable.toLowerCase().contains("cloudtime")) {
                        lable = "Cloudtime";
                    } else if (tmpLable.toLowerCase().contains("shared")) {
                        lable = "Shared";
                    } else if (tmpLable.toLowerCase().contains("streamcloud")) {
                        lable = "Streamcloud";
                    } else if (tmpLable.toLowerCase().contains("wholecloud")) {
                        lable = "WholeCloud";
                    } else if (tmpLable.toLowerCase().contains("youwatch")) {
                        lable = "YouWatch";
                    } else {
                        lable = "Other";
                    }

                    hostersList.add(new hosterListItem(url, lable));
                }


            } catch (IOException e) {
                Log.d("JSOUP", "Can't connect:", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //TextView dbgTv = (TextView) findViewById(R.id.debug);
            //dbgTv.setText(debug);

            populateList();
            //progressDialog.dismiss();
        }
    }



    private void populateList() {
        ArrayAdapter<hosterListItem> adapter = new hostersListAdapter();
        hostersListView.setAdapter(adapter);

        Integer numOfItems = adapter.getCount();

        Integer totalItemsHeigt = 0;
        for (int pos = 0; pos < numOfItems; pos++) {
            View item = adapter.getView(pos, null, hostersListView);
            item.measure(0, 0);
            totalItemsHeigt += item.getMeasuredHeight();
        }

        Integer totalDividersHeight = hostersListView.getDividerHeight() * (numOfItems - 1);
        ViewGroup.LayoutParams params = hostersListView.getLayoutParams();
        params.height = totalItemsHeigt + totalDividersHeight;
        hostersListView.setLayoutParams(params);
        hostersListView.requestLayout();

        hostersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView urlView = (TextView) view.findViewById(R.id.hosterUrl);
                showHoster(urlView.getText().toString());
            }
        });
    }

    private void showHoster(String url) {
        ((ShowActivity) getActivity()).setSelectedHoster(url);

        new getVideo(url).execute();
    }

    class hostersListAdapter extends ArrayAdapter<hosterListItem> {

        public hostersListAdapter() {
            super(getActivity(), R.layout.list_item_hoster, hostersList);
        }

        @Override
        public View getView(int pos, View view, ViewGroup parent) {
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.list_item_hoster, parent, false);
            }

            hosterListItem current = hostersList.get(pos);

            TextView lable = (TextView) view.findViewById(R.id.hosterLabel);
            lable.setText(current.getHosterLable());

            TextView url = (TextView) view.findViewById(R.id.hosterUrl);
            url.setText(current.getUrl());


            return view;
        }
    }


    /** VIDEO **/

    class getVideo extends AsyncTask<Void, Void, Void> {

        private String url;
        private Document webDoc;
        private Boolean failed = false;

        public getVideo(String url) {
            this.url = url;
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                hosterWebDoc = Jsoup.connect(url).get();

                url = hosterWebDoc.select("#video_actions div a").attr("href").toString();

                try {

                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(getActivity(), Uri.parse(url));

                } catch (ActivityNotFoundException e) {
                    failed = true;
                }

            } catch (IOException e) {
                Log.d("JSOUP", "Can't connect:", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (failed) {
                Snackbar.make(rootview.findViewById(R.id.hosterListView), "Da ist was schiefgelaufen. Bitte versuche einen anderen Hoster", Snackbar.LENGTH_LONG).show();
            }
        }
    }

}
