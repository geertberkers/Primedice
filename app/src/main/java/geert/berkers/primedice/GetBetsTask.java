package geert.berkers.primedice;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Geert on 23-1-2016.
 */
public class GetBetsTask extends AsyncTask<String, Void, ArrayList<Bet>> {

    HttpURLConnection connection;

    @Override
    protected ArrayList<Bet> doInBackground(String... params) {
        return getBalance(params[0]);
    }

    private ArrayList<Bet> getBalance(String betsInfoURL) {
        String betsJSON = "NoBets";

        try {
            URL url = new URL(betsInfoURL);

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

            betsJSON = response.toString();
            Log.i("bets", betsJSON);

        } catch (Exception ex) {

            Log.e("Exception", ex.toString());
            ex.printStackTrace();

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }

        return getBetsList(betsJSON, betsInfoURL);
    }

    public ArrayList<Bet> getBetsList(String betsJSON, String betsInfoURL) {
        String getThese;
        if (betsInfoURL.contains("highrollers")) {
            getThese = "highrollers";
        } else if (betsInfoURL.contains("mybets")) {
            getThese = "mybets";
        } else{
            getThese = "bets";
        }

        ArrayList<Bet> betArrayList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(betsJSON);

            JSONArray jsonArray = jsonObject.getJSONArray(getThese);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonBet = jsonArray.getJSONObject(i);
                betArrayList.add(new Bet(jsonBet));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return betArrayList;
    }
}
