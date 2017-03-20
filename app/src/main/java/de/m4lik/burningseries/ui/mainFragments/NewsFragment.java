package de.m4lik.burningseries.ui.mainFragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import de.m4lik.burningseries.R;
import de.m4lik.burningseries.ui.listitems.NewsCardItem;


public class NewsFragment extends Fragment {

    //@BindView(R.id.newsRecyclerView)
    RecyclerView newsRecyclerView;

    List<NewsCardItem> newsItems = new ArrayList<>();

    public NewsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news, container, false);

        //ButterKnife.bind(rootView);
        newsRecyclerView = (RecyclerView) rootView.findViewById(R.id.newsRecyclerView);

        newsRecyclerView.setHasFixedSize(true);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        new News().fetch();

        return  rootView;
    }


    private class News extends AsyncTask<Void, Void, Void> {

        void fetch() {
            this.execute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Document document = Jsoup.connect("https://bs.to/news/").get();

                Elements elements = document.select(".news ul li");
                for (Element element : elements) {
                    String date = element.select("time").first().text().split(":")[0];
                    String title = element.select("a").text();
                    Integer id = Integer.parseInt(element.select("a").attr("href").split("news/")[1]);

                    Document newsDoc = Jsoup.connect("https://bs.to/news/" + id).get();

                    String content = newsDoc.select(".news p").first().text().trim();

                    if (!title.toLowerCase().contains("song of the day"))
                        newsItems.add(new NewsCardItem(id, title, date, content));
                }

            } catch (Exception ignore) {

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            newsRecyclerView.setAdapter(new CardViewAdapter());
        }
    }

    class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.NewsViewHolder> {

        public class NewsViewHolder extends RecyclerView.ViewHolder {

            CardView cardView;
            TextView newsTitle;
            TextView newsDate;
            TextView newsContent;

            NewsViewHolder(View itemView) {
                super(itemView);
                newsTitle = (TextView) itemView.findViewById(R.id.newsTitle);
                newsDate = (TextView) itemView.findViewById(R.id.newsDate);
                newsContent = (TextView) itemView.findViewById(R.id.newsContent);
            }
        }

        @Override
        public int getItemCount() {
            return newsItems.size();
        }

        @Override
        public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item_news, parent, false);
            return new NewsViewHolder(v);
        }

        @Override
        public void onBindViewHolder(NewsViewHolder holder, int position) {
            holder.newsTitle.setText(newsItems.get(position).getTitle());
            holder.newsDate.setText(newsItems.get(position).getDate());
            holder.newsContent.setText(newsItems.get(position).getContent());
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }
    }
}
