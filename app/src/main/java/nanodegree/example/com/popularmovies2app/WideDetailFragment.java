package nanodegree.example.com.popularmovies2app;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import nanodegree.example.com.popularmovies2app.adapter.MoviePagerAdapter;
import nanodegree.example.com.popularmovies2app.model.Movie;


/**
 * A simple {@link Fragment} subclass.
 */
public class WideDetailFragment extends Fragment
{
    private static final String TAG = WideDetailFragment.class.getSimpleName();
    private View rootView;

    public WideDetailFragment()
    {
        // Required empty public constructor
    }

    public static WideDetailFragment newInstance()
    {
        WideDetailFragment fragment = new WideDetailFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_movie_wide_detail, container, false);
        this.rootView = view;
        Log.d(TAG, "WideFragT: " + getActivity().findViewById(R.id.movie_detail_frame));
        if (getActivity().findViewById(R.id.movie_detail_frame) != null)
        {
            Log.d(TAG, "WideFragT:  expects null at relaunch: " + getMovie());
            if(getMovie() != null)
            {
                ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
                PagerAdapter pagerAdapter = new MoviePagerAdapter(getActivity().getSupportFragmentManager(), getMovie());
                viewPager.setAdapter(pagerAdapter);


                // Setup the Tabs
                TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tab_layout);
                tabLayout.setupWithViewPager(viewPager);
                int tabCount = tabLayout.getTabCount();
                Log.d(TAG, "onCreateView: tabcou: " + tabCount);
            }
        }
        return view;
    }

    public void createTabLayout(View rootView)
    {
        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
        PagerAdapter pagerAdapter = new MoviePagerAdapter(getActivity().getSupportFragmentManager(), getMovie());
        viewPager.setAdapter(pagerAdapter);
        pagerAdapter.notifyDataSetChanged();

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }

    private Movie getMovie()
    {
        Bundle extras = getActivity().getIntent().getExtras();
        Bundle b = (extras != null && extras.getParcelable("movie") != null ) ? extras : this.getArguments();

        Movie movie = null;
        if (b != null)
        {
            movie = b.getParcelable("movie");
        }
        return movie;
    }

}
