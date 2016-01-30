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
public class PlaceWithdrawalTask extends AsyncTask<String, Void, String> {

    HttpURLConnection connection;

    @Override
    protected String doInBackground(String... params) {
        return placeWithdrawal(params[0], params[1], params[2]);
    }

    public String placeWithdrawal(String withdrawalURL, String amount, String address) {
        String result = "NoResult";

        try {
            URL url = new URL(withdrawalURL);

            String urlParameters =
                    "amount=" + URLEncoder.encode(amount, "UTF-8") +
                            "&address=" + URLEncoder.encode(address, "UTF-8");

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

            result = response.toString();
            Log.i("response", result);

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
