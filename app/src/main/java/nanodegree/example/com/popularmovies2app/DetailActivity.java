package nanodegree.example.com.popularmovies2app;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;


import butterknife.BindString;
import butterknife.ButterKnife;
import nanodegree.example.com.popularmovies2app.adapter.MoviePagerAdapter;
import nanodegree.example.com.popularmovies2app.model.Movie;
import nanodegree.example.com.popularmovies2app.model.MovieAPIVideoResponse;
import nanodegree.example.com.popularmovies2app.services.rest.TheMovieAPIService;
import nanodegree.example.com.popularmovies2app.utils.HttpUtils;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetailActivity extends AppCompatActivity
{
    @BindString(R.string.q_retrofit_base_url)
    String baseUrl;
    private static final String TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        setContentView(R.layout.activity_movie_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*Tab layout for movies*/
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        PagerAdapter pagerAdapter = new MoviePagerAdapter(getSupportFragmentManager(), getMovie());
        viewPager.setAdapter(pagerAdapter);

        // Setup the Tabs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    public void getMovieVideoUsingRetrofit()
    {
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
                Log.d(TAG, "onResponse: Retro: " + response.body().apiMovieVideosList.get(0).getId());
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
        Bundle b = getIntent().getExtras();
        Movie movie = b.getParcelable("movie");
        return movie;
    }

}
