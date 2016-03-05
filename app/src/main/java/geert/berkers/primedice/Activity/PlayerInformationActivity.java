package geert.berkers.primedice.Activity;

import android.os.Bundle;
import android.view.View;
import android.app.Activity;
import android.widget.Toast;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONObject;
import org.json.JSONException;

import java.util.concurrent.ExecutionException;

import geert.berkers.primedice.R;
import geert.berkers.primedice.Data.URL;
import geert.berkers.primedice.Data.User;
import geert.berkers.primedice.DataHandler.GetJSONResultFromURLTask;


/**
 * Primedice Application Created by Geert on 27-1-2016.
 */
public class PlayerInformationActivity extends AppCompatActivity {

    private String playerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_information_layout);

        TextView txtWagered = (TextView) findViewById(R.id.txtWagered);
        TextView txtProfit = (TextView) findViewById(R.id.txtProfit);
        TextView txtBetsMade = (TextView) findViewById(R.id.txtBetsMade);
        TextView txtMessages = (TextView) findViewById(R.id.txtMessages);
        TextView txtRegistered = (TextView) findViewById(R.id.txtDateJoined);
        TextView txtWins = (TextView) findViewById(R.id.txtWins);
        TextView txtLosses = (TextView) findViewById(R.id.txtLosses);
        TextView txtLuck = (TextView) findViewById(R.id.txtLuck);

        Button btnPM = (Button) findViewById(R.id.btnPM);
        Button btnTip = (Button) findViewById(R.id.btnTip);

        Bundle b = getIntent().getExtras();
        try {
            playerName = b.getString("playerName");

            boolean ownAccount = MainActivity.getUserName().equals(playerName);

            User player = getUser();

            if (player != null) {
                txtWagered.setText(player.getWageredAsString());
                txtProfit.setText(player.getProfitAsString());
                txtBetsMade.setText(player.getBetsAsString());
                txtMessages.setText(player.getMessages());
                txtRegistered.setText(player.getRegistered());
                txtWins.setText(player.getWins());
                txtLosses.setText(player.getLosses());
                txtLuck.setText(player.getLuck());

                if (!ownAccount) {
                    final Activity activity = this;

                    btnPM.setVisibility(View.VISIBLE);
                    btnTip.setVisibility(View.VISIBLE);

                    btnPM.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MainActivity.sendPM(activity, playerName);
                        }
                    });

                    // Notifications not visible in this activity.
                    btnTip.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MainActivity.tipUser(activity, playerName);
                        }
                    });
                }
                else{
                    btnPM.setVisibility(View.GONE);
                    btnTip.setVisibility(View.GONE);
                }
                setTitle(player.getUsername().toUpperCase() + "'S INFO");
            } else throw new Exception("Player is null");
        } catch (Exception ex) {
            Toast.makeText(this.getApplicationContext(), "Player not found!", Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
            this.finish();
        }
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

    private User getUser() {
        String userResult = "NoResult";

        GetJSONResultFromURLTask userTask = new GetJSONResultFromURLTask();

        try {
            userResult = userTask.execute(URL.GET_USER + playerName).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        if (userResult != null) {
            if (!userResult.equals("NoResult")) {

                try {
                    JSONObject json = new JSONObject(userResult);
                    JSONObject jsonUser = json.getJSONObject("user");

                    return new User(jsonUser);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}