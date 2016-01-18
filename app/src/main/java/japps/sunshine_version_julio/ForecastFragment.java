package japps.sunshine_version_julio;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

;

/**
 * Created by Julio on 21/10/2015.
 */
public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> adaptador;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    public void updateWeather() {
        String city = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default)
        );
        String tempUnit = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(
                getString(R.string.pref_tempUnit_key),
                getString(R.string.pref_tempUnit_default)
        );
        FetchWeatherTask task = new FetchWeatherTask(getContext(), adaptador);
        task.execute(city, tempUnit);
        if (getView() != null) {
            TextView textView = (TextView) getView().findViewById(R.id.mainTextView);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        adaptador = new ArrayAdapter<>(getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                new ArrayList<String>());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(adaptador);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent newActivity = new Intent(getContext(), DetailActivity.class);
                newActivity.putExtra(getString(R.string.detail_fragment_key),
                        adaptador.getItem(position).toString());
                startActivity(newActivity);
            }
        });

        return rootView;
    }
}
