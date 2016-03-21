package com.cm.android.displayingbitmaps.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.cm.android.displayingbitmaps.AnalyticsTrackers;
import com.cm.android.displayingbitmaps.R;
import com.cm.android.displayingbitmaps.provider.AuthProvider;
import com.cm.android.displayingbitmaps.util.Utils;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class IntroActivity extends Activity {

    private static int AUTHENTICATE = 1213793;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        setup();
    }

    private void setup() {
        ImageView next = (ImageView) findViewById(R.id.next_image);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IntroActivity.this, PinActivity.class);
                startActivityForResult(intent, AUTHENTICATE);
            }
        });
        mTracker = Utils.getAnalyticsTracker(this, AnalyticsTrackers.Target.APP);

        this.runOnUiThread(new Runnable() {
            public void run() {
                String creditCardIconSet = IntroActivity.this.getSharedPreferences(Utils.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("CREDIT_CARD_ICON_SET", "N");
                if (creditCardIconSet.equals("N")) {
                    FileOutputStream out1 = null;
                    FileOutputStream out2 = null;
                    Bitmap bm1 =null;
                    Bitmap bm2 = null;
                    try {
                        String imageAbsolutePath = Utils.getExternalImageStorageDir(IntroActivity.this).getAbsolutePath();
                        String thumbnailAbsolutePath = Utils.getExternalThumbnailStorageDir(IntroActivity.this).getAbsolutePath();

                        bm1 = BitmapFactory.decodeResource(getResources(), R.drawable.credit_card_icon);
                        File file1 = new File(imageAbsolutePath, "credit_card_icon.png");
                        out1 = new FileOutputStream(file1);
                        bm1.compress(Bitmap.CompressFormat.PNG, 100, out1);

                        bm2 = BitmapFactory.decodeResource(getResources(), R.drawable.credit_card_icon);
                        File file2 = new File(thumbnailAbsolutePath, "credit_card_icon.png");
                        out2 = new FileOutputStream(file2);
                        bm2.compress(Bitmap.CompressFormat.PNG, 100, out2);

                        //finally
                        IntroActivity.this.getSharedPreferences(Utils.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit().putString("CREDIT_CARD_ICON_SET", "Y").commit();
                    } catch (Exception e) {
                        //do nothing
                    } finally {
                        try {
                            out1.flush();
                            out1.close();
                            bm1.recycle();
                            out2.flush();
                            out2.close();
                            bm2.recycle();
                        } catch (Exception e) {//do nothing}
                        }
                    }
                }

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        mTracker.setScreenName("Introduction");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTHENTICATE) {
            if (AuthProvider.getAuthProvider().isIsLoggedIn()) {
                Intent intent = new Intent(IntroActivity.this, ImageGridActivity.class);
                startActivity(intent);
                finish();
            }

        }
    }
}
