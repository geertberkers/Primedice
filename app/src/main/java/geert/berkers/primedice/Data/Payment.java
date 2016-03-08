package geert.berkers.primedice.Data;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

/**
 * Primedice Application Created by Geert on 20-2-2016.
 */
public class Payment implements Parcelable{

    private int id;
    private String amount;
    private String type;
    private String userID;
    private boolean confirmed;
    private String txid;
    private String timestamp;
    private boolean acredited;
    private String address;
    private String ip;

    public Payment(JSONObject payment) {
        try {
            this.id = payment.getInt("id");
            this.amount = payment.getString("amount");
            this.type = payment.getString("type");
            this.userID = payment.getString("user_id");
            this.confirmed = payment.getBoolean("confirmed");
            this.txid = payment.getString("txid");
            this.timestamp = payment.getString("timestamp");
            try {
                this.acredited = payment.getBoolean("acredited");
            } catch (Exception ex) {
                this.acredited = false;
            }
            this.address = payment.getString("address");
            this.ip = payment.getString("ip");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getAmount() {

        double satoshi = Long.valueOf(amount);

        DecimalFormat format = new DecimalFormat("0.00000000");

        String resultBTC = format.format(satoshi / 100000000);
        resultBTC = resultBTC.replace(",", ".");

        return resultBTC;
    }

    public String getTxid() {
        return txid;
    }

    public String getTimestamp() {
        return timestamp.substring(0,10);
    }

    @Override
    public String toString() {
        return getTxid().substring(0, 7) + "...   " + getAmount() + "   " + getTimestamp();
    }

    // Create Payment from parcel
    private Payment(Parcel read) {
        this.id = read.readInt();
        this.amount = read.readString();
        this.type = read.readString();
        this.userID = read.readString();
        this.confirmed = read.readString().equals("Y");
        this.txid = read.readString();
        this.timestamp = read.readString();
        this.acredited = read.readString().equals("Y");
        this.address = read.readString();
        this.ip = read.readString();
    }

    // Create from parcel
    public static final Parcelable.Creator<Payment> CREATOR = new Parcelable.Creator<Payment>() {

        @Override
        public Payment createFromParcel(Parcel source) {
            return new Payment(source);
        }

        @Override
        public Payment[] newArray(int size) {
            return new Payment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override // Save the object as parcel
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeInt(id);
        arg0.writeString(amount);
        arg0.writeString(type);
        arg0.writeString(userID);
        if(confirmed) arg0.writeString("Y"); else arg0.writeString("N");
        arg0.writeString(txid);
        arg0.writeString(timestamp);
        if(acredited) arg0.writeString("Y"); else arg0.writeString("N");
        arg0.writeString(address);
        arg0.writeString(ip);
    }
}
