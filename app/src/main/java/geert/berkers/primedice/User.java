package geert.berkers.primedice;

import android.os.Parcel;
import android.os.Parcelable;
import java.text.DecimalFormat;
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

    // Create User
    public User(int userID, String username, double balance, boolean password, String adress, Date registered, boolean otp_enabled, boolean email_enabled, boolean address_enabled, int wagered, double profit, int bets, int wins, int losses, int win_risk, int lose_risk, int messages, int reffered, double affiliate_total, int nonce, String client, String previous_server, String previous_client, String previous_server_hashed, String next_seed, String server, String otp_token, String otp_qr) {
        this.userID = userID;
        this.username = username;
        this.balance = balance;
        this.password = password;
        this.adress = adress;
        //TODO: FIX DATE
        //this.registered = registered;
        this.otp_enabled = otp_enabled;
        this.email_enabled = email_enabled;
        this.address_enabled = address_enabled;
        this.wagered = wagered;
        this.profit = profit;
        this.bets = bets;
        this.wins = wins;
        this.losses = losses;
        this.win_risk = win_risk;
        this.lose_risk = lose_risk;
        this.messages = messages;
        this.reffered = reffered;
        this.affiliate_total = affiliate_total;
        this.nonce = nonce;
        this.client = client;
        this.previous_server = previous_server;
        this.previous_client = previous_client;
        this.previous_server_hashed = previous_server_hashed;
        this.next_seed = next_seed;
        this.server = server;
        this.otp_token = otp_token;
        this.otp_qr = otp_qr;
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
        //TODO: CHECK FOR DOTS IN STEAD OF COMMA
        //TODO: CHECK FORMAT WITH 0 ON THE END
        DecimalFormat format = new DecimalFormat("#.########");
        return "Username: " + username + "\nBalance: " + format.format(balance / 100000000) + " BTC\nWagered: " + format.format((((double) wagered / 100000000)))+" BTC";
    }

    /* JSON EXAMPLE FROM GETTING USER INFORMATION
    {
        "user":{
                "id":1,
                "userid":"990311",
                "username":"GeertBerkers",
                "balance":1844.327,
                "password":true,
                "address":"1Fxt3cxzjGNAmovb8Hpwve9664G1kS3fzL",
                "registered":"2015-11-11T10:10:36.825Z",
                "otp_enabled":false,
                "email_enabled":true,
                "address_enabled":false,
                "wagered":311163139,
                "profit":-10125759.675,
                "bets":26890,
                "wins":13227,
               "losses":13663,
                "win_risk":51704011,
                "lose_risk":51321007,
                "messages":5310,
                "referred":1,
                "affiliate_total":334348.218,
                "nonce":6794,
                "client":"PnU7n2PAJBxUnrqO8JkRqKlxFbU4LY",
                "previous_server":"4acd34f371ff366f6ccd3117240f5c9768a44b5c61250e7a432f1ab0808a0963",
                "previous_client":"YQgYMd0yQJ0g95sRwMzwxzlObF9Eze",
                "previous_server_hashed":"bc753fdaaeee2b452df4d33f1d1e8fb3ee1f87279244937abf2dd19ef9d3dd34",
                "next_seed":"d31e2062d593c6309284c5cd361bfbe7f61bde71e7b0706f477e896b48226aeb",
                "server":"89d6a5185b5df01ff07510e693b93a4a779c01912d29f485dfdbe80c0796d511",
                "otp_token":"HBIF2WBFNNXEI7JYGVAVM23BJFRX2VS5",
                "otp_qr":"https://chart.googleapis.com/chart?chs=166x166&chld=L|0&cht=qr&chl=otpauth://totp/Primedice%2FGeertBerkers%3Fsecret=HBIF2WBFNNXEI7JYGVAVM23BJFRX2VS5"
            },
        "meta":{
                "blocked":false
            }
        }
    */
}
