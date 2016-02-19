package geert.berkers.primedice;

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

import java.util.concurrent.ExecutionException;

/**
 * Primedice Application Created by Geert on 2-2-2016.
 */
public class FaucetFragment extends Fragment {

    private View view;
    private MainActivity activity;

    private TextView txtBalance;
    private WebView faucetWebView;
    private Button btnClaimFaucet, btnCancelFaucet;
    private String mime, encoding, html, note, claimFaucetURL;

    String response;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            this.view = inflater.inflate(R.layout.fragment_faucet, container, false);

            initControls();
        }

        refreshFaucet();
        txtBalance.setText(activity.getUser().getBalance());

        return view;
    }


    @SuppressLint("SetJavaScriptEnabled")
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void initControls() {
        activity = (MainActivity) getActivity();

        txtBalance = (TextView) view.findViewById(R.id.txtBalance);

        faucetWebView = (WebView) view.findViewById(R.id.faucetWebView);

        btnClaimFaucet = (Button) view.findViewById(R.id.btnClaimFaucet);
        btnCancelFaucet = (Button) view.findViewById(R.id.btnCancelFaucet);

        faucetWebView.getSettings().setJavaScriptEnabled(true);

        mime = "text/html";
        encoding = "utf-8";
        claimFaucetURL = "https://api.primedice.com/api/faucet?access_token=";
        note = "<p><b>Note:</b> You must have over 10 BTC wagered before your faucet starts increasing <br><br> The amount you can claim from the faucet is dependant on your level. Your level and more information about it can be viewed under the profile tab.</p>";
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
                txtBalance.setText(activity.getUser().getBalance());
                refreshFaucet();
            }
        });
    }

    public void refreshFaucet() {
        faucetWebView.loadDataWithBaseURL("https://primedice.com/play", html, mime, encoding, null);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void onClaimFaucet() {
        if (txtBalance.getText().toString().equals("0.00000000")) {
            faucetWebView.evaluateJavascript("grecaptcha.getResponse()", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {

                    Log.w("FaucetResult", value);
                    String responseForAPI = value.replace("%22", "");
                    responseForAPI = responseForAPI.replace("\"", "");
                    Log.w("ResponseForAPI", responseForAPI);

                    try {
                        ClaimFaucetTask changeSeedTask = new ClaimFaucetTask();
                        String result = changeSeedTask.execute((claimFaucetURL + activity.getAccess_token()), responseForAPI).get();

                        if (result != null) {
                            Log.w("ClaimResult", result);

                            JSONObject jsonResult = new JSONObject(result);
                            activity.getUser().updateUserBalance(jsonResult.getString("balance"));

                            txtBalance.setText(activity.getUser().getBalance());
                            Toast.makeText(activity.getApplicationContext(), "Faucet claimed!", Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(activity.getApplicationContext(), "CAPTCHA incorrect or claimed to early!", Toast.LENGTH_LONG).show();
                        }

                        refreshFaucet();

                    } catch (InterruptedException | ExecutionException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Toast.makeText(activity.getApplicationContext(), "Balance is not null!", Toast.LENGTH_LONG).show();
        }
    }
}