package geert.berkers.primedice;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Primedice Application Created by Geert on 23-1-2016.
 */
public class GetJSONResultFromURLTask extends AsyncTask<String, Void, String> {

    HttpURLConnection connection;

    @Override
    protected String doInBackground(String... params) {
        return getJSONResult(params[0]);
    }

    private String getJSONResult(String URL){
        String result = "NoResult";

        try {
            URL url = new URL(URL);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();

            // Get Response from site
            InputStream inputStream = connection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            StringBuilder response = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }

            bufferedReader.close();

            result = response.toString();
            Log.i("Result", result);

        } catch (Exception ex) {

            Log.e("Exception", ex.toString());
            ex.printStackTrace();

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }

        return result;
    }
}
