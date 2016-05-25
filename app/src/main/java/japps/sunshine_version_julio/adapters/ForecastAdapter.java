package japps.sunshine_version_julio.adapters;

/**
 * Created by Julio on 19/1/2016.
 */

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import japps.sunshine_version_julio.R;
import japps.sunshine_version_julio.fragments.ForecastFragment;
import japps.sunshine_version_julio.utils.Utility;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;

        switch (viewType){
            case VIEW_TYPE_TODAY: {
                layoutId = R.layout.list_item_forecast_today;
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                layoutId = R.layout.list_item_forecast;
                break;
            }
        }
        return LayoutInflater.from(context).inflate(layoutId, parent, false);
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);

        // Set forecast icon
        ImageView iconView =  (ImageView) view.findViewById(R.id.list_item_icon);
        iconView.setImageResource(R.mipmap.weather_icon);

        // Set date from cursor
        TextView dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
        long dateMillis = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        dateView.setText(Utility.getFriendlyDayString(context,dateMillis));

        // Set weather from cursor
        TextView forecastView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
        String forecastValue = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        forecastView.setText(forecastValue);

        // Set high temperature from cursor
        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        TextView highView = (TextView) view.findViewById(R.id.list_item_high_textview);
        highView.setText(Utility.formatTemperature(high, Utility.isMetric(context)));

        // Set low temperature from cursor
        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        TextView lowView = (TextView) view.findViewById(R.id.list_item_low_textview);
        lowView.setText(Utility.formatTemperature(low, Utility.isMetric(context)));
    }
}
