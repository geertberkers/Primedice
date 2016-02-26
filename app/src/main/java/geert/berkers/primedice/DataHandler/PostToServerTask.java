package geert.berkers.primedice.DataHandler;

import android.os.AsyncTask;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Primedice Application Created by Geert on 26-1-2016.
 */
public class PostToServerTask extends AsyncTask<String, Void, String> {

    private Connection connection;

    @Override
    protected String doInBackground(String... params) {
        return postToServer(params[0], params[1]);
    }

    private String postToServer(String betURL, String urlParameters) {
        String result = "NoResult";

        try {
            URL url = new URL(betURL);

            connection = new Connection(url, "POST");

            if(urlParameters != null) {
                connection.setRequestParameters(urlParameters);
            }

            result = connection.getResult();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            connection.closeConnection();
        }

        return result;
    }
}
