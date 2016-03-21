/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cm.android.displayingbitmaps.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cm.android.displayingbitmaps.AnalyticsTrackers;
import com.cm.android.displayingbitmaps.R;
import com.cm.android.displayingbitmaps.provider.Images;
import com.cm.android.displayingbitmaps.util.AsyncTask;
import com.cm.android.displayingbitmaps.util.ImageFetcher;
import com.cm.android.displayingbitmaps.util.ImageWorker;
import com.cm.android.displayingbitmaps.util.Utils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;

/**
 * This fragment will populate the children of the ViewPager from {@link ImageDetailActivity}.
 */
public class ImageDetailFragment extends Fragment implements ImageWorker.OnImageLoadedListener, ImageWorker.OnImageDeletedListener {
    private static final String IMAGE_DATA_EXTRA = "extra_image_data";
    private static final String IMAGE_THUMBNAIL_DATA_EXTRA = "extra_thumbnail_image_data";
    private String mImageUrl;
    private String mThumbnailUrl;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private ImageFetcher mImageFetcher;
    //@author anshu
    private Tracker mTracker;
    private AdView mAdView;


    /**
     * Factory method to generate a new instance of the fragment given an image number.
     *
     * @param imageUrl The image url to load
     * @return A new instance of ImageDetailFragment with imageNum extras
     */
    public static ImageDetailFragment newInstance(String imageUrl, String imageThumbnailUrl) {
        final ImageDetailFragment f = new ImageDetailFragment();

        final Bundle args = new Bundle();
        args.putString(IMAGE_DATA_EXTRA, imageUrl);
        args.putString(IMAGE_THUMBNAIL_DATA_EXTRA, imageThumbnailUrl);
        f.setArguments(args);

        return f;
    }

    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageDetailFragment() {
    }

    /**
     * Populate image using a url from extras, use the convenience factory method
     * {@link ImageDetailFragment#newInstance(String, String)} to create this fragment.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageUrl = getArguments() != null ? getArguments().getString(IMAGE_DATA_EXTRA) : null;
        mThumbnailUrl = getArguments() != null ? getArguments().getString(IMAGE_THUMBNAIL_DATA_EXTRA) : null;

        mTracker = Utils.getAnalyticsTracker(getActivity(), AnalyticsTrackers.Target.APP);
        //required to set the options menu
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate and locate the main ImageView
        final View v = inflater.inflate(R.layout.image_detail_fragment, container, false);
        mImageView = (ImageView) v.findViewById(R.id.imageView);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressbar);
        //@author anshu:
        //Admob
        mAdView = (AdView) v.findViewById(R.id.adView);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                //GA
                mTracker.setScreenName("ImageDetail");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                //AdMob
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            }
        });

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Use the parent activity to load the image asynchronously into the ImageView (so a single
        // cache can be used over all pages in the ViewPager
        if (ImageDetailActivity.class.isInstance(getActivity())) {
            mImageFetcher = ((ImageDetailActivity) getActivity()).getImageFetcher();
            mImageFetcher.loadImage(mImageUrl, mImageView, this);
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mImageView != null) {
            // Cancel any pending image work
            ImageWorker.cancelWork(mImageView);
            mImageView.setImageDrawable(null);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.image_detail_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
            case R.id.delete_menu:
                deleteImage();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ImageDetailFragment.this.getActivity());

        builder
                .setMessage("Delete image?")
                .setPositiveButton("Delete now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Yes-code
                        mProgressBar.setVisibility(View.VISIBLE);
                        String[] params = {mImageUrl, mThumbnailUrl};
                        new DeleteImageAsyncTask().execute(params);
                        getActivity().finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    @Override
    public void onImageLoaded(boolean success) {
        // Set loading spinner to gone once image has loaded. Cloud also show
        // an error view here if needed.
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onImageDeleted(boolean success) {
        //getActivity().getSupportFragmentManager().popBackStack();
        mProgressBar.setVisibility(View.GONE);
        getActivity().finish();
    }


    /**
     * @author anshu
     */
    private class DeleteImageAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String mImageUrl = strings[0];
            String mImageThumbnailUrl = strings[1];

            try {
                File file = new File(mImageUrl);
                if (file.delete()) {
                    mImageFetcher.deleteImage(mImageUrl, ImageDetailFragment.this);
                    mImageFetcher.deleteImage(mImageThumbnailUrl, ImageDetailFragment.this);
                } else {
                    Toast.makeText(getActivity(), "Unable to delete",
                            Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {

            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }


}
