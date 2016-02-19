package geert.berkers.primedice;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.DecimalFormat;

/**
 * Primedice Application Created by Geert on 26-1-2016.
 */
public class Bet implements Parcelable {

    private long id;
    private boolean win, jackpot;
    private int amount, profit, nonce;
    private double target, roll, multiplier;
    private String player, playerID, condition, timestamp, client,server;

    public Bet(JSONObject bet) {
        try {
            //Log.i("Bet", bet.toString());
            this.id = bet.getLong("id");
            this.player = bet.getString("player");
            this.playerID = bet.getString("player_id");
            this.amount = bet.getInt("amount");
            this.target = bet.getDouble("target");
            this.profit = (int) bet.getDouble("profit");
            try {
                this.win = bet.getBoolean("win");
            }
            catch (Exception ex) {
                this.win = bet.getInt("win") != 0;
            }
            this.condition = bet.getString("condition");
            this.roll = bet.getDouble("roll");
            this.nonce = bet.getInt("nonce");
            this.client = bet.getString("client");
            this.multiplier = bet.getDouble("multiplier");
            this.timestamp = bet.getString("timestamp");
            try {
                this.jackpot = bet.getBoolean("jackpot");
            }
            catch (Exception ex) {
                this.jackpot = bet.getInt("jackpot") != 0;
            }
            this.server = bet.getString("server");
        } catch (JSONException ex) {
            Log.e("JSON Error", ex.toString());
        }
    }

    // Create bet from parcel
    private Bet(Parcel read){
        this.id = read.readLong();
        this.player = read.readString();
        this.playerID = read.readString();
        this.amount = read.readInt();
        this.target = read.readDouble();
        this.profit = read.readInt();
        win = read.readString().equals("Y");
        this.condition = read.readString();
        this.roll = read.readDouble();
        this.nonce = read.readInt();
        this.client = read.readString();
        this.multiplier = read.readDouble();
        this.timestamp = read.readString();
        jackpot = read.readString().equals("Y");
        this.server = read.readString();
    }

    // Create from parcel
    public static final Parcelable.Creator<Bet> CREATOR = new Parcelable.Creator<Bet>(){

        @Override
        public Bet createFromParcel(Parcel source) {
            return new Bet(source);
        }

        @Override
        public Bet[] newArray(int size) {
            return new Bet[size];
        }
    };

    public Bet(Long ID, String user, int profit, String win, int amount, String condition, Double target, Double roll, Integer nonce, String client, Double multiplier, String timestamp, String server) {
        this.id = ID;
        this.player = user;
        this.win = win.equals("Y");
        this.profit = profit;
        this.amount = amount;
        this.condition = condition;
        this.target = target;
        this.roll = roll;
        this.nonce = nonce;
        this.client = client;
        this.multiplier = multiplier;
        this.server = server;
        this.timestamp = timestamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override // Save the object as parcel
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeLong(id);
        arg0.writeString(player);
        arg0.writeString(playerID);
        arg0.writeInt(amount);
        arg0.writeDouble(target);
        arg0.writeInt(profit);
        if(win)  { arg0.writeString("Y"); } else { arg0.writeString("N"); }
        arg0.writeString(condition);
        arg0.writeDouble(roll);
        arg0.writeInt(nonce);
        arg0.writeString(client);
        arg0.writeDouble(multiplier);
        arg0.writeString(timestamp);
        if(jackpot)  { arg0.writeString("Y"); } else { arg0.writeString("N"); }
        arg0.writeString(server);
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
        return satToBTC(profit);
    }

    public boolean getWinOrLose() {
        return win;
    }

    public double getRoll() {
        return roll;
    }

    public String getServerseed() {
        return server;
    }

    public String getClientseed() {
        return client;
    }

    public String getTimeOfBet() {
        return timestamp.substring(0, 10) + " " + timestamp.substring(11, 19) + " GMT";
    }

    public int getAmount(){
        return amount;
    }

    public String getWagered() {
        return satToBTC(amount);
    }

    public String getPayout() {
        DecimalFormat format = new DecimalFormat("0.000");
        String payoutString = format.format(multiplier);
        payoutString = payoutString.replace(",",".");
        payoutString = payoutString.substring(0,5);
        return  payoutString;
    }

    public String getBetGame() {
        return condition + String.valueOf(target);
    }

    private String satToBTC(double satoshi) {
        DecimalFormat format = new DecimalFormat("0.00000000");

        String resultBTC = format.format(satoshi / 100000000);
        resultBTC = resultBTC.replace(",",".");

        return resultBTC;
    }

    public String getNonce() {
        return String.valueOf(nonce);
    }
}
