package geert.berkers.primedice;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Primedice Application Created by Geert on 22-1-2016.
 */
public class LogOutTask extends AsyncTask<String, String, String> {

    HttpURLConnection connection;

    @Override
    protected String doInBackground(String... params) {
        return getLogOutResult(params[0]);
    }

    public String getLogOutResult(String logoutURL) {

        String logOutResult = "NoResult";

        try {
            URL url = new URL(logoutURL);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("POST");
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

            logOutResult = response.toString();
            Log.i("response", logOutResult);

        } catch (Exception ex) {

            Log.e("Exception", ex.toString());
            ex.printStackTrace();

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }

        return logOutResult;
    }
}
