package nanodegree.example.com.popularmovies2app.services;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import nanodegree.example.com.popularmovies2app.MainActivity;
import nanodegree.example.com.popularmovies2app.config.MovieFetchOptions;
import nanodegree.example.com.popularmovies2app.model.Movie;

import nanodegree.example.com.popularmovies2app.model.provider.MovieContract.MovieEntry;
import nanodegree.example.com.popularmovies2app.model.provider.MovieContract;
import nanodegree.example.com.popularmovies2app.utils.HttpUtils;
import nanodegree.example.com.popularmovies2app.utils.JsonUtils;


/**
 * Created by parth panchal on 29-02-2016.
 */
public class FetchMovieData extends AsyncTask<String, Void, List<Movie>>
{

    private final HttpUtils httpUtils;
    private final JsonUtils jsonUtils;
    private final String TAG = FetchMovieData.class.getSimpleName();

    private MainActivity.DelegateMovieAdapterProcess movieDelegate;
    private Context context;
    private MovieFetchOptions loadType;


    public FetchMovieData(Context context)
    {
        httpUtils = new HttpUtils();
        jsonUtils = new JsonUtils();
        this.context = context;
    }

    public FetchMovieData(MainActivity.DelegateMovieAdapterProcess delegate)
    {
        this.movieDelegate = delegate;
        httpUtils = new HttpUtils();
        jsonUtils = new JsonUtils();
    }

    public void setMovieDelegate(MainActivity.DelegateMovieAdapterProcess movieDelegate)
    {
        this.movieDelegate = movieDelegate;
    }


    @Override
    protected List<Movie> doInBackground(String... params)
    {
        Cursor cursor = context.getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI, null, null, null, null);
        List<Movie> movies = null;
        if (!(loadType == MovieFetchOptions.Favourite))
        {
            String location = params[0];
            String data = null;
            data = getData(location);

            JSONArray result = jsonUtils.getMovieResultsInJsonArray(data);
            movies = jsonUtils.parseJsonArrayToList(result, params[1], params[2]);
            if (cursor.getCount() > 0 && cursor.moveToFirst())
            {
                while (!cursor.isAfterLast())
                {
                    Movie m = getMovieFromCursor(cursor);

                    if (movies.contains(m))
                    {
                        int movieIndex = movies.indexOf(m);
                        movies.set(movieIndex, m);
                    }
                    cursor.moveToNext();
                }
            }


        }
        else
        {
            if (cursor.getCount() > 0 && cursor.moveToFirst())
            {
                movies = new ArrayList<>();
                while (!cursor.isAfterLast())
                {
                    Movie m = getMovieFromCursor(cursor);
                    movies.add(m);
                    cursor.moveToNext();
                }
            }

        }
        return movies;
    }

    @NonNull
    private Movie getMovieFromCursor(Cursor cursor)
    {
        Movie m = new Movie();
        m.setId(cursor.getInt(cursor.getColumnIndex(MovieEntry.MOVIE_ID)));
        m.setAdult(cursor.getInt(cursor.getColumnIndex(MovieEntry.ADULT)) > 0);
        m.setOriginalTitle(cursor.getString(cursor.getColumnIndex(MovieEntry.ORIGINAL_TITLE)));
        m.setPosterPath(cursor.getString(cursor.getColumnIndex(MovieEntry.POSTER_PATH)));
        m.setBackdropPath(cursor.getString(cursor.getColumnIndex(MovieEntry.BACKDROP_PATH)));
        m.setOverview(cursor.getString(cursor.getColumnIndex(MovieEntry.OVERVIEW)));
        m.setReleaseDate(cursor.getString(cursor.getColumnIndex(MovieEntry.RELEASE_DATE)));
        m.setVoteCount(cursor.getInt(cursor.getColumnIndex(MovieEntry.VOTE_COUNT)));
        m.setVoteAverage(cursor.getString(cursor.getColumnIndex(MovieEntry.VOTE_AVERAGE)));
        m.setPopularity(cursor.getDouble(cursor.getColumnIndex(MovieEntry.POPULARITY)));
        m.setFavourite(cursor.getInt(cursor.getColumnIndex(MovieEntry.FAVOURITE)) > 0);
        return m;
    }

    @Override
    protected void onPostExecute(List<Movie> movies)
    {

        movieDelegate.process(movies);
    }

    private String getData(String location)
    {
        String jsonString = null;
        HttpURLConnection connection;
        BufferedReader reader = null;
        connection = httpUtils.getConnection(location);

        InputStream is = httpUtils.getInputStream(connection);

        if (is != null)
        {
            reader = httpUtils.getReader(is);
            jsonString = readData(reader);

        }

        httpUtils.closeConnection(connection);
        httpUtils.closeReader(reader);
        return jsonString;
    }


    private String readData(BufferedReader reader)
    {
        String line;
        StringBuffer buffer = new StringBuffer();
        try
        {
            while ((line = reader.readLine()) != null)
            {
                buffer.append(line + "\n");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (buffer.length() == 0)
        {
            return null;
        }

        String jsonString = buffer.toString();
        return jsonString;
    }

    public MovieFetchOptions getLoadType()
    {
        return loadType;
    }

    public void setLoadType(MovieFetchOptions loadType)
    {
        this.loadType = loadType;
    }

}
