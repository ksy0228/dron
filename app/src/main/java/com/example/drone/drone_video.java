package com.example.drone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class drone_video extends AppCompatActivity {
    private String API_KEY = "AIzaSyDaWKr2m1ffRLW8e2SA_n786p6wZOaJyU0";
    private ListView listView;
    private int timer = 0;
    private String[] video_id = new String[10];
    MyAdapter myAdapter = new MyAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drone_video);

        listView = findViewById(R.id.listView);

        YoutubeAsyncTask youtubeAsyncTask = new YoutubeAsyncTask();
        youtubeAsyncTask.execute();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {

                String url = "https://www.youtube.com/watch?v=" + video_id[position];
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);

            }
        }) ;

    }

    private class YoutubeAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
                final JacksonFactory JSON_FACTORY = new JacksonFactory();
                final long NUMBER_OF_VIDEOS_RETURNED = 10;

                YouTube youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
                    public void initialize(HttpRequest request) throws IOException {
                    }
                }).setApplicationName("youtube-search-sample").build();

                YouTube.Search.List search = youtube.search().list("id,snippet");

                search.setKey(API_KEY);

                search.setQ("드론조종법");
                search.setOrder("relevance"); //date relevance

                search.setType("video");

                search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
                search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
                SearchListResponse searchResponse = search.execute();

                List<SearchResult> searchResultList = searchResponse.getItems();

                if (searchResultList != null) {
                    prettyPrint(searchResultList.iterator(), "드론");
                }
            } catch (GoogleJsonResponseException e) {
                System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                        + e.getDetails().getMessage());
                System.err.println("There was a service error 2: " + e.getLocalizedMessage() + " , " + e.toString());
            } catch (IOException e) {
                System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
            } catch (Throwable t) {
                t.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            listView.setAdapter(myAdapter);
        }

        public void prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query) {
            if (!iteratorSearchResults.hasNext()) {
                System.out.println(" There aren't any results for your query.");
            }

            String cnt;

            while (iteratorSearchResults.hasNext()) {
                SearchResult singleVideo = iteratorSearchResults.next();
                ResourceId rId = singleVideo.getId();

                // Double checks the kind is video.
                if (rId.getKind().equals("youtube#video")) {
                    Thumbnail thumbnail = (Thumbnail) singleVideo.getSnippet().getThumbnails().get("default");
                    cnt = singleVideo.getSnippet().getTitle().trim();
                    video_id[timer] = rId.getVideoId();
                    myAdapter.addItem(cnt);
                    timer++;
                }
            }

        }
    }
}