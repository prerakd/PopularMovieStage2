package nanodegree.example.com.popularmovies2app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;


import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import nanodegree.example.com.popularmovies2app.config.AppStatus;
import nanodegree.example.com.popularmovies2app.config.MovieFetchOptions;
import nanodegree.example.com.popularmovies2app.model.Movie;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment
{
    private final String TAG = DetailActivityFragment.class.getSimpleName();

    Movie movie;

    @Bind(R.id.movie_detail_poster_img)
    ImageView poster;
    @Bind(R.id.movie_detail_title)
    TextView title;
    @Bind(R.id.movie_detail_release)
    TextView releaseDate;
    @Bind(R.id.movie_detail_vote_average)
    TextView voteAverage;
    @Bind(R.id.movie_detail_synopsis)
    TextView synopsis;

    @BindString(R.string.img_poster_url)
    String detailBackdropPrefix;

    public DetailActivityFragment()
    {

    }

    public static DetailActivityFragment newInstance(Movie m)
    {

    Bundle args = new Bundle();
    args.putParcelable("movie", m);
        DetailActivityFragment fragment = new DetailActivityFragment();
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View fragmentLayout = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        ButterKnife.bind(this, fragmentLayout);
        movie = getMovie();
        putMovieDataIntoViews();
        return fragmentLayout;
    }


    private Movie getMovie()
    {
        Bundle extras = getActivity().getIntent().getExtras();
        Bundle b = (extras != null && extras.getParcelable("movie") != null ) ? extras : this.getArguments();
        Movie movie = b.getParcelable("movie");
        return movie;
    }


    private void putMovieDataIntoViews()
    {

        if(AppStatus.getState() == MovieFetchOptions.Favourite && !AppStatus.isOnline(getContext()))
        {
            String localPath = AppStatus.getLocalStoragePath(getContext()) + movie.getBackdropPath();
            Picasso.with(getContext()).load(new File(localPath))
                    .placeholder(R.drawable.main_default_poster_drawable)
                    .error(R.drawable.main_error_poster_drawable)
                    .into(poster);
        }
        else
        {
            String backdropPath = detailBackdropPrefix + movie.getBackdropPath();
            Picasso.with(getContext()).load(backdropPath)
                    .placeholder(R.drawable.main_default_poster_drawable)
                    .error(R.drawable.main_error_poster_drawable)
                    .into(poster);
        }
        title.setText(movie.getTitle());
        releaseDate.setText(movie.getReleaseDate());
        voteAverage.setText(""+(Float.valueOf(movie.getVoteAverage()) / 2f));
        synopsis.setText(movie.getOverview());
    }

    private View getViewById(int id, View layout)
    {
        return layout.findViewById(id);
    }
}
