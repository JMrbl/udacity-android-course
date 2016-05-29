package japps.sunshine_version_julio.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
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
import japps.sunshine_version_julio.tasks.FetchWeatherTask;
import japps.sunshine_version_julio.utils.Utility;

;

/**
 * Created by Julio on 21/10/2015.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

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
    private int mPosition;
    private ListView mListView;
    private final int FORECAST_LOADER_ID = 0;
    private final String POSITION_KEY = "POSITION";
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
        setCityTimeTextView();
    }

    public void onLocationChanged() {
        updateWeather();
    }

    public void restartLoader() {
        getLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
    }

    public void setCityTimeTextView() {
        if (getView() != null) {
            TextView textView = (TextView) getView().findViewById(R.id.mainTextView);
            String city = Utility.getPreferredCity(getActivity());
            String horaActual = new SimpleDateFormat("h:mm a").format(
                    Calendar.getInstance(
                            TimeZone.getTimeZone("America/Caracas"),
                            new Locale("es-ES")
                    ).getTime()
            );
            textView.setText(city + ", " + horaActual);
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
        String city = Utility.getPreferredCity(getActivity());
        String tempUnit = Utility.getPreferredTempUnit(getActivity());
        FetchWeatherTask task = new FetchWeatherTask(getActivity()) {
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                restartLoader();
            }
        };
        task.execute(city, tempUnit);
    }

    public void setUseTodayLayout (boolean useTodayLayout){
        if (mAdapter != null){
            mAdapter.setUseTodayLayout(useTodayLayout);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(POSITION_KEY)) {
            mPosition = savedInstanceState.getInt(POSITION_KEY);
        }
        mAdapter = new ForecastAdapter(getActivity(), null, 0);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocationSetting(getActivity());
                    ((Callback) getActivity()).onItemSelected(WeatherContract.WeatherEntry
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
        String locationSetting = Utility.getPreferredLocationSetting(getActivity());
        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());
        CursorLoader cl = new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mPosition != ListView.INVALID_POSITION) {
            mListView.smoothScrollToPosition(mPosition);
        }
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
