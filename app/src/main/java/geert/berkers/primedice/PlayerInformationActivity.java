package geert.berkers.primedice;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

/**
 * Primedice Application Created by Geert on 27-1-2016.
 */
public class PlayerInformationActivity extends AppCompatActivity {

    private User player;
    private String playerName;
    private TextView txtWagered,txtProfit,txtBetsMade,txtMessages,txtRegistered,txtWins,txtLosses,txtLuck,txtPowerLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_information);

        txtWagered = (TextView) findViewById(R.id.txtWagered);
        txtProfit = (TextView) findViewById(R.id.txtProfit);
        txtBetsMade = (TextView) findViewById(R.id.txtBetsMade);
        txtMessages = (TextView) findViewById(R.id.txtMessages);
        txtRegistered = (TextView) findViewById(R.id.txtDateJoined);
        txtWins = (TextView) findViewById(R.id.txtWins);
        txtLosses = (TextView) findViewById(R.id.txtLosses);
        txtLuck = (TextView) findViewById(R.id.txtLuck);
        txtPowerLevel = (TextView) findViewById(R.id.txtPower);

        Bundle b = getIntent().getExtras();
        try {
            playerName = b.getString("playerName");

            player = getUser();

            if (player != null) {
                txtWagered.setText(player.getWagered());
                txtProfit.setText(player.getProfit());
                txtBetsMade.setText(player.getBets());
                txtMessages.setText(player.getMessages());
                txtRegistered.setText(player.getRegistered());
                txtWins.setText(player.getWins());
                txtLosses.setText(player.getLosses());
                txtLuck.setText(player.getLuck());
                txtPowerLevel.setText("Not implemented yet");

                setTitle(player.username.toUpperCase() + "'S INFO");
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

    public User getUser() {

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
