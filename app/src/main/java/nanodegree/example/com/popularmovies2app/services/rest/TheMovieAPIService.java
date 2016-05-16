package nanodegree.example.com.popularmovies2app.services.rest;


import nanodegree.example.com.popularmovies2app.model.MovieAPIResponse;
import nanodegree.example.com.popularmovies2app.model.MovieAPIReviewResponse;
import nanodegree.example.com.popularmovies2app.model.MovieAPIVideoResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by parth panchal on 29-02-2016.
 */
public interface TheMovieAPIService
{
    @GET("3/discover/movie")
    Call<MovieAPIResponse> fetchMovies(@Query("api_key") String apiKey, @Query("sort_by") String sort);

    // http://api.themoviedb.org/3/movie/83542/reviews?api_key=###
    @GET("3/movie/{movieId}/reviews")
    Call<MovieAPIReviewResponse> fetchMovieReview(@Path("movieId") String movieId, @Query("api_key") String apiKey);

    //http://api.themoviedb.org/3/movie/550/videos?api_key=###
    @GET("3/movie/{movieId}/videos")
    Call<MovieAPIVideoResponse> fetchMovieTrailer(@Path("movieId") String movieId, @Query("api_key") String apiKey);
}
