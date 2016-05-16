package nanodegree.example.com.popularmovies2app.adapter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;


import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import nanodegree.example.com.popularmovies2app.MainActivity;
import nanodegree.example.com.popularmovies2app.R;
import nanodegree.example.com.popularmovies2app.config.AppStatus;
import nanodegree.example.com.popularmovies2app.config.FavouriteInsertStatus;
import nanodegree.example.com.popularmovies2app.config.MovieFetchOptions;
import nanodegree.example.com.popularmovies2app.model.Favourite;
import nanodegree.example.com.popularmovies2app.model.Movie;
import nanodegree.example.com.popularmovies2app.model.provider.MovieContract;
import nanodegree.example.com.popularmovies2app.services.task.SaveFavouriteImageToDisk;
import nanodegree.example.com.popularmovies2app.utils.TheMovieDBUtils;

/**
 * Created by parth panchal on 29-02-2016.
 */
public class MoviePosterAdapter extends ArrayAdapter<Movie>
{
    private final String TAG = MoviePosterAdapter.class.getSimpleName();

    private final Context context;
    private int layoutResourceId;

    private List<Movie> moviesList;

    private int currentPosition = 0;
    private static final String FAV = "fav";

    @BindString(R.string.img_backdrop_url)
    String mainBackdropPrefix;

    @Bind(R.id.img_poster)
    ImageView imageView;
    @Bind(R.id.poster_favourite)
    ImageView favImage;
    public MoviePosterAdapter(Context context, int resource, List<Movie> moviesList)
    {
        super(context, R.layout.grid_main_poster, moviesList);
        this.context = context;
        this.layoutResourceId = resource;
        this.moviesList = moviesList;
    }

    public List<Movie> getMoviesList()
    {
        return moviesList;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View row = convertView;
        if (row == null)
        {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
        }
        Movie movie = moviesList.get(position);
        ButterKnife.bind(this, row);

        favImage.setTag(position);

        modifyIconForFavouriteMovies(row);

        loadImageFromLocalOrOnline(movie);

        return row;
    }

    private void loadImageFromLocalOrOnline(Movie movie)
    {
        if (AppStatus.getState() == MovieFetchOptions.Favourite)
        {
            String localPath = AppStatus.getLocalStoragePath(getContext()) + movie.getPosterPath();
            Picasso.with(this.context).load(new File(localPath))
                    .placeholder(R.drawable.main_default_poster_drawable)
                    .error(R.drawable.main_error_poster_drawable)
                    .into(imageView);
        }
        else
        {
            String posterPath = mainBackdropPrefix + movie.getPosterPath();
            Picasso.with(this.context).load(posterPath)
                    .placeholder(R.drawable.main_default_poster_drawable)
                    .error(R.drawable.main_error_poster_drawable)
                    .into(imageView);
        }
    }

    private void modifyIconForFavouriteMovies(View row)
    {
        int tag = (int) favImage.getTag();

        Movie scrollMovie = moviesList.get(tag);
        if (scrollMovie.isFavourite())
        {
            ImageView iv = (ImageView) row.findViewById(R.id.poster_favourite);
            iv.setImageResource(R.drawable.ic_favorite_black_24dp);
        }
        else
        {
            ImageView iv = (ImageView) row.findViewById(R.id.poster_favourite);
            iv.setImageResource(R.drawable.ic_favorite_border_black_24dp);
        }
    }

    @OnClick(R.id.poster_favourite)
    void favouriteClick(final View view)
    {
        final View currentView = view;
        final int position = (int) (view.getTag());
        final Movie currentMovie = moviesList.get(position);
        ImageView fav = (ImageView) view.findViewById(R.id.poster_favourite);
        toggleFavourite(currentView, currentMovie, fav);
    }


    private void toggleFavourite(final View currentView, final Movie currentMovie, ImageView fav)
    {
        final ImageView imageView = (ImageView) currentView;
        AsyncTask<Void, Void, Favourite> task = new AsyncTask<Void, Void, Favourite>()
        {

            @Override
            protected Favourite doInBackground(Void... params)
            {
                Favourite model = new Favourite();
//                model.view = imageView;
                model.status = addToFavourite(currentMovie);
                model.movie = currentMovie;

                return model;
            }

            @Override
            protected void onPostExecute(Favourite favModel)
            {
                favModel.view = currentView;
                ImageView iv = (ImageView) favModel.view;
                executeByFavouriteState(favModel, iv);
            }
        };

        task.execute();
    }

