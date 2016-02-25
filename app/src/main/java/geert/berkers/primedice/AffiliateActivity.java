package geert.berkers.primedice;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;

/**
 * Created by Geert on 24-2-2016.
 */
public class AffiliateActivity extends AppCompatActivity {

    private String link, refCommissionURL;
    private LayoutInflater factory;
    private View withdrawAmountView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.affiliate_layout);

        setTitle("Affiliate Overview");

        refCommissionURL = "https://api.primedice.com/api/affiliate/withdraw?access_token=";
        Bundle b = getIntent().getExtras();

        try {
            final User user = b.getParcelable("user");
            final String access_token = b.getString("access_token");
            if (user != null) {

                Button btnWithdraw = (Button) findViewById(R.id.btnWithdrawCommision);
                TextView txtAffiliateLinkInfo = (TextView) findViewById(R.id.affiliateLinkInfo);
                TextView txtAffiliateLink = (TextView) findViewById(R.id.affiliateLink);
                final TextView txtAvailableCommision = (TextView) findViewById(R.id.txtAvailableCommission);
                TextView txtTotalCommision = (TextView) findViewById(R.id.txtTotalCommission);
                TextView txtCommission = (TextView) findViewById(R.id.txtCommission);
                TextView txtRefferedUsers = (TextView) findViewById(R.id.txtRefferedUsers);

                link = "https://primedice.com/?ref=" + user.getUsername();

                String affiliateURL = "https://api.primedice.com/api/affiliates/1?access_token=";
                ;

                GetJSONResultFromURLTask getAffiliateInfoTask = new GetJSONResultFromURLTask();
                String result = getAffiliateInfoTask.execute(affiliateURL + access_token).get();

                JSONObject json = new JSONObject(result);
                JSONObject affiliate = json.getJSONObject("affiliate");

                String availableCommision = affiliate.getString("affiliate_balance");
                String totalCommision = affiliate.getString("affiliate_total");
                String commission = String.valueOf(affiliate.getInt("comission"));
                String commissionLevel = affiliate.getString("level");
                String commissionText = commission + "% (" + commissionLevel + ")";
                int referred = affiliate.getInt("referred");

                txtAvailableCommision.setText(satoshiStringToBTCString(availableCommision));
                txtTotalCommision.setText(satoshiStringToBTCString(totalCommision));
                txtCommission.setText(commissionText);
                txtRefferedUsers.setText(String.valueOf(referred));

                Log.w("AffiliateInfo", result);

                txtAffiliateLink.setText(link);

                txtAffiliateLink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        copyLink();
                    }
                });

                txtAffiliateLinkInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        copyLink();
                    }
                });

                btnWithdraw.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        factory = LayoutInflater.from(getApplicationContext());
                        if (withdrawAmountView != null) {
                            ViewGroup parent = (ViewGroup) withdrawAmountView.getParent();
                            if (parent != null) {
                                parent.removeView(withdrawAmountView);
                            }
                        }
                        try {
                            withdrawAmountView = factory.inflate(R.layout.withdraw_commission_layout, null);
                        } catch (InflateException e) {
                            e.printStackTrace();
                        }

                        final EditText withdrawAmount = (EditText) withdrawAmountView.findViewById(R.id.edAmount);

                        final Button btnMax = (Button) withdrawAmountView.findViewById(R.id.btnMaxWithdawAmount);

                        btnMax.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                withdrawAmount.setText(user.getBalance());

                            }
                        });

                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(v.getContext());
                        alertDialog.setTitle("WITHDRAW TO PRIMEDICE BALANCE");
                        alertDialog.setMessage("This amount will be moved to your Primedice balance.");
                        alertDialog.setView(withdrawAmountView);
                        alertDialog.setPositiveButton("WITHDRAW", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                try {
                                    String amount = withdrawAmount.getText().toString();

                                    amount = amount.replace(",", ".");
                                    Double amountToWithdraw = Double.valueOf(amount);

                                    double toSatoshiMultiplier = 100000000;
                                    double satoshiDouble = amountToWithdraw * toSatoshiMultiplier;

                                    // Dirty fix for rounding math problems
                                    if (satoshiDouble - Double.valueOf(satoshiDouble).intValue() >= 0.999) {
                                        satoshiDouble += 0.1;
                                    }

                                    int satoshiWithdrawAmount = (int) satoshiDouble;

                                    GetRefCommissionTask getRefCommissionTask = new GetRefCommissionTask();

                                    String result = getRefCommissionTask.execute(refCommissionURL + access_token, String.valueOf(satoshiWithdrawAmount)).get();

                                    JSONObject json = new JSONObject(result);
                                    txtAvailableCommision.setText(satoshiStringToBTCString(json.getString("affiliate_balance")));

                                    //TODO: update user

                                } catch (InterruptedException | ExecutionException | JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        alertDialog.show();
                    }
                });
            } else throw new Exception("User is null");

        } catch (Exception e) {
            Log.e("UserException", e.toString());

            this.finish();
            e.printStackTrace();

        }
    }

    private void copyLink() {
        ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Affiliate link Primedice", link);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this.getApplicationContext(), "Copied affiliate link!", Toast.LENGTH_SHORT).show();
    }

    public String satoshiStringToBTCString(String satoshiString) {

        int satoshi = Double.valueOf(satoshiString).intValue();

        DecimalFormat format = new DecimalFormat("0.00000000");

        String btcString = format.format((double) satoshi / 100000000);
        btcString = btcString.replace(",", ".");

        return btcString + " BTC";
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}