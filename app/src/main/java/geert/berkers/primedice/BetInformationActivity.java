package geert.berkers.primedice;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Primedice Application Created by Geert on 27-1-2016.
 */
public class BetInformationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bet_information);

        TextView txtUsername = (TextView) findViewById(R.id.txtUsername);
        TextView txtRoll = (TextView) findViewById(R.id.txtRoll);
        TextView txtServerseed = (TextView) findViewById(R.id.txtServerseed);
        TextView txtClientseed = (TextView) findViewById(R.id.txtClientseed);
        TextView txtTime = (TextView) findViewById(R.id.txtTimeOfBet);
        TextView txtWagered = (TextView) findViewById(R.id.txtWagered);
        TextView txtPayout = (TextView) findViewById(R.id.txtPayout);
        TextView txtBetGame = (TextView) findViewById(R.id.txtBetGame);
        TextView txtBetProtif = (TextView) findViewById(R.id.txtBetProfit);

  /*
        //TO-DO: Check if you want to keep it
        TextView txtVerification = (TextView) findViewById(R.id.txtVerificate);

        txtVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://primedice.com/verify"));
                startActivity(browserIntent);
            }
        });
*/

        Bundle b = getIntent().getExtras();
        try {
            Bet bet = b.getParcelable("bet");

            if (bet != null) {
                txtUsername.setText(bet.getPlayer());
                txtRoll.setText(String.valueOf(bet.getRoll()));
                txtServerseed.setText(bet.getServerseed());
                String clientSeed= bet.getClientseed() + "-" + bet.getNonce();
                txtClientseed.setText(clientSeed);
                txtTime.setText(bet.getTimeOfBet());
                txtWagered.setText(bet.getWagered());
                String payoutString = bet.getPayout() + "x";
                txtPayout.setText(payoutString);
                txtBetGame.setText(bet.getBetGame());
                txtBetProtif.setText(bet.getProfit());

                boolean win = bet.getWinOrLose();
                if (!win) {
                    txtBetProtif.setTextColor(Color.RED);
                } else {
                    txtBetProtif.setTextColor(Color.argb(255, 0, 100, 0));
                }

                setTitle("BET #" + bet.getIDString().replace(",","") + " INFO");
            } else throw new Exception("Bet is null");
        } catch (Exception ex) {
            Toast.makeText(this.getApplicationContext(),"Bet not found!", Toast.LENGTH_SHORT).show();
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
}
