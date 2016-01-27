package geert.berkers.primedice;

import android.text.style.TtsSpan;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Date;

/**
 * Created by Geert on 26-1-2016.
 */
public class Bet {

    private long id;
    private String player;
    private String playerID;
    private int amount;
    private double target;
    private int profit;
    private boolean win;
    private String condition;
    private double roll;
    private int nonce;
    private String client;
    private double multiplier;
    private Date timestamp;
    private boolean jackpot;
    private String server;
    private boolean revealed;

    public Bet(JSONObject bet) {
        try {
            Log.i("Bet", bet.toString());
            this.id = bet.getLong("id");
            this.player = bet.getString("player");
            this.playerID = bet.getString("player_id");
            this.amount = bet.getInt("amount");
            this.target = bet.getDouble("target");
            this.profit = bet.getInt("profit");
            this.win = bet.getBoolean("win");
            this.condition = bet.getString("condition");
            this.roll = bet.getDouble("roll");
            this.nonce = bet.getInt("nonce");
            this.client = bet.getString("client");
            this.multiplier = bet.getDouble("multiplier");
            String timestampString = bet.getString("timestamp");
            this.jackpot = bet.getBoolean("jackpot");
            this.server = bet.getString("server");
            //this.revealed = bet.getBoolean("revealed");
        } catch (JSONException ex) {
            Log.e("JSON Error", ex.toString());
        }
    }

    public String getIDString(){
        String id = String.valueOf(this.id);

        StringBuilder str = new StringBuilder(id);
        int idx = str.length() - 3;

        while (idx > 0)
        {
            str.insert(idx, ",");
            idx = idx - 3;
        }

        return str.toString();
    }
    public String getPlayer() {
        return this.player;
    }

    public String getProfit() {
        DecimalFormat format = new DecimalFormat("0.00000000");

        String profitString = format.format((double)profit / 100000000);
        profitString = profitString.replace(",", ".");

        return profitString;
    }

    public boolean getWinOrLose() {
        return win;
    }
}
