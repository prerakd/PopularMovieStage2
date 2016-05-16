package nanodegree.example.com.popularmovies2app.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import nanodegree.example.com.popularmovies2app.DetailActivityFragment;
import nanodegree.example.com.popularmovies2app.MainActivityFragment;
import nanodegree.example.com.popularmovies2app.ReviewFragment;
import nanodegree.example.com.popularmovies2app.TrailerFragment;
import nanodegree.example.com.popularmovies2app.model.Movie;


/**
 * Created by parth panchal on 29-02-2016.
 */
public class MoviePagerAdapter extends FragmentStatePagerAdapter
{

    Movie movie;

    public MoviePagerAdapter(FragmentManager fm, Movie movie)
    {
        super(fm);
        this.movie = movie;
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {

            case 0:
                return DetailActivityFragment.newInstance(movie);
            case 1:
                return ReviewFragment.newInstance(movie);
            case 2:
                return TrailerFragment.newInstance(movie);
            default:
                return MainActivityFragment.newInstance();
        }
    }

    @Override
    public int getCount()
    {
        return 3;
    }

    @Override
    public int getItemPosition(Object object)
    {
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return "Overview";
            case 1:
                return "Reviews";
            case 2:
                return "Trailers";
        }
        return "Tab " + (position + 1);
    }


}
