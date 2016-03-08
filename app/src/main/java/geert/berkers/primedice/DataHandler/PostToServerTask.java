package geert.berkers.primedice.DataHandler;

import android.os.AsyncTask;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Primedice Application Created by Geert on 26-1-2016.
 */
public class PostToServerTask extends AsyncTask<String, Void, String> {

    private APIConnector APIConnector;

    @Override
    protected String doInBackground(String... params) {
        return postToServer(params[0], params[1]);
    }

    private String postToServer(String betURL, String urlParameters) {
        String result = null;

        try {
            URL url = new URL(betURL);

            APIConnector = new APIConnector(url, "POST");

            if(urlParameters != null) {
                APIConnector.setRequestParameters(urlParameters);
            }

            result = APIConnector.getResult();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            APIConnector.closeConnection();
        }

        return result;
    }
}
