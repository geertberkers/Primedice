package geert.berkers.primedice.DataHandler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;

import java.net.MalformedURLException;
import java.net.URL;

import geert.berkers.primedice.Activity.MainActivity;
import geert.berkers.primedice.Data.URLS;
import geert.berkers.primedice.Data.User;

/**
 * Primedice Application Created by Geert on 30-3-2016.
 */
public class LoginTask extends AsyncTask<String, Void, User> {

    private Activity activity;
    private String access_token;
    private ProgressDialog progressDialog;
    private APIConnector APIConnector;

    public LoginTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected User doInBackground(String... params) {
        this.access_token = params[0];

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String result = getUserJSON(URLS.USER + access_token);

        if(result != null) {
            return new User(result);
        }
        else{
            return null;
        }
    }

    private String getUserJSON(String url) {
        String result = null;

        try {
            URL URL = new URL(url);
            APIConnector = new APIConnector(URL, "GET");
            result = APIConnector.getResult();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            APIConnector.closeConnection();
        }

        return result;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(activity, "Logging in", "Please be patient while logging in.");
    }

    @Override
    protected void onPostExecute(User user) {
        if (user != null) {
            Intent mainActivity = new Intent(activity, MainActivity.class);
            mainActivity.putExtra("userParcelable", user);
            mainActivity.putExtra("access_token", access_token);
            activity.startActivity(mainActivity);
            activity.finish();
        }
        progressDialog.dismiss();

    }
}
