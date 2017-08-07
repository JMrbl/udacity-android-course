package japps.sunshine_version_julio.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import japps.sunshine_version_julio.R;

/**
 * Created by Julio on 16/7/2017.
 */

public class LocationEditTextPreference extends EditTextPreference {
    private static final String LOG_TAG = LocationEditTextPreference.class.getSimpleName();
    private static final int MIN_LENGHT = 3;
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

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        EditText editText = getEditText();
        editText.addTextChangedListener(onPreferenceChangeListener());
    }

    private TextWatcher onPreferenceChangeListener() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                handleDialog(getDialog(), editable);
            }
        };
    }

    private void handleDialog(Dialog dialog, Editable editable) {
        if (dialog instanceof AlertDialog){
            AlertDialog alertDialog = (AlertDialog) dialog;
            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            handlePositiveButtonAvailability(positiveButton, editable);
        }
    }

    private void handlePositiveButtonAvailability(Button positiveButton, Editable editable) {
        if (editable.length() < MIN_LENGHT){
            positiveButton.setEnabled(false);
        } else {
            positiveButton.setEnabled(true);
        }
    }
}
