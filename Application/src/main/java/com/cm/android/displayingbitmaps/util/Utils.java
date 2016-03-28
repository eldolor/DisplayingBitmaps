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

package com.cm.android.displayingbitmaps.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.os.StrictMode;

import com.cm.android.common.logger.Log;
import com.cm.android.displayingbitmaps.AnalyticsTrackers;
import com.cm.android.displayingbitmaps.ui.ImageDetailActivity;
import com.cm.android.displayingbitmaps.ui.ImageGridActivity;
import com.google.android.gms.analytics.Tracker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class containing some static utility methods.
 */
public class Utils {
    private Utils() {
    }

    ;
    private static final String TAG = Utils.class.getName();
    public static final int IO_BUFFER_SIZE = 8 * 1024;
    public static final String SHARED_PREF_NAME = "com.cm.android.displayingbitmaps";
    public static final int THUMBNAIL_SIZE = 320;


    @TargetApi(VERSION_CODES.HONEYCOMB)
    public static void enableStrictMode() {
        if (Utils.hasGingerbread()) {
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder =
                    new StrictMode.ThreadPolicy.Builder()
                            .detectAll()
                            .penaltyLog();
            StrictMode.VmPolicy.Builder vmPolicyBuilder =
                    new StrictMode.VmPolicy.Builder()
                            .detectAll()
                            .penaltyLog();

            if (Utils.hasHoneycomb()) {
                threadPolicyBuilder.penaltyFlashScreen();
                vmPolicyBuilder
                        .setClassInstanceLimit(ImageGridActivity.class, 1)
                        .setClassInstanceLimit(ImageDetailActivity.class, 1);
            }
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }

    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT;
    }

    /**
     * Recursively extracts all file names from a given path
     *
     * @param path
     * @return list of file names
     * @author anshu
     */
    public static List<String> listFiles(String path) {

        File root = new File(path);
        File[] list = root.listFiles();
        ArrayList<String> fileNames = new ArrayList<String>();

        if (list == null)
            return null;

        for (File f : list) {
            if (f.isDirectory()) {
                Utils.listFiles(f.getAbsolutePath());
            } else {
                fileNames.add(f.getName());
            }
        }
        return fileNames;
    }

    /**
     * Recursively extracts all file names with their absolute path from a given path
     *
     * @param path
     * @return list of file names with absolute path
     * @author anshu
     */
    public static List<String> listFileAbsolutePaths(String path) {

        File root = new File(path);
        File[] list = root.listFiles();
        ArrayList<String> fileNames = new ArrayList<String>();

        if (list == null)
            return null;

        for (File f : list) {
            if (f.isDirectory()) {
                Utils.listFiles(f.getAbsolutePath());
            } else {
                fileNames.add(f.getAbsolutePath());
            }
        }
        return fileNames;
    }

    /**
     * Recursively deletes all files
     *
     * @param f
     * @throws java.io.IOException
     */
    public static void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete())
            Log.d(TAG, "Failed to delete file: " + f);
    }


    /**
     * Get the external storage directory.
     *
     * @param context The context to use
     * @return The external storage dir
     */
    @TargetApi(VERSION_CODES.FROYO)
    public static File getExternalImageStorageDir(Context context) {
        File storageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/images");
        if (!storageDir.exists())
            storageDir.mkdirs();
        return storageDir;
    }

    /**
     * Get the external storage directory.
     *
     * @param context The context to use
     * @return The external storage dir
     */
    @TargetApi(VERSION_CODES.FROYO)
    public static File getExternalThumbnailStorageDir(Context context) {
        File storageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/thumbs");
        if (!storageDir.exists())
            storageDir.mkdirs();
        return storageDir;
    }

    /**
     * @return
     * @throws IOException
     * @author anshu
     */
    public static File createImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

//        File storageDir = Utils.getExternalImageStorageDir(context);

//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );
        String pathname = Utils.getExternalImageStorageDir(context).getAbsolutePath() + File.separator + imageFileName + ".jpg";
        File image = new File(pathname);

        return image;
    }

    /**
     *
     * @param urlString
     * @param outputStream
     * @return
     */
    public static boolean write(String urlString, OutputStream outputStream) {

        File image = new File(urlString);
        BufferedOutputStream out = null;
        BufferedInputStream in = null;

        try {
            //final URL url = new URL(urlString);
            in = new BufferedInputStream(new FileInputStream(image), IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);

            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;
        } catch (final IOException e) {
            Log.e(TAG, "Error in downloadBitmap - " + e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
            }
        }
        return false;
    }


    /**
     * Create a thumnail image
     *
     * @param context
     * @param imageAbsolutePath
     * @param thumbnailAbsolutePath
     * @param thumbnailWidth
     * @param thumbnailHeight
     * @return
     */
    public static boolean createThumbnail(Context context, String imageAbsolutePath, String thumbnailAbsolutePath, int thumbnailWidth, int thumbnailHeight) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {

            //bis = new BufferedInputStream(new FileInputStream(imageAbsolutePath), Utils.IO_BUFFER_SIZE);
            bos = new BufferedOutputStream(new FileOutputStream(thumbnailAbsolutePath), Utils.IO_BUFFER_SIZE);


//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inJustDecodeBounds = true;
//
//            options.inSampleSize = ImageResizer.calculateInSampleSize(options, thumbnailWidth, thumbnailHeight);
//
//            Bitmap decodedBitmap = BitmapFactory.decodeStream(bis, null, options);
//            if(decodedBitmap == null ){
//                Log.e("createThumbnail", "image data could not be decoded.");
//            }
//
//            Bitmap scaledBitmap = Bitmap.createScaledBitmap(decodedBitmap, thumbnailWidth, thumbnailHeight, false);
//            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//            Bitmap bMap = ThumbnailUtils.extractThumbnail(imageBitmap, thumbnailWidth, thumbnailHeight);
//            bMap.compress(Bitmap.CompressFormat.JPEG, 100, bos);

            Bitmap thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imageAbsolutePath), thumbnailWidth, thumbnailHeight);
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bos);

            return true;
        } catch (Exception e) {
            Log.e("createThumbnail", e.toString());
            return false;
        } finally {
            try {
                if (bos != null) {
                    bos.flush();
                    bos.close();
                    //imageBitmap.recycle();
                }
                if (bis != null) {
                    bis.close();
                }
            } catch (final Exception e) {
            }
        }


    }

    /**
     * Return GA Tracker
     *
     * @param context
     * @param target
     * @return
     */
    public static Tracker getAnalyticsTracker(Context context, AnalyticsTrackers.Target target){
        try{
            AnalyticsTrackers.initialize(context);
        }catch (Exception e) {}

        return AnalyticsTrackers.getInstance().get(target);

    }
}
