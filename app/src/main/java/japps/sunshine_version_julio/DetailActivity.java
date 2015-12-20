package japps.sunshine_version_julio;

/**
 * Created by Julio on 13-11-2015.
 */


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            DetailFragment df = new DetailFragment();
            df.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, df)
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

    public static class DetailFragment extends Fragment {


        public String getValor() {
            return getArguments().getString(getString(R.string.detail_fragment_key));
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            if (container == null) {
                return null;
            }
            String forecast = getValor();
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            TextView textView = (TextView) rootView.findViewById(R.id.newActivityText);
            textView.setTextSize(30);
            textView.setText(forecast);

            return rootView;
        }
    }

}
