package geert.berkers.primedice.DataHandler;

import android.os.AsyncTask;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Primedice Application Created by Geert on 23-1-2016.
 */
public class GetJSONResultFromURLTask extends AsyncTask<String, Void, String> {

    private Connection connection;

    @Override
    protected String doInBackground(String... params) {
        return getJSONResult(params[0]);
    }

    private String getJSONResult(String URL) {
        String result = "NoResult";

        try {
            URL url = new URL(URL);

            connection = new Connection(url, "GET");

            result = connection.getResult();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            connection.closeConnection();
        }

        return result;
    }
}
