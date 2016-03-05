package geert.berkers.primedice.Fragment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import geert.berkers.primedice.Activity.MainActivity;
import geert.berkers.primedice.Data.URL;
import geert.berkers.primedice.DataHandler.PostToServerTask;
import geert.berkers.primedice.R;

/**
 * Primedice Application Created by Geert on 2-2-2016.
 */
public class FaucetFragment extends Fragment {

    private View view;
    private MainActivity activity;

    private Date nextClaim;
    private TextView txtBalance;
    private WebView faucetWebView;
    private String mime, encoding, html;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            this.view = inflater.inflate(R.layout.fragment_faucet, container, false);

            initControls();
        }

        refreshFaucet();
        txtBalance.setText(activity.getUser().getBalanceAsString());

        return view;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void initControls() {
        activity = (MainActivity) getActivity();

        txtBalance = (TextView) view.findViewById(R.id.txtBalance);

        faucetWebView = (WebView) view.findViewById(R.id.faucetWebView);

        Button btnClaimFaucet = (Button) view.findViewById(R.id.btnClaimFaucet);
        Button btnCancelFaucet = (Button) view.findViewById(R.id.btnCancelFaucet);

        faucetWebView.getSettings().setJavaScriptEnabled(true);

        mime = "text/html";
        encoding = "utf-8";
        String note = "<p><b>Note:</b> You must have over 10 BTC wagered before your faucet starts increasing <br><br> The amount you can claim from the faucet is dependant on your level. Your level and more information about it can be viewed under the profile tab.</p>";
        html = "<html><head><title>Collect Faucet</title></head>"
                + "<body><h2 align=\"center\">Receive a free amount to play with!</h2>"
                + "<div class=\"g-recaptcha\" data-sitekey=\"6LeX6AcTAAAAAMwAON0oEyRDoTbusREfJa2vxDMh\" align=\"center\"></div>"
                + note
                + "<script src=\"https://www.google.com/recaptcha/api.js\" asyncr></script></body></html>";


        btnClaimFaucet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClaimFaucet();
            }
        });

        btnCancelFaucet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtBalance.setText(activity.getUser().getBalanceAsString());
                refreshFaucet();
            }
        });
    }

    private void refreshFaucet() {
        faucetWebView.loadDataWithBaseURL(URL.PRIMEDICE, html, mime, encoding, null);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void onClaimFaucet() {
        if (txtBalance.getText().toString().equals("0.00000000")) {
            faucetWebView.evaluateJavascript("grecaptcha.getResponse()", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {

                    String responseForAPI = value.replace("%22", "");
                    responseForAPI = responseForAPI.replace("\"", "");
                    Log.i("ResponseForAPI", responseForAPI);

                    if (nextClaim != null) {
                        Date now = new Date();
                        if (nextClaim.before(now)) {

                            claimFaucet(responseForAPI);

                        } else {
                            long difference = nextClaim.getTime() - now.getTime();
                            Toast.makeText(activity.getApplicationContext(), "Wait " + (difference / 1000) + " seconds!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        claimFaucet(responseForAPI);
                    }
                }
            });
        } else {
            MainActivity.showNotification(true, "Balance is not null!", 5);
        }
    }

    @SuppressWarnings("deprecation")
    private void claimFaucet(String responseForAPI) {
        try {
            PostToServerTask claimFaucetTask = new PostToServerTask();
            String urlParameters = "response=" + URLEncoder.encode(responseForAPI, "UTF-8");

            String result = claimFaucetTask.execute((URL.CLAIM_FAUCET + activity.getAccess_token()), urlParameters).get();

            if (result != null) {
                Log.i("CLAIM_FAUCET_RESULT", result);

                JSONObject jsonResult = new JSONObject(result);
                activity.getUser().updateUserBalance(jsonResult.getString("balance"));

                txtBalance.setText(activity.getUser().getBalanceAsString());
                Toast.makeText(activity.getApplicationContext(), "Faucet claimed!", Toast.LENGTH_LONG).show();

                nextClaim = new Date();
                nextClaim.setMinutes(nextClaim.getMinutes() + 3);

            } else {
                Toast.makeText(activity.getApplicationContext(), "Try again. Please be patient.", Toast.LENGTH_LONG).show();
            }

            refreshFaucet();

        } catch (InterruptedException | ExecutionException | JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    public void updateBalance(final String balance) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtBalance.setText(balance);
            }
        });
    }
}