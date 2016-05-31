package japps.sunshine_version_julio.fragments;

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
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import japps.sunshine_version_julio.R;
import japps.sunshine_version_julio.activities.DetailActivity;
import japps.sunshine_version_julio.data.WeatherContract;
import japps.sunshine_version_julio.data.WeatherContract.WeatherEntry;
import japps.sunshine_version_julio.utils.Utility;

/**
 * Created by Julio on 24/5/2016.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ShareActionProvider shareActionProvider;
    private final String HASH_TAG = " #SunshineApp";
    public static final String DETAIL_URI = "URI";
    private final String LOG_TAG = DetailActivity.class.getSimpleName();
    private String mForecast;
    private final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_HUMIDITY,
            WeatherEntry.COLUMN_PRESSURE,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_DEGREES,
            WeatherEntry.COLUMN_WEATHER_ID,
            // This works because the WeatherProvider returns location data joined with
            // weather data, even though they're stored in two different tables.
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_HUMIDITY = 5;
    public static final int COL_WEATHER_PRESSURE = 6;
    public static final int COL_WEATHER_WIND_SPEED = 7;
    public static final int COL_WEATHER_DEGREES = 8;
    public static final int COL_WEATHER_CONDITION_ID = 9;
    private Uri mUri;


    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.fragment_share, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if (mForecast != null) {
            setShareIntent();
        }
        super.onCreateOptionsMenu(menu, inflater);
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

    public void onLocationChanged(String newLocation) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        if (mUri == null) { // if not Uri is provided return null
            return null;
        }
        return new CursorLoader(getActivity(), mUri, DETAIL_COLUMNS,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }
        int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
        ViewHolder holder = (ViewHolder) getView().getTag();

        // Set weather image
        holder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

        // Set date from data
        long dateMillis = data.getLong(COL_WEATHER_DATE);
        holder.dayView.setText(Utility.getDayName(getActivity(), dateMillis));
        holder.dateView.setText(Utility.getFormattedMonthDay(getActivity(), dateMillis));

        // Set weather from data
        String description = data.getString(COL_WEATHER_DESC);
        holder.descriptionView.setText(Utility.translateForecast(description));
        holder.iconView.setContentDescription(description);

        // Set high temperature from data
        double high = data.getDouble(COL_WEATHER_MAX_TEMP);
        boolean isMetric = Utility.isMetric(getActivity());
        holder.highTempView.setText(Utility.formatTemperature(getContext(), high, isMetric));

        // Set low temperature from data
        double low = data.getDouble(COL_WEATHER_MIN_TEMP);
        holder.lowTempView.setText(Utility.formatTemperature(getContext(), low, isMetric));

        // Set humidity from data
        float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
        holder.humidity.setText(getActivity().getString(R.string.format_humidity, humidity));

        // Set wind from data
        float windSpd = data.getFloat(COL_WEATHER_WIND_SPEED);
        float windDir = data.getFloat(COL_WEATHER_DEGREES);
        holder.wind.setText(Utility.getFormattedWind(getActivity(), windSpd, windDir));

        // Set pressure from data
        float pressure = data.getFloat(COL_WEATHER_PRESSURE);
        holder.pressure.setText(getActivity().getString(R.string.format_pressure, pressure));

        // We still need this for the share intent
        mForecast = String.format("%s - %s - %s/%s", dateMillis, description, high, low);

        setShareIntent();
    }

    @Override
    public void onLoaderReset(Loader loader) {
    }

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dayView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;
        public final TextView humidity;
        public final TextView wind;
        public final TextView pressure;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.detail_forecast_icon);
            dayView = (TextView) view.findViewById(R.id.detail_day_textview);
            dateView = (TextView) view.findViewById(R.id.detail_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.detail_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.detail_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.detail_low_textview);
            humidity = (TextView) view.findViewById(R.id.detail_humidity_textview);
            wind = (TextView) view.findViewById(R.id.detail_wind_textview);
            pressure = (TextView) view.findViewById(R.id.detail_pressure_textview);
        }
    }
}
