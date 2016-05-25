package japps.sunshine_version_julio.activities;

/**
 * Created by Julio on 13-11-2015.
 */


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import japps.sunshine_version_julio.fragments.ForecastFragment;
import japps.sunshine_version_julio.R;
import japps.sunshine_version_julio.utils.Utility;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
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

    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        private ShareActionProvider shareActionProvider;
        private final String HASH_TAG = " #SunshineApp";
        private final String LOG_TAG = DetailActivity.class.getSimpleName();
        private String mForecast;
        private final int DETAIL_LOADER_ID = 1;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            getLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            if (container == null) {return null;}
            return inflater.inflate(R.layout.fragment_detail, container, false);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

            inflater.inflate(R.menu.fragment_share, menu);
            MenuItem menuItem = menu.findItem(R.id.action_share);
            shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
            if (mForecast != null){
                setShareIntent();
            }
            super.onCreateOptionsMenu(menu, inflater);
        }

        public Uri getUri() {
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                return intent.getData();
            }
            return null;
        }

        private void setShareIntent() {
            if (shareActionProvider != null) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                } else {
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                }
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, mForecast + HASH_TAG);
                shareActionProvider.setShareIntent(intent);
            } else {
                Log.d(LOG_TAG, "Share Action Provider is null?");
            }
        }

        @Override
        public Loader onCreateLoader(int id, Bundle args) {
            Uri forecastUri = getUri();
            if (forecastUri == null){return null;}
            return new CursorLoader(getActivity(),forecastUri, ForecastFragment.FORECAST_COLUMNS,
                    null,null,null);
        }

        @Override
        public void onLoadFinished(Loader loader, Cursor data) {
            if (!data.moveToFirst()){
                return;
            }
            TextView textView = (TextView) getView().findViewById(R.id.newActivityText);
            mForecast = Utility.convertCursorRowToUXFormat(getActivity(),data);
            textView.setText(mForecast);
            textView.setTextSize(30);
            setShareIntent();
        }

        @Override
        public void onLoaderReset(Loader loader) {
        }
    }

}
