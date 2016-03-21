package com.cm.android.displayingbitmaps.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.Toast;

import com.cm.android.displayingbitmaps.R;
import com.cm.android.displayingbitmaps.provider.AuthProvider;

import me.philio.pinentry.PinEntryView;

/**
 * Created by anshugaind on 3/17/16.
 */
public class ChangePinActivity extends Activity{

    private PinEntryView pinEntryView;
    private Vibrator mVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        setContentView(R.layout.pin_entry);
        pinEntryView = (PinEntryView) findViewById(R.id.pin_entry_simple);
        pinEntryView.setOnPinEnteredListener(new PinEntryView.OnPinEnteredListener() {
            @Override
            public void onPinEntered(String pin) {
                Toast.makeText(ChangePinActivity.this, "Pin updated: " + pin, Toast.LENGTH_LONG).show();
                mVibrator.vibrate(500);
                AuthProvider.getAuthProvider().createPin(ChangePinActivity.this, pin);
                    finish();
            }
        });
    }
}


