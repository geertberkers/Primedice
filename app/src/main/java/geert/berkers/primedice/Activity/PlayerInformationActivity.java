package geert.berkers.primedice.Activity;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

import geert.berkers.primedice.DataHandler.GetJSONResultFromURLTask;
import geert.berkers.primedice.R;
import geert.berkers.primedice.Data.User;

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
        //TextView txtPowerLevel = (TextView) findViewById(R.id.txtPower);

        Bundle b = getIntent().getExtras();
        try {
            playerName = b.getString("playerName");

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
                //txtPowerLevel.setText("Not implemented yet");

                setTitle(player.getUsername().toUpperCase() + "'S INFO");
            } else throw new Exception("Player is null");
        } catch (Exception ex) {
            Toast.makeText(this.getApplicationContext(), "Player not found!", Toast.LENGTH_SHORT).show();
            Log.e("NoBetFound", "Didn't find a bet!");
            Log.e("NoBetFound", ex.toString());

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

        User user;
        String userResult = "NoResult";

        GetJSONResultFromURLTask userTask = new GetJSONResultFromURLTask();

        try {
            userResult = userTask.execute("https://api.primedice.com/api/users/" + playerName).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if (userResult == null || userResult.equals("NoResult")) {
            user = null;
        } else {
            user = new User(false, userResult);
        }

        return user;
    }
}
