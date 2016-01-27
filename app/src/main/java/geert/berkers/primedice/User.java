package geert.berkers.primedice;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Geert on 23-1-2016.
 */
public class User implements Parcelable {

    Date registered;
    double balance, profit, affiliate_total;
    boolean password, otp_enabled, email_enabled, address_enabled;
    int userID, wagered, bets, wins, losses, win_risk, lose_risk, messages, reffered, nonce;
    String username, adress, client, previous_server, previous_client, previous_server_hashed, next_seed, server, otp_token, otp_qr;

    // Create User from JSON
    public User(String jsonUserString)
    {
        try {
            JSONObject json = new JSONObject(jsonUserString);

            JSONObject jsonUser = json.getJSONObject("user");

            this.userID = jsonUser.getInt("userid");
            this.username = jsonUser.getString("username");
            this.balance = jsonUser.getDouble("balance");
            this.password = jsonUser.getBoolean("password");
            this.adress = jsonUser.getString("address");
            //TODO: Fix date
            //String registeredString = jsonUser.getString("registered");
            //Date registered = parseDate(registeredString);
            this.otp_enabled = jsonUser.getBoolean("otp_enabled");
            this.email_enabled = jsonUser.getBoolean("email_enabled");
            this.address_enabled = jsonUser.getBoolean("address_enabled");
            this.wagered = jsonUser.getInt("wagered");
            this.profit = jsonUser.getDouble("profit");
            this.bets = jsonUser.getInt("bets");
            this.wins = jsonUser.getInt("wins");
            this.losses = jsonUser.getInt("losses");
            this.win_risk = jsonUser.getInt("win_risk");
            this.lose_risk = jsonUser.getInt("lose_risk");
            this.messages = jsonUser.getInt("messages");
            this.reffered = jsonUser.getInt("referred");
            this.affiliate_total = jsonUser.getInt("affiliate_total");
            this.nonce = jsonUser.getInt("nonce");
            this.client = jsonUser.getString("client");
            this.previous_server = jsonUser.getString("previous_server");
            this.previous_client = jsonUser.getString("previous_client");
            this.previous_server_hashed = jsonUser.getString("previous_server_hashed");
            this.next_seed = jsonUser.getString("next_seed");
            this.server = jsonUser.getString("server");
            this.otp_token = jsonUser.getString("otp_token");
            this.otp_qr = jsonUser.getString("otp_qr");

            Log.i("User", this.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    // Create user from parcel
    public User(Parcel read){
        this.userID = read.readInt();
        this.username = read.readString();
        this.balance = read.readDouble();
        if(read.readString().equals("Y")) { password = true; } else { password = false;}
        this.adress = read.readString();
        if(read.readString().equals("Y")) { otp_enabled = true; } else { otp_enabled = false;}
        if(read.readString().equals("Y")) { email_enabled = true; } else { email_enabled = false;}
        if(read.readString().equals("Y")) { address_enabled = true; } else { address_enabled = false;}
        this.wagered = read.readInt();
        this.profit = read.readDouble();
        this.bets = read.readInt();
        this.wins = read.readInt();
        this.losses = read.readInt();
        this.win_risk = read.readInt();
        this.lose_risk = read.readInt();
        this.messages = read.readInt();
        this.reffered = read.readInt();
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

        //TODO: FIX DATE
        /*
        try {
            this.registered = DateFormat.getDateInstance().parse(read.readString());
        }
        catch (ParseException ex)
        {
            Log.e("ParseError", ex.toString());
        }
        */
    }

    // Create from parcel
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>(){

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

    // Save the object as parcel
    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeInt(userID);
        arg0.writeString(username);
        arg0.writeDouble(balance);
        if(password)  { arg0.writeString("Y"); } else { arg0.writeString("N"); }
        arg0.writeString(adress);
        if(otp_enabled)  { arg0.writeString("Y"); } else { arg0.writeString("N"); }
        if(email_enabled)  { arg0.writeString("Y"); } else { arg0.writeString("N"); }
        if(address_enabled)  { arg0.writeString("Y"); } else { arg0.writeString("N"); }
        arg0.writeInt(wagered);
        arg0.writeDouble(profit);
        arg0.writeInt(bets);
        arg0.writeInt(wins);
        arg0.writeInt(losses );
        arg0.writeInt(win_risk );
        arg0.writeInt(lose_risk );
        arg0.writeInt(messages);
        arg0.writeInt(reffered );
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

        //TODO: FIX DATE (PARSE EXCEPTION)
        /*
        try{
        arg0.writeString(DateFormat.getDateInstance().format(registered));
    }catch (Exception ex)
        {
            ex.printStackTrace();
        }
        */
    }

    @Override
    public String toString() {
        DecimalFormat format = new DecimalFormat("0.00000000");

        String balanceString = format.format(balance / 100000000);
        balanceString = balanceString.replace(",",".");

        return "Username: " + username + "\nBalance: " + balanceString + " BTC\nWagered: " + format.format((((double) wagered / 100000000)))+" BTC";
    }

    // Parse date-text to date-object
    // TODO: FIX MAKING A DATE FROM STRING. THIS STILL CRASHES
    public static Date parseDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss'Z'");
        try {
            return format.parse(dateString);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public boolean updateUser(JSONObject jsonUser) {
        try{
            this.userID = jsonUser.getInt("userid");
            this.username = jsonUser.getString("username");
            this.balance = jsonUser.getDouble("balance");
            this.password = jsonUser.getBoolean("password");
            this.adress = jsonUser.getString("address");
            //TODO: Fix date
            //String registeredString = jsonUser.getString("registered");
            //Date registered = parseDate(registeredString);
            this.otp_enabled = jsonUser.getBoolean("otp_enabled");
            this.email_enabled = jsonUser.getBoolean("email_enabled");
            this.address_enabled = jsonUser.getBoolean("address_enabled");
            this.wagered = jsonUser.getInt("wagered");
            this.profit = jsonUser.getDouble("profit");
            this.bets = jsonUser.getInt("bets");
            this.wins = jsonUser.getInt("wins");
            this.losses = jsonUser.getInt("losses");
            this.win_risk = jsonUser.getInt("win_risk");
            this.lose_risk = jsonUser.getInt("lose_risk");
            this.messages = jsonUser.getInt("messages");
            this.reffered = jsonUser.getInt("referred");
            this.affiliate_total = jsonUser.getInt("affiliate_total");
            this.nonce = jsonUser.getInt("nonce");
            this.client = jsonUser.getString("client");
            this.previous_server = jsonUser.getString("previous_server");
            this.previous_client = jsonUser.getString("previous_client");
            this.previous_server_hashed = jsonUser.getString("previous_server_hashed");
            this.next_seed = jsonUser.getString("next_seed");
            this.server = jsonUser.getString("server");
            return true;
        }
        catch (Exception ex){
            Log.e("Error", ex.toString());
            return false;
        }
    }
}
