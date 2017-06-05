package japps.sunshine_version_julio.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import japps.sunshine_version_julio.R;
import japps.sunshine_version_julio.adapters.ForecastAdapter;
import japps.sunshine_version_julio.data.WeatherContract;
import japps.sunshine_version_julio.sync.SunshineSyncAdapter;
import japps.sunshine_version_julio.utils.Utility;

;

/**
 * Created by Julio on 21/10/2015.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    private TextView mEmptyView;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Uri dateUri);
    }

    private ForecastAdapter mAdapter;
    private Context mContext;
    private int mPosition;
    private boolean mUseTodayLayout;
    private ListView mListView;
    private boolean mSavedState;
    private final int FORECAST_LOADER_ID = 0;
    private final String POSITION_KEY = "POSITION";
    public static final String CITY_KEY = "CITY";
    public static final String UNITS_KEY = "UNITS";
    public static final String CURSOR_LOADER_FINISH_KEY = "LOADER_FINISH";
    public static final String CURSOR_LOADER_FINISH_TABLET_KEY = "LOADER_FINISH_TABLET";
    private final String LOG_TAG = ForecastFragment.class.getSimpleName();

    public static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_CONDITION_ID = 6;
    public static final int COL_COORD_LAT = 7;
    public static final int COL_COORD_LONG = 8;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        //setCityTimeTextView();
    }

    @Override
    public void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_map) {
            showMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onLocationChanged() {
        updateWeather();
    }

    public void showMap() {
        if (mAdapter != null) {
            Cursor c = mAdapter.getCursor();
            if (c != null && c.moveToPosition(0)) {
                String posLat = c.getString(COL_COORD_LAT);
                String posLong = c.getString(COL_COORD_LONG);
//                String city = Utility.getPreferredCity(getActivity());
//                Uri uriGeo = Uri.parse(String.format("geo:0,0?q=%s", city));
                Uri uriGeo = Uri.parse("geo:0,0?q=" + posLat + "," + posLong);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(uriGeo);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Snackbar.make(getActivity().getCurrentFocus(), "App map not found", Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }

    public void restartLoader() {
        getLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
    }

    public void destroyLoader() {
        getLoaderManager().destroyLoader(FORECAST_LOADER_ID);
    }

    public void setCityTimeTextView() {
        if (getView() != null) {
            TextView textView = (TextView) getView().findViewById(R.id.cityName_TextView);
            String city = Utility.getPreferredCity(mContext);
            String horaActual = new SimpleDateFormat("h:mm a").format(
                    Calendar.getInstance(
                            TimeZone.getTimeZone("America/Caracas"),
                            new Locale("es-ES")
                    ).getTime()
            );
            textView.setText(String.format("%s, %s", Utility.capitalize(city), horaActual));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(POSITION_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }


    public void updateWeather() {
        SunshineSyncAdapter.syncImmediately(mContext);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (mAdapter != null) {
            mAdapter.setUseTodayLayout(useTodayLayout);
        }
    }

    public void selectListViewItem() {
        int count = mListView.getAdapter().getCount();
        if (count > 0) {
            View view = mListView.getAdapter().getView(mPosition, null, null);
            long viewId = mListView.getAdapter().getItemId(mPosition);
            mListView.performItemClick(view, mPosition, viewId);
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        mAdapter.swapCursor(null);
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(POSITION_KEY)) {
            mPosition = savedInstanceState.getInt(POSITION_KEY);
            mSavedState = true;
        }
        mContext = getActivity();
        mAdapter = new ForecastAdapter(mContext, null, 0);
        mAdapter.setUseTodayLayout(mUseTodayLayout);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mEmptyView = (TextView) rootView.findViewById(R.id.listview_forecast_empty);
        mListView.setEmptyView(mEmptyView);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocationSetting(mContext);
                    ((Callback) mContext).onItemSelected(WeatherContract.WeatherEntry
                            .buildWeatherLocationWithDate(locationSetting,
                                    cursor.getLong(COL_WEATHER_DATE)
                            ));
                    mPosition = position;
                }
            }
        });

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocationSetting(mContext);
        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());
        CursorLoader cl = new CursorLoader(mContext, weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        if (Utility.isTablet(mContext) && mPosition != ListView.INVALID_POSITION) {
            mListView.smoothScrollToPosition(mPosition);
            if (!mSavedState) {
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(CURSOR_LOADER_FINISH_TABLET_KEY));
            }
        } else if (!Utility.isTablet(mContext)) {
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(CURSOR_LOADER_FINISH_KEY));
        }
    }

    private void updateEmptyView() {
        @SunshineSyncAdapter.LocationStatus int location = Utility.getLocationStatus(getActivity());
        if (location != SunshineSyncAdapter.LOCATION_STATUS_OK && mAdapter.getCount() == 0) {
            TextView tv = (TextView) getView().findViewById(R.id.listview_forecast_empty);
            if (null != tv) {
                // if cursor is empty, why? do we have an invalid location
                int message = R.string.no_weather;
                switch (location) {
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN:
                        message = R.string.empty_forecast_list_server_down;
                        break;
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID:
                        message = R.string.empty_forecast_list_server_error;
                        break;
                    case SunshineSyncAdapter.LOCATION_STATUS_INVALID:
                        message = R.string.empty_forecast_list_invalid_location;
                    default:
                        if (!Utility.isNetworkAvailable(getActivity())) {
                            message = R.string.no_network;
                        }
                }
                tv.setText(message);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(CURSOR_LOADER_FINISH_KEY));
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_location_status_key))) {
            updateEmptyView();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
