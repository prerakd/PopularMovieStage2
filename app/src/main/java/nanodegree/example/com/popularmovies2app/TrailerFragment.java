package nanodegree.example.com.popularmovies2app;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import nanodegree.example.com.popularmovies2app.adapter.TrailerAdapter;
import nanodegree.example.com.popularmovies2app.config.AppStatus;
import nanodegree.example.com.popularmovies2app.model.Movie;
import nanodegree.example.com.popularmovies2app.model.MovieAPIVideoResponse;
import nanodegree.example.com.popularmovies2app.model.MovieVideo;
import nanodegree.example.com.popularmovies2app.services.rest.TheMovieAPIService;
import nanodegree.example.com.popularmovies2app.utils.HttpUtils;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TrailerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TrailerFragment extends Fragment implements AdapterView.OnItemClickListener
{

    private static final String TAG = TrailerFragment.class.getSimpleName();

    @Nullable
    @BindString(R.string.q_retrofit_base_url)
    String baseUrl;

    @BindString(R.string.youtube_url_prefix)
    String youtubeUrlPrefix;


    @Nullable
    @Bind(R.id.trailer_list_view)
    ListView trailerListView;

    private ArrayAdapter<MovieVideo> trailerAdapter;
    private List<MovieVideo> trailers;


    public TrailerFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TrailerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TrailerFragment newInstance(Movie m)
    {
        Bundle args = new Bundle();
        args.putParcelable("movie", m);
        TrailerFragment fragment = new TrailerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_trailer, container, false);
        ButterKnife.bind(this, rootView);

        if (savedInstanceState != null)
        {
            Bundle b = this.getArguments();
            trailers = savedInstanceState.getParcelableArrayList("MovieTrailerList");
            trailerAdapter = new TrailerAdapter(getContext(), R.layout.list_movie_trailer, trailers, getActivity());
            trailerListView.setAdapter(trailerAdapter);
            trailerListView.setOnItemClickListener(this);
        }
        else
        {
            if (AppStatus.isOnline(getContext()))
            {
                getMovieTrailerUsingRetrofit();
            }
            else
            {
                Toast.makeText(getContext(), "This feature is not available for offline", Toast.LENGTH_SHORT).show();
            }
        }

        return rootView;
    }

    public void getMovieTrailerUsingRetrofit()
    {
        // retrofit debug purpose
        OkHttpClient.Builder httpClient = HttpUtils.getDebugBuilder();
        Movie m = getMovie();

        Retrofit apiService = new Retrofit.Builder().baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();


        TheMovieAPIService initAPIService = apiService.create(TheMovieAPIService.class);
        final Call<MovieAPIVideoResponse> apiCall = initAPIService.fetchMovieTrailer(String.valueOf(m.getId()), BuildConfig
                .THE_MOVIE_DB_API_KEY);

        apiCall.enqueue(new Callback<MovieAPIVideoResponse>()
        {
            @Override
            public void onResponse(Call<MovieAPIVideoResponse> call, Response<MovieAPIVideoResponse> response)
            {
                List<MovieVideo> apiMovieTrailerList = response.body().apiMovieVideosList;
                int trailerCount = apiMovieTrailerList.size();
                if (trailerCount > 0)
                {

                    trailers = new ArrayList<MovieVideo>(apiMovieTrailerList);
                    trailerAdapter = new TrailerAdapter(getContext(), R.layout.list_movie_trailer, trailers, getActivity());
                    trailerListView.setAdapter(trailerAdapter);
                    trailerListView.setOnItemClickListener(TrailerFragment.this);

                }
                else
                {
                    Toast.makeText(getContext(), "No Trailers found for this movie...", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MovieAPIVideoResponse> call, Throwable t)
            {
                Log.d(TAG, "onFailure: Retro: " + t.getMessage());
            }
        });
    }

    private Movie getMovie()
    {
        Bundle extras = getActivity().getIntent().getExtras();
        Bundle b = (extras != null && extras.getParcelable("movie") != null) ? extras : this.getArguments();
        Movie movie = b.getParcelable("movie");
        return movie;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        String ytTrailerUrl = youtubeUrlPrefix + trailers.get(position).getKey();
        Log.d(TAG, "onItemClick: " + view.getId());
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ytTrailerUrl)));
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putParcelableArrayList("MovieTrailerList", (ArrayList<? extends Parcelable>) trailers);
        super.onSaveInstanceState(outState);
    }


}
