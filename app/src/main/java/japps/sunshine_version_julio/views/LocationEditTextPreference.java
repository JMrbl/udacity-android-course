package japps.sunshine_version_julio.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.EditTextPreference;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;

import japps.sunshine_version_julio.R;

/**
 * Created by Julio on 16/7/2017.
 */

public class LocationEditTextPreference extends EditTextPreference {
    private static final String LOG_TAG = LocationEditTextPreference.class.getSimpleName();
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LocationEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        handleAttribute(context, attrs);
    }

    public LocationEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        handleAttribute(context, attrs);
    }

    public LocationEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        handleAttribute(context, attrs);
    }

    public LocationEditTextPreference(Context context) {
        super(context);
    }

    private void handleAttribute(Context context, AttributeSet attrs){
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.LocationEditTextPreference,
                0, 0);
        Log.d(LOG_TAG, String.valueOf(typedArray.getInteger(R.styleable.LocationEditTextPreference_minLength, 0)));
    }
}
