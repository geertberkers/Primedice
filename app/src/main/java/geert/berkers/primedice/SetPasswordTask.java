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
class SetPasswordTask extends AsyncTask<String, Void, String> {

    private HttpURLConnection connection;

    @Override
    protected String doInBackground(String... params) {
        return setPassword(params[0], params[1], params[2], params[3]);
    }

    private String setPassword(String URL, String currentPassword, String newPassword, String optionalEmail) {
        String result = null;

        try {
            URL url = new URL(URL);

            String urlParameters = "password=" + URLEncoder.encode(newPassword, "UTF-8");

            if (currentPassword != null && currentPassword.length() != 0) {
                urlParameters = urlParameters + "&oldPassword=" + URLEncoder.encode(currentPassword, "UTF-8");
            }

            if (optionalEmail != null && optionalEmail.length() != 0) {
                urlParameters = urlParameters + "&email=" + URLEncoder.encode(optionalEmail, "UTF-8");
            }

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
