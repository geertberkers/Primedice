package geert.berkers.primedice.DataHandler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;

/**
 * Primedice Application Created by Geert on 29-1-2016.
 */

public class DownloadImageTask extends AsyncTask<Object, Void, Bitmap> {

    String url = null;
    ImageView imageView = null;

    @Override
    protected Bitmap doInBackground(Object... objects) {
        this.url = (String) objects[0];
        this.imageView = (ImageView) objects[1];

        return downloadBitmap(url);
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        imageView.setImageBitmap(result);
    }

    private Bitmap downloadBitmap(String url) {
        Bitmap downloadedImage = null;
        try {
            InputStream in = new URL(url).openStream();
            downloadedImage = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return downloadedImage;
    }
}
/*
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    protected Bitmap doInBackground(String... urls) {
        String url = urls[0];
        Bitmap downloadedImage = null;
        try {
            InputStream in = new java.net.URL(url).openStream();
            downloadedImage = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return downloadedImage;
    }
}
*/