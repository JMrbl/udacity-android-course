package japps.sunshine_version_julio.activities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import japps.sunshine_version_julio.R;
import japps.sunshine_version_julio.fragments.DetailFragment;
import japps.sunshine_version_julio.fragments.ForecastFragment;
import japps.sunshine_version_julio.sync.SunshineSyncAdapter;
import japps.sunshine_version_julio.utils.Utility;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback {

    private String mLocationCity;
    private String mUnit;
    static final String DETAIL_FRAGMENT_TAG = "DFTAG";
    private boolean mTwoPane;
    private ProgressBar mProgressBar;
    private ForecastFragment mForecastFragment;
    private DetailFragment mDetailFragment;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String key = intent.getAction();
            switch (key){
                case SunshineSyncAdapter.SYNC_START_KEY:{
                    if (mProgressBar != null){
                        mForecastFragment.destroyLoader();
                        mProgressBar.setVisibility(ProgressBar.VISIBLE);
                    }
                    break;
                }
                case SunshineSyncAdapter.SYNC_FINISH_KEY:{
                    if (mForecastFragment.isAdded()) {
                        mForecastFragment.restartLoader();
                    }
                    break;
                }
                case ForecastFragment.CURSOR_LOADER_FINISH_KEY:{
                    if (mProgressBar != null){
                        mProgressBar.setVisibility(ProgressBar.GONE);
                    }
                    break;
                }
                case ForecastFragment.CURSOR_LOADER_FINISH_TABLET_KEY:{
                    if (mProgressBar != null){
                        mProgressBar.setVisibility(ProgressBar.GONE);
                    }
                    mForecastFragment.selectListViewItem();
                }
            }
//            if(key.equals(SunshineSyncAdapter.SYNC_FINISH_KEY) && mForecastFragment.isAdded()) {
//                mForecastFragment.restartLoader();
//            } else if (key.equals(ForecastFragment.CURSOR_LOADER_FINISH_KEY)){
//                mForecastFragment.selectListViewItem();
//            }
        }
    };

    public boolean isTwoPane() {
        return mTwoPane;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mForecastFragment = (ForecastFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast);
        mDetailFragment = (DetailFragment) getSupportFragmentManager()
                .findFragmentByTag(DETAIL_FRAGMENT_TAG);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mBroadcastReceiver, new IntentFilter(SunshineSyncAdapter.SYNC_START_KEY));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mBroadcastReceiver, new IntentFilter(SunshineSyncAdapter.SYNC_FINISH_KEY));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mBroadcastReceiver, new IntentFilter(ForecastFragment.CURSOR_LOADER_FINISH_KEY));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mBroadcastReceiver, new IntentFilter(ForecastFragment.CURSOR_LOADER_FINISH_TABLET_KEY));
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment(), DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
//            ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager()
//                    .findFragmentById(R.id.fragment_forecast);
            mForecastFragment.setUseTodayLayout(true);
            getSupportActionBar().setElevation(0f);
        }
        SunshineSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        onLocationChanged();
        onUnitsChanged();
    }

    private void onUnitsChanged() {
        String newUnit = Utility.getPreferredTempUnit(this);
        if (newUnit != null && mUnit != null && !newUnit.equals(mUnit)){
            if (mForecastFragment != null) {
                mForecastFragment.restartLoader();
            }
            if (mDetailFragment != null) {
                mDetailFragment.restartLoader();
            }
        }
        mUnit = newUnit;
    }

    private void onLocationChanged() {
        String newLocationCity = Utility.getPreferredCity(this);
        if (newLocationCity != null && mLocationCity != null && !newLocationCity.equals(mLocationCity)) {
//            final ForecastFragment ff = (ForecastFragment) getSupportFragmentManager()
//                    .findFragmentById(R.id.fragment_forecast);
            if (mForecastFragment != null) {
                mForecastFragment.onLocationChanged();
            }
//            DetailFragment df = (DetailFragment) getSupportFragmentManager()
//                    .findFragmentByTag(DETAIL_FRAGMENT_TAG);
            if (mDetailFragment != null) {
                mDetailFragment.onLocationChanged(newLocationCity);
            }
        }
        mLocationCity = newLocationCity;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, fragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);

        }

    }
}


