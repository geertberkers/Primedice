package geert.berkers.primedice.DataHandler;

import android.os.AsyncTask;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Primedice Application Created by Geert on 23-1-2016.
 */
public class GetFromServerTask extends AsyncTask<String, Void, String> {

    private APIConnector APIConnector;

    @Override
    protected String doInBackground(String... params) {
        return getJSONResult(params[0]);
    }

    private String getJSONResult(String URL) {
        String result = null;

        try {
            URL url = new URL(URL);
            APIConnector = new APIConnector(url, "GET");
            result = APIConnector.getResult();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            APIConnector.closeConnection();
        }

        return result;
    }
}
