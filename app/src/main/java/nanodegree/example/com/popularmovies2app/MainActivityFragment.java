package nanodegree.example.com.popularmovies2app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


import butterknife.BindString;
import butterknife.ButterKnife;
import nanodegree.example.com.popularmovies2app.adapter.MoviePosterAdapter;
import nanodegree.example.com.popularmovies2app.config.AppStatus;
import nanodegree.example.com.popularmovies2app.config.MovieFetchOptions;
import nanodegree.example.com.popularmovies2app.model.Movie;
import nanodegree.example.com.popularmovies2app.model.MovieAPIResponse;
import nanodegree.example.com.popularmovies2app.services.FetchMovieData;
import nanodegree.example.com.popularmovies2app.services.rest.TheMovieAPIService;
import nanodegree.example.com.popularmovies2app.utils.HttpUtils;
import nanodegree.example.com.popularmovies2app.utils.TheMovieDBUtils;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements MainActivity.DelegateMovieAdapterProcess<Movie>
        , AdapterView.OnItemClickListener
{
    @BindString(R.string.q_retrofit_base_url)
    String baseUrl;
    private final String TAG = MainActivityFragment.class.getSimpleName();
    private List<Movie> moviesList;
    private ArrayAdapter movieAdapter;
    private GridView gridMoviePoster;
    private boolean twoPane = false;
    private boolean isPositionFirst = true;
    private static final String DF_TAG = "DFTAG";


    public MainActivityFragment()
    {
        moviesList = new ArrayList<>();
    }

    public boolean isTwoPane()
    {
        return twoPane;
    }

    public void setTwoPane(boolean twoPane)
    {
        this.twoPane = twoPane;
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);
        bridgeGridViewWithAdapter(rootView);

        checkBundleAndProcess(savedInstanceState);

        gridMoviePoster.setOnItemClickListener(this);
        return rootView;
    }

    public static MainActivityFragment newInstance()
    {



        MainActivityFragment fragment = new MainActivityFragment();
        return fragment;
    }

    private void bridgeGridViewWithAdapter(View rootView)
    {
        gridMoviePoster = (GridView) rootView.findViewById(R.id.grid_poster);
        movieAdapter = getPosterAdapter();
        gridMoviePoster.setAdapter(movieAdapter);
    }

    private void checkBundleAndProcess(Bundle savedInstanceState)
    {
        Log.d(TAG, "checkBundleAndProcess: ");
        if (AppStatus.isOnline(getContext()))
        {
            if (savedInstanceState != null)
            {
                Log.d(TAG, "Bundled: " + savedInstanceState.getParcelableArrayList("MovieList"));
                moviesList = savedInstanceState.getParcelableArrayList("MovieList");
                process(moviesList);
            }
            else
            {
                // this code will work on coming back from detail Activity
                MovieFetchOptions option = AppStatus.getState() == null ? MovieFetchOptions.Popular : AppStatus.getState();
                populateGridView(option);
            }
        }
        else
        {
            AppStatus.setState(MovieFetchOptions.Favourite);
            populateGridView(MovieFetchOptions.Favourite);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int menuId = item.getItemId();
        switch (menuId)
        {
            case R.id.action_popular:
            {
                AppStatus.setState(MovieFetchOptions.Popular);
                isPositionFirst = true;
                populateGridView(MovieFetchOptions.Popular);
                break;
            }
            case R.id.action_rating:
            {
                AppStatus.setState(MovieFetchOptions.Rating);
                isPositionFirst = true;
                populateGridView(MovieFetchOptions.Rating);
                break;
            }
            case R.id.action_favourite:
            {
                AppStatus.setState(MovieFetchOptions.Favourite);
                isPositionFirst = true;
                populateGridView(MovieFetchOptions.Favourite);
                break;
            }

        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {

        outState.putParcelableArrayList("MovieList", (ArrayList<? extends Parcelable>) moviesList);
        super.onSaveInstanceState(outState);
    }

    private void populateGridView(MovieFetchOptions option)
    {
        TheMovieDBUtils movieUtil = new TheMovieDBUtils(getContext());
        String sortOrder = movieUtil.getSortingOrder(option);


        getMovieDataUsingAsyncTask(option, movieUtil);

    }

    private void getMovieDataUsingAsyncTask(MovieFetchOptions option, TheMovieDBUtils movieUtil)
    {

        String requestUrl = "";
        String backdropBasePath = movieUtil.getStringResource(R.string.img_backdrop_url);
        String posterBasePath = movieUtil.getStringResource(R.string.img_poster_url);

        FetchMovieData movieData = new FetchMovieData(getContext());
        movieData.setMovieDelegate(this);

        if (MovieFetchOptions.Favourite == option)
        {
            movieData.setLoadType(option);
        }
        else
        {
            requestUrl = movieUtil.buildURL(option);
        }
        movieData.execute(requestUrl, posterBasePath, backdropBasePath);
    }

    private ArrayAdapter getPosterAdapter()
    {
        return new MoviePosterAdapter(getContext(), R.layout.grid_main_poster, moviesList);
    }


    @Override
    public void process(List<? extends Movie> movieList)
    {
        if (movieList != null && movieList.size() > 0)
        {
            movieAdapter.clear();
            movieAdapter.addAll(movieList);
            if (isPositionFirst && twoPane)
            {
                Bundle bundle = new Bundle();
                bundle.putParcelable("movie", movieList.get(0));

                Fragment fragment = WideDetailFragment.newInstance();

                fragment.setArguments(bundle);
                int status = getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_frame, fragment, DF_TAG)
                        .commit();
                isPositionFirst = false;

                if (AppStatus.getState() == null)
                {
                    AppStatus.setState(MovieFetchOptions.Popular);
                }
            }

        }
        else
        {
            Toast.makeText(getContext(), "No movies found", Toast.LENGTH_LONG).show();
            AppStatus.setState(MovieFetchOptions.Popular);
        }

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (twoPane)
        {
            Bundle bundle = new Bundle();
            bundle.putParcelable("movie", moviesList.get(position));
            Fragment fragment = WideDetailFragment.newInstance();
            fragment.setArguments(bundle);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_frame, fragment, DF_TAG)
                    .commit();

        }
        else
        {
            Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra("movie", moviesList.get(position));
            startActivity(intent);
        }

    }

    public void getMovieDataUsingRetrofit(String order)
    {
        // retrofit debug purpose
        OkHttpClient.Builder httpClient = HttpUtils.getDebugBuilder();

        Retrofit apiService = new Retrofit.Builder().baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        String sortingOrder = order;

        TheMovieAPIService initAPIService = apiService.create(TheMovieAPIService.class);
        final Call<MovieAPIResponse> apiCall = initAPIService.fetchMovies(BuildConfig.THE_MOVIE_DB_API_KEY, order);

        apiCall.enqueue(new Callback<MovieAPIResponse>()
        {
            @Override
            public void onResponse(Call<MovieAPIResponse> call, Response<MovieAPIResponse> response)
            {
                process(response.body().apiMoviesList);
            }

            @Override
            public void onFailure(Call<MovieAPIResponse> call, Throwable t)
            {
                // display a dialog here about failure
            }
        });
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        isPositionFirst = true;
    }
}