    private void executeByFavouriteState(Favourite favModel, ImageView iv)
    {
        switch (favModel.status)
        {
            case Sucess:
            {
                iv.setImageResource(R.drawable.ic_favorite_black_24dp);
                downloadImagesAndSaveToDisk(favModel);
                Toast.makeText(getContext(), "Added to favourite", Toast.LENGTH_LONG).show();
                break;
            }
            case Remove:
            {
                removeFavouriteImages(favModel);
                Toast.makeText(getContext(), "Removed from favourite", Toast.LENGTH_LONG).show();
                if (AppStatus.getState() == MovieFetchOptions.Favourite)
                {
                    int favMovieIndex = moviesList.indexOf(favModel.movie);
                    if (favMovieIndex != -1)
                    {
                        moviesList.remove(favMovieIndex);
                        notifyDataSetChanged();
                        redirectIfEmpty(favModel);
                    }
                }
                else
                {
                    iv.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                }
                break;
            }
            case Failure:
            {
                Toast.makeText(getContext(), "Error: tap again", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void redirectIfEmpty(Favourite favModel)
    {
        if(moviesList.size() < 1  )
        {
            if(AppStatus.isOnline(getContext()))
            {
                // I dont know whether this is correct or not, please give feedback
                Intent intent = new Intent(favModel.view.getContext(), MainActivity.class);
                AppStatus.setState(MovieFetchOptions.Popular);
                favModel.view.getContext().startActivity(intent);
            }
            else
            {
//                new AlertDialog.Builder()
            }
        }
    }

    private void removeFavouriteImages(Favourite favModel)
    {
        final String[] files = {favModel.movie.getPosterPath(), favModel.movie.getBackdropPath()};
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                for (String file : files)
                {
                    String localPath = AppStatus.getLocalStoragePath(getContext());
                    File f = new File(localPath + file);


                    if (f.exists())
                    {
                        f.delete();
                    }
                }

            }
        }).start();
    }

    private void downloadImagesAndSaveToDisk(Favourite favModel)
    {
        final Movie m = favModel.movie;
        String backdropPath = mainBackdropPrefix + m.getBackdropPath();
        String posterPath = mainBackdropPrefix + m.getPosterPath();
        Picasso.with(getContext()).load(posterPath).into(new SaveFavouriteImageToDisk(getContext(), m.getPosterPath()));
        Picasso.with(getContext()).load(backdropPath).into(new SaveFavouriteImageToDisk(getContext(), m.getBackdropPath()));

    }

    private FavouriteInsertStatus addToFavourite(Movie currentMovie)
    {
        FavouriteInsertStatus status = checkAndInsertFavourite(currentMovie);
        if (status == FavouriteInsertStatus.Sucess)
        {
            status = FavouriteInsertStatus.Sucess;
        }

        return status;
    }

    public FavouriteInsertStatus checkAndInsertFavourite(final Movie m)
    {
        FavouriteInsertStatus status;
        if (getFavouriteStatus(m) == FavouriteInsertStatus.NoDuplicate)
        {
            status = insertFavouriteMovie(m);
        }
        else
        {
            status = removeFavouriteMovie(m);
        }
        return status;
    }

    private FavouriteInsertStatus getFavouriteStatus(Movie currentMovie)
    {
        String currentMovieId = String.valueOf(currentMovie.getId());
        String selection = MovieContract.MovieEntry.MOVIE_ID + "=?";
        Uri currentMovieUri = MovieContract.MovieEntry.buildMovieUri(currentMovieId);
        Cursor cursor = context.getContentResolver().query(currentMovieUri, MovieContract.MovieEntry.PROJECTION_ALL, selection,
                new String[]{currentMovieId},
                null);
        return cursor.getCount() > 0 ? FavouriteInsertStatus.Duplicate : FavouriteInsertStatus.NoDuplicate;
    }

    private FavouriteInsertStatus insertFavouriteMovie(Movie currentMovie)
    {
        FavouriteInsertStatus status = FavouriteInsertStatus.Failure;
        int index = moviesList.indexOf(currentMovie);
        currentMovie.setFavourite(true);
        ContentValues favouriteValues = TheMovieDBUtils.parseMovieToContentValues(currentMovie);
        ContentResolver contentResolver = context.getContentResolver();
        Uri favUri = contentResolver.insert(MovieContract.MovieEntry.CONTENT_URI, favouriteValues);

        long favUriStatus = ContentUris.parseId(favUri);
        String id = String.valueOf(currentMovie.getId());
        Cursor cursor = contentResolver.query(favUri, null, null, new String[]{id}, null);

        if (cursor.moveToFirst())
        {
            long listId = currentMovie.getId();
            long dbId = cursor.getLong(cursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_ID));
            status = (listId == dbId) ? FavouriteInsertStatus.Sucess : FavouriteInsertStatus.Failure;
        }
        cursor.close();
        return status;
    }


    private FavouriteInsertStatus removeFavouriteMovie(Movie currentMovie)
    {
        FavouriteInsertStatus status = FavouriteInsertStatus.Failure;
        currentMovie.setFavourite(false);
        int index = moviesList.indexOf(currentMovie);
        moviesList.get(index).setFavourite(false);

        ContentResolver contentResolver = context.getContentResolver();

        Uri favUri = MovieContract.MovieEntry.buildMovieUri(String.valueOf(currentMovie.getId()));
        int deleteStatus = contentResolver.delete(favUri, MovieContract.MovieEntry.MOVIE_ID + "=?", new
                String[]{String.valueOf(currentMovie.getId())});

        status = (deleteStatus != -1) ? FavouriteInsertStatus.Remove : FavouriteInsertStatus.Failure;

        return status;

    }

}


