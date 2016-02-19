package geert.berkers.primedice;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Primedice Application Created by Geert on 26-1-2016.
 */
class PlaceBetTask extends AsyncTask<String, Void, String> {

    private HttpURLConnection connection;

    @Override
    protected String doInBackground(String... params) {
        return placeBetUpdateUser(params[0], params[1], params[2], params[3]);
    }

    private String placeBetUpdateUser(String betURL, String amount, String target, String condition) {
        String betResult = null;

        try {
            URL url = new URL(betURL);

            String urlParameters =
                    "amount=" + URLEncoder.encode(amount, "UTF-8") +
                            "&target=" + URLEncoder.encode(target, "UTF-8") +
                            "&condition=" + URLEncoder.encode(condition, "UTF-8");

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.connect();

            // Send request to site
            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.writeBytes(urlParameters);
            dataOutputStream.flush();
            dataOutputStream.close();

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

            betResult = response.toString();
            Log.i("response", betResult);

        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
        return betResult;
    }
}
