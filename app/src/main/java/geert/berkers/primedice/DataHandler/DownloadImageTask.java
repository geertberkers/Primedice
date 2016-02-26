package geert.berkers.primedice.DataHandler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;

/**
 * Primedice Application Created by Geert on 29-1-2016.
 */

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    protected Bitmap doInBackground(String... urls) {
        String url = urls[0];
        Bitmap downloadedImage = null;
        try {
            InputStream in = new java.net.URL(url).openStream();
            downloadedImage = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return downloadedImage;
    }
}
