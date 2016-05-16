package nanodegree.example.com.popularmovies2app.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by parth panchal on 29-02-2016.
 */
public class MovieAPIReviewResponse
{
    @SerializedName("results")
    public List<MovieReview> apiMovieReviewsList = new ArrayList<>();
}
