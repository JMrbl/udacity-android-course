package japps.sunshine_version_julio;

/**
 * Created by Julio on 13-11-2015.
 */


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

        private ShareActionProvider shareActionProvider;
        private final String HASH_TAG = " #SunshineApp";
        private final String LOG_TAG = DetailActivity.class.getSimpleName();
        private String forecastValue;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            if (container == null) {
                return null;
            }
            forecastValue = getValor();
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            TextView textView = (TextView) rootView.findViewById(R.id.newActivityText);
            textView.setTextSize(30);
            textView.setText(forecastValue);

            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

            inflater.inflate(R.menu.fragment_share, menu);
            MenuItem menuItem = menu.findItem(R.id.action_share);
            shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            Intent intent = new Intent(Intent.ACTION_SEND);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            } else {
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            }
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, forecastValue + HASH_TAG);
            setShareIntent(intent);
            super.onCreateOptionsMenu(menu, inflater);
        }

        public String getValor() {
            return getArguments().getString(getString(R.string.detail_fragment_key));
        }

        private void setShareIntent(Intent intent) {
            if (shareActionProvider != null) {
                shareActionProvider.setShareIntent(intent);
            } else {
                Log.d(LOG_TAG, "Share Action Provider is null?");
            }
        }
    }

}
