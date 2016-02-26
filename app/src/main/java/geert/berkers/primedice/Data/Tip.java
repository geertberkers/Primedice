package geert.berkers.primedice.Data;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

/**
 * Primedice Application Created by Geert on 20-2-2016.
 */
public class Tip implements Parcelable {

    private String amount;
    private String username;
    private String receiver;
    private String timestamp;

    public String getAmount() {
        double satoshi = Long.valueOf(amount);

        DecimalFormat format = new DecimalFormat("0.00000000");

        String resultBTC = format.format(satoshi / 100000000);
        resultBTC = resultBTC.replace(",", ".");

        return resultBTC;
    }

    public String getUsername() {
        return username;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getTimestamp() {
        return timestamp.substring(0,10);
    }

    public Tip(JSONObject tip){
        try {
            this.amount = tip.getString("amount");
            this.username = tip.getString("username");
            this.receiver = tip.getString("receiver");
            this.timestamp = tip.getString("timestamp");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    // Create Payment from parcel
    private Tip(Parcel read) {
        this.amount = read.readString();
        this.username = read.readString();
        this.receiver = read.readString();
        this.timestamp = read.readString();

    }

    // Create from parcel
    public static final Parcelable.Creator<Tip> CREATOR = new Parcelable.Creator<Tip>() {

        @Override
        public Tip createFromParcel(Parcel source) {
            return new Tip(source);
        }

        @Override
        public Tip[] newArray(int size) {
            return new Tip[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override // Save the object as parcel
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(amount);
        arg0.writeString(username);
        arg0.writeString(receiver);
        arg0.writeString(timestamp);
    }
}

// {"tips":[{
// "amount":"100000",
// "username":"GeertBank",
// "receiver":"GeertDev",
// "timestamp":"2016-02-20T16:43:28.801Z"},
//
// {"amount":"200000",
// "username":"JenFromCA",
// "receiver":"GeertBank",
// "timestamp":"2016-02-20T16:37:58.177Z"}

