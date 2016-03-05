package geert.berkers.primedice.DataHandler;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Primedice Application Created by Geert on 26-2-2016.
 */
class Connection {

    private HttpURLConnection connection;

    public Connection(URL url, String requestMethod) {
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod(requestMethod);
            //connection.setDoInput(true);
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setRequestParameters(String urlParameters) {
        try {
            // Send request to site
            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.writeBytes(urlParameters);
            dataOutputStream.flush();
            dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getResult() {
        try {
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

            String result = response.toString();
            Log.i("RESPONSE_SERVER", result);

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void closeConnection() {
        if (connection != null) {
            connection.disconnect();
        }
    }
}