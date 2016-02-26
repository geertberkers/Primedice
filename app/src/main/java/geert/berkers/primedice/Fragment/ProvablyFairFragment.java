package geert.berkers.primedice.Fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import geert.berkers.primedice.Activity.BetInformationActivity;
import geert.berkers.primedice.Activity.MainActivity;
import geert.berkers.primedice.Data.Bet;
import geert.berkers.primedice.DataHandler.GetJSONResultFromURLTask;
import geert.berkers.primedice.DataHandler.PostToServerTask;
import geert.berkers.primedice.R;

/**
 * Primedice Application Created by Geert on 2-2-2016.
 */
public class ProvablyFairFragment extends Fragment {

    private View view;
    private MainActivity activity;

    private EditText edBetID;
    private View changeSeedView;
    private Button btnBetLookup;
    private String betLookupURL, changeSeedURL;
    private TextView txtRandomSeed, txtClientSeed, txtServeSeedHashed, txtBetsMade, txtPreviousClientSeed, txtPreviousServerSeedRevealed, txtPreviousServerSeedHashed;

    private static final String ALLOWED_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            this.view = inflater.inflate(R.layout.fragment_provably_fair, container, false);
            initControls();
            setInformation();
            setListeners();
        } else {
            setInformation();
        }
        return view;
    }

    private void initControls() {
        activity = (MainActivity) getActivity();

        edBetID = (EditText) view.findViewById(R.id.edBetID);
        btnBetLookup = (Button) view.findViewById(R.id.btnBetLookup);

        txtBetsMade = (TextView) view.findViewById(R.id.txtBetsMade);
        txtRandomSeed = (TextView) view.findViewById(R.id.txtChangeSeed);
        txtClientSeed = (TextView) view.findViewById(R.id.txtClientseed);
        txtServeSeedHashed = (TextView) view.findViewById(R.id.txtServerseed);
        txtPreviousClientSeed = (TextView) view.findViewById(R.id.txtPreviousClientseed);
        txtPreviousServerSeedHashed = (TextView) view.findViewById(R.id.txtPreviousServerseedHashed);
        txtPreviousServerSeedRevealed = (TextView) view.findViewById(R.id.txtPreviousServerseedRevealed);

        betLookupURL = "https://api.primedice.com/api/bets/";
        changeSeedURL = "https://api.primedice.com/api/seed?access_token=";

        txtRandomSeed.setPaintFlags(txtRandomSeed.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    private void setInformation() {
        txtClientSeed.setText(activity.getUser().getClient());
        txtServeSeedHashed.setText(activity.getUser().getServer());
        txtBetsMade.setText(String.valueOf(activity.getUser().getNonce()));
        txtPreviousClientSeed.setText(activity.getUser().getPreviousClient());
        txtPreviousServerSeedRevealed.setText(activity.getUser().getPreviousServer());
        txtPreviousServerSeedHashed.setText(activity.getUser().getPreviousServerHashed());
    }

    private void setListeners() {
        txtRandomSeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater factory = LayoutInflater.from(activity);

                if (changeSeedView != null) {
                    ViewGroup parent = (ViewGroup) changeSeedView.getParent();
                    if (parent != null) {
                        parent.removeView(changeSeedView);
                    }
                }
                try {
                    changeSeedView = factory.inflate(R.layout.changeseed_layout, null);
                } catch (InflateException e) {
                    e.printStackTrace();
                }

                final EditText edNewSeed = (EditText) changeSeedView.findViewById(R.id.edNewClientSeed);
                edNewSeed.setText(generateRandomSeed());

                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
                alertDialog.setTitle("CHANGE SEED");
                alertDialog.setView(changeSeedView);
                alertDialog.setPositiveButton("SET NEW SEED", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        String result;
                        String newClientSeed = edNewSeed.getText().toString();

                        try {
                            String urlParameters = "seed=" + URLEncoder.encode(newClientSeed, "UTF-8");

                            PostToServerTask changeSeedTask = new PostToServerTask();
                            result = changeSeedTask.execute((changeSeedURL + activity.getAccess_token()), urlParameters).get();

                            if (result == null) {
                                Toast.makeText(activity.getApplicationContext(), "To many requests for chancing seed!", Toast.LENGTH_LONG).show();
                            } else {
                                JSONObject jsonResult = new JSONObject(result);
                                JSONObject seeds = jsonResult.getJSONObject("seeds");

                                String client = seeds.getString("client");
                                String previous_server = seeds.getString("previous_server");
                                String previous_client = seeds.getString("previous_client");
                                String previous_server_hashed = seeds.getString("previous_server_hashed");
                                String next_seed = seeds.getString("next_seed");
                                String server = seeds.getString("server");

                                activity.getUser().updateSeeds(client, previous_server, previous_client, previous_server_hashed, next_seed, server);
                            }
                        } catch (InterruptedException | ExecutionException | JSONException | UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        setInformation();
                    }
                });
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertDialog.show();

            }
        });

        btnBetLookup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String betid = edBetID.getText().toString();
                betid = betid.replace(",", "");

                if (betid.length() >= 10) {

                    Bet bet = null;
                    try {
                        GetJSONResultFromURLTask getBetsTask = new GetJSONResultFromURLTask();
                        String result = getBetsTask.execute((betLookupURL + betid)).get();

                        JSONObject jsonBet = new JSONObject(result);
                        bet = new Bet(jsonBet.getJSONObject("bet"));

                    } catch (InterruptedException | ExecutionException | JSONException e) {
                        e.printStackTrace();
                    }

                    if (bet != null) {
                        Intent betInfoIntent = new Intent(v.getContext(), BetInformationActivity.class);
                        betInfoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        betInfoIntent.putExtra("bet", bet);
                        v.getContext().startActivity(betInfoIntent);
                    }
                } else {
                    Toast.makeText(activity.getApplicationContext(), "Bet #" + betid + " may not exist or might be archived.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private static String generateRandomSeed() {
        final Random random = new Random();
        final StringBuilder newSeed = new StringBuilder(30);

        for (int i = 0; i < 30; ++i) {
            newSeed.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        }

        return newSeed.toString();
    }
}
