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
 * Created by Geert on 23-1-2016.
 */
public class GetUserTask extends AsyncTask<String, Void, String> {

    HttpURLConnection connection;

    @Override
    protected String doInBackground(String... params) {
        return getBalance(params[0]);
    }

    private String getBalance(String userURL){
        String balance = "NoBalance";

        try {
            URL url = new URL(userURL);

            /*
            String urlParameters =
                    "acces_token=" + URLEncoder.encode(acces_token, "UTF-8") +
                            "&password=" + URLEncoder.encode(password, "UTF-8");
            */

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

            balance = response.toString();
            Log.i("balance", balance);

        } catch (Exception ex) {

            Log.e("Exception", ex.toString());
            ex.printStackTrace();

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }

        return balance;
    }
}
