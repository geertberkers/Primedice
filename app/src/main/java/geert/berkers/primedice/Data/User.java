package geert.berkers.primedice.Data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Primedice Application Created by Geert on 23-1-2016.
 */
public class User implements Parcelable {

    private Long wagered;
    private Date registered;
    private double balance, profit, affiliate_total;
    private boolean password, otp_enabled, email_enabled, address_enabled;
    private SimpleDateFormat registeredDateFormat = new SimpleDateFormat("MMM dd yyyy");
    private int userID, bets, wins, losses, win_risk, lose_risk, messages, referred, nonce;
    private String username, address/*, registered*/, client, previous_server, previous_client, previous_server_hashed, next_seed, server, otp_token, otp_qr;

    // Create User from JSON
    public User(String jsonUserString) {
        try {
            JSONObject json = new JSONObject(jsonUserString);
            JSONObject jsonUser = json.getJSONObject("user");

            this.userID = jsonUser.getInt("userid");
            this.username = jsonUser.getString("username");
            this.balance = jsonUser.getDouble("balance");
            this.password = jsonUser.getBoolean("password");
            this.address = jsonUser.getString("address");
            setRegisteredFromDateFormat(jsonUser.getString("registered"));
            this.otp_enabled = jsonUser.getBoolean("otp_enabled");
            this.email_enabled = jsonUser.getBoolean("email_enabled");
            this.address_enabled = jsonUser.getBoolean("address_enabled");
            this.wagered = jsonUser.getLong("wagered");
            this.profit = jsonUser.getDouble("profit");
            this.bets = jsonUser.getInt("bets");
            this.wins = jsonUser.getInt("wins");
            this.losses = jsonUser.getInt("losses");
            this.win_risk = jsonUser.getInt("win_risk");
            this.lose_risk = jsonUser.getInt("lose_risk");
            this.messages = jsonUser.getInt("messages");
            this.referred = jsonUser.getInt("referred");
            this.affiliate_total = jsonUser.getInt("affiliate_total");
            this.nonce = jsonUser.getInt("nonce");
            this.client = jsonUser.getString("client");
            this.previous_server = jsonUser.getString("previous_server");
            this.previous_client = jsonUser.getString("previous_client");
            this.previous_server_hashed = jsonUser.getString("previous_server_hashed");
            this.next_seed = jsonUser.getString("next_seed");
            this.server = jsonUser.getString("server");
            try {
                this.otp_token = jsonUser.getString("otp_token");
                this.otp_qr = jsonUser.getString("otp_qr");
            } catch (Exception ex) {
                Log.i("2FA OTP","Two Factor Authentication already set.");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Create User to show
    public User(JSONObject jsonUser) {
        try {
            this.userID = jsonUser.getInt("userid");
            this.username = jsonUser.getString("username");
            setRegisteredFromDateFormat(jsonUser.getString("registered"));
            this.wagered = jsonUser.getLong("wagered");
            this.profit = jsonUser.getDouble("profit");
            this.bets = jsonUser.getInt("bets");
            this.wins = jsonUser.getInt("wins");
            this.losses = jsonUser.getInt("losses");
            this.win_risk = jsonUser.getInt("win_risk");
            this.lose_risk = jsonUser.getInt("lose_risk");
            this.messages = jsonUser.getInt("messages");
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    // Create user from parcel
    private User(Parcel read) {
        this.userID = read.readInt();
        this.username = read.readString();
        this.balance = read.readDouble();
        password = read.readString().equals("Y");
        this.address = read.readString();

        try {
            this.registered = registeredDateFormat.parse(read.readString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        otp_enabled = read.readString().equals("Y");
        email_enabled = read.readString().equals("Y");
        address_enabled = read.readString().equals("Y");
        this.wagered = read.readLong();
        this.profit = read.readDouble();
        this.bets = read.readInt();
        this.wins = read.readInt();
        this.losses = read.readInt();
        this.win_risk = read.readInt();
        this.lose_risk = read.readInt();
        this.messages = read.readInt();
        this.referred = read.readInt();
        this.affiliate_total = read.readDouble();
        this.nonce = read.readInt();
        this.client = read.readString();
        this.previous_server = read.readString();
        this.previous_client = read.readString();
        this.previous_server_hashed = read.readString();
        this.next_seed = read.readString();
        this.server = read.readString();
        this.otp_token = read.readString();
        this.otp_qr = read.readString();
    }

    // Create from parcel
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {

        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override // Save the object as parcel
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeInt(userID);
        arg0.writeString(username);
        arg0.writeDouble(balance);
        if (password) {
            arg0.writeString("Y");
        } else {
            arg0.writeString("N");
        }
        arg0.writeString(address);
        arg0.writeString(getRegistered());
        if (otp_enabled) {
            arg0.writeString("Y");
        } else {
            arg0.writeString("N");
        }
        if (email_enabled) {
            arg0.writeString("Y");
        } else {
            arg0.writeString("N");
        }
        if (address_enabled) {
            arg0.writeString("Y");
        } else {
            arg0.writeString("N");
        }
        arg0.writeLong(wagered);
        arg0.writeDouble(profit);
        arg0.writeInt(bets);
        arg0.writeInt(wins);
        arg0.writeInt(losses);
        arg0.writeInt(win_risk);
        arg0.writeInt(lose_risk);
        arg0.writeInt(messages);
        arg0.writeInt(referred);
        arg0.writeDouble(affiliate_total);
        arg0.writeInt(nonce);
        arg0.writeString(client);
        arg0.writeString(previous_server);
        arg0.writeString(previous_client);
        arg0.writeString(previous_server_hashed);
        arg0.writeString(next_seed);
        arg0.writeString(server);
        arg0.writeString(otp_token);
        arg0.writeString(otp_qr);
    }

    public void updateUser(JSONObject jsonUser) {
        try {
            this.userID = jsonUser.getInt("userid");
            this.username = jsonUser.getString("username");
            this.balance = jsonUser.getDouble("balance");
            this.password = jsonUser.getBoolean("password");
            this.address = jsonUser.getString("address");
            setRegisteredFromDateFormat(jsonUser.getString("registered"));
            this.otp_enabled = jsonUser.getBoolean("otp_enabled");
            this.email_enabled = jsonUser.getBoolean("email_enabled");
            this.address_enabled = jsonUser.getBoolean("address_enabled");
            this.wagered = jsonUser.getLong("wagered");
            this.profit = jsonUser.getDouble("profit");
            this.bets = jsonUser.getInt("bets");
            this.wins = jsonUser.getInt("wins");
            this.losses = jsonUser.getInt("losses");
            this.win_risk = jsonUser.getInt("win_risk");
            this.lose_risk = jsonUser.getInt("lose_risk");
            this.messages = jsonUser.getInt("messages");
            this.referred = jsonUser.getInt("referred");
            this.affiliate_total = jsonUser.getInt("affiliate_total");
            this.nonce = jsonUser.getInt("nonce");
            this.client = jsonUser.getString("client");
            this.previous_server = jsonUser.getString("previous_server");
            this.previous_client = jsonUser.getString("previous_client");
            this.previous_server_hashed = jsonUser.getString("previous_server_hashed");
            this.next_seed = jsonUser.getString("next_seed");
            this.server = jsonUser.getString("server");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "Username: " + username + "\nWagered: " + satToBTC(Double.valueOf(wagered)) + " BTC";
    }

    public long getWageredAsLong(){
        return wagered;
    }

    public double getProfit(){
        return profit;
    }

    public int getBets(){
        return bets;
    }

    public String getWageredAsString() {
        return satToBTC(Double.valueOf(wagered));
    }

    public String getProfitAsString() {
        return satToBTC((int) profit);
    }

    public double getBalance(){
        return balance;
    }

    public String getAddress(){
        return address;
    }

    public void setBalance(double newBalance){
        this.balance = newBalance;
    }
    public void setAddress(String newAddress){
        this.address = newAddress;
    }

    public String getBalanceAsString() {
        // Use cast int to round it down
        int value = (int) balance;
        return satToBTC(value);
    }

    public String getBetsAsString() {
        return intToDottedString(bets);
    }

    public String getMessages() {
        return intToDottedString(messages);
    }

    public String getRegistered() {
        return registeredDateFormat.format(registered);
    }

    public String getWins() {
        return intToDottedString(wins);
    }

    public String getLosses() {
        return intToDottedString(losses);
    }

    private String intToDottedString(int integer) {
        String id = String.valueOf(integer);

        StringBuilder str = new StringBuilder(id);
        int idx = str.length() - 3;

        while (idx > 0) {
            str.insert(idx, ",");
            idx = idx - 3;
        }

        return str.toString();
    }

    private String satToBTC(double satoshi) {
        DecimalFormat format = new DecimalFormat("0.00000000");

        String resultBTC = format.format(satoshi / 100000000);
        resultBTC = resultBTC.replace(",", ".");

        return resultBTC;
    }

    public String getLuck() {
        DecimalFormat format = new DecimalFormat("0.00");

        double luckValue = (double) win_risk / lose_risk * 100;

        String luck = format.format(luckValue);
        luck = luck.replace(",", ".");
        luck = luck + "%";

        return luck;
    }

    public String getUsername() {
        return username;
    }

    public void updateSeeds(String client, String previous_server, String previous_client, String previous_server_hashed, String next_seed, String server) {
        this.nonce = 0;
        this.client = client;
        this.previous_server = previous_server;
        this.previous_client = previous_client;
        this.previous_server_hashed = previous_server_hashed;
        this.next_seed = next_seed;
        this.server = server;
    }

    public void updateUserBalance(String balance) {
        this.balance = Double.valueOf(balance);
    }

    public void setAddressEnabled() {
        this.address_enabled = true;
    }

    public boolean getAddressEnabled() {
        return address_enabled;
    }

    public boolean getEmailEnabled() {
        return email_enabled;
    }

    public boolean getPasswordSet() {
        return password;
    }

    public boolean getOTPEnabled() {
        return otp_enabled;
    }

    public String getOTPQR() {
        return otp_qr;
    }

    public void setPasswordSet() {
        this.password = true;
    }

    public void setEmailEnabled() {
        this.email_enabled = true;
    }

    public String getOTPToken() {
        return otp_token;
    }

    public void setOTPEnabled() {
        this.otp_enabled = true;
    }

    public String getClient() {
        return client;
    }

    public String getServer() {
        return server;
    }

    public int getNonce() {
        return nonce;
    }

    public String getPreviousClient() {
        return previous_client;
    }

    public String getPreviousServer() {
        return previous_server;
    }

    public String getPreviousServerHashed() {
        return previous_server_hashed;
    }

    public void setRegisteredFromDateFormat(String timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC")); //2015-11-25T11:52:01.617Z

        try {
            registered = formatter.parse(timestamp);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
    }
}
