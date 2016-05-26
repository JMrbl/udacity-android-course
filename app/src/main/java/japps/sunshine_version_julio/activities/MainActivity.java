package japps.sunshine_version_julio.activities;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import japps.sunshine_version_julio.fragments.ForecastFragment;
import japps.sunshine_version_julio.R;
import japps.sunshine_version_julio.utils.Utility;

public class MainActivity extends AppCompatActivity {

    private String mLocationCity;
    static final String FORECAST_FRAGMENT_TAG = "FORECAST_FRAGMENT";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            mLocationCity = Utility.getPreferredCity(this);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment(),FORECAST_FRAGMENT_TAG)
                    .commit();
        }
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
        String locationCity = Utility.getPreferredCity(this);
        if (!locationCity.equals(mLocationCity)){
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentByTag(FORECAST_FRAGMENT_TAG);
            ff.onLocationChange();
            mLocationCity = locationCity;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_map) {
            showMap();
            return true;
        }
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showMap() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String city = Utility.getPreferredCity(this);
        intent.setData(Uri.parse(String.format("geo:0,0?q=%s", city)));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Snackbar.make(getCurrentFocus(),"App map not found", Snackbar.LENGTH_LONG).show();
        }
    }

}


