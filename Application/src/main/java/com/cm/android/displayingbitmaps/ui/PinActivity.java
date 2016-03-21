package com.cm.android.displayingbitmaps.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.cm.android.displayingbitmaps.R;
import com.cm.android.displayingbitmaps.provider.AuthProvider;

import me.philio.pinentry.PinEntryView;

/**
 * Created by anshugaind on 3/17/16.
 */
public class PinActivity extends Activity{

    private PinEntryView pinEntryView;
    private Vibrator mVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        setContentView(R.layout.pin_entry);
        ImageView pinCancel = (ImageView) findViewById(R.id.pin_cancel);
        pinCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PinActivity.this.finish();
            }
        });
        pinEntryView = (PinEntryView) findViewById(R.id.pin_entry_simple);
        pinEntryView.setOnPinEnteredListener(new PinEntryView.OnPinEnteredListener() {
            @Override
            public void onPinEntered(String pin) {
                //Toast.makeText(PinActivity.this, "Pin entered: " + pin, Toast.LENGTH_LONG).show();
                AuthProvider.getAuthProvider().authenticate(PinActivity.this, pin);
                if (AuthProvider.getAuthProvider().isIsLoggedIn()) {
                    finish();
                } else{
                    mVibrator.vibrate(500);
                    Toast.makeText(PinActivity.this, "Please enter a valid PIN", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}


