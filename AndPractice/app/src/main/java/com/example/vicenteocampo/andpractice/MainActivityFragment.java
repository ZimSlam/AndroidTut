package com.example.vicenteocampo.andpractice;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.vicenteocampo.andpractice.data.MovieContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    ImageAdapter gridAdapter;
    String source;
    String apiKey;


    public MainActivityFragment() {

    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("url",source);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // xml contains fragment with grid
        setHasOptionsMenu(true);
        apiKey = "" ;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml

            switch(item.getItemId()){
                case R.id.sort_popular:
                    source = "http://api.themoviedb.org/3/discover/movie?sort_" +
                    "by=popularity.desc&api_key=" + apiKey;
                    new FetchMoviesTask(getActivity(),gridAdapter).execute(source);
                    return true;
                case R.id.sort_top:
                    source = "http://api.themoviedb.org/3/discover/movie?sort_" +
                            "by=vote_average.desc&api_key=" + apiKey;
                    new FetchMoviesTask(getActivity(),gridAdapter).execute(source);
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
    }


    // Inflate our layout and attach adapter to GridView
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        final GridView gridview = (GridView) rootView.findViewById(R.id.grid);
        //Adapter places images into the Gridview object

        source = "http://api.themoviedb.org/3/discover/movie?sort_" +
                "by=popularity.desc&api_key=" + apiKey;
        if(savedInstanceState != null)
            source = savedInstanceState.getString("url");

            gridAdapter = new ImageAdapter(getActivity());
            gridview.setAdapter(gridAdapter);


            //Query for data, API key is omitted on public repo
            // Source: https://www.themoviedb.org/documentation/api?language=en
            new FetchMoviesTask(getActivity(),gridAdapter).execute(source);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent  details = new Intent(getActivity(), DetailsActivity.class);
                details.putExtra("id",position);
                startActivity(details);
            }
        });

        return rootView;
    }

    // Parses data from query and updates View with adapter in a background thread


    //extends BaseAdapter to be able add images to Gridview

    //Populates our gridview with images
    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<String> mThumbIds = new ArrayList<>();

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mThumbIds.size();
        }


        public String getItem(int position) {
            return mThumbIds.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView view;
            Log.v("image", "update image");
            if (convertView == null) {
                view = new ImageView(mContext);
                view.setAdjustViewBounds(true);

                view.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                view.setScaleType(ImageView.ScaleType.FIT_XY);
                view.setPadding(5, 5, 5, 5);

            } else {

                view = (ImageView) convertView;
            }

            Cursor cImage = mContext.getContentResolver().query(
                    MovieContract.MovieEntry.CONTENT_URI, null, null, null, null);

            cImage.moveToPosition(position);
            Bitmap b;
            byte[] res = cImage.getBlob(cImage.getColumnIndex(MovieContract.
                    MovieEntry.COLUMN_POSTER));

            Log.v("uri", MovieContract.MovieEntry.buildMovieUri(position).toString());
            Log.v("byte", res.toString());
            b = BitmapFactory.decodeByteArray(res,0,res.length);
            view.setImageBitmap(b);
            cImage.close();
            return view;
        }

        public void add(String result) {

            mThumbIds.add(result);
        }
        public void clear(){
            mThumbIds.clear();
        }


    }
}
