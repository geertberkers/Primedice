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
    //{"withdrawals":[{
// "id":707534,
// "amount":"160000",
// "type":"withdrawal",
// "user_id":"1022127",
// "confirmed":true,
// "txid":"146a24e0da7335b17fc8b40e601089118973b9e9e8a4a557d3117c802d1d64c7",
// "timestamp":"2016-02-12T15:40:53.766Z",
// "acredited":null,
// "address":"1EMg7sDJGGdTpeimtWrmDmANbgqfi3FEnB",
// "ip":"213.93.49.78"}

    //{"deposits":[{
// "id":707561,
// "amount":"30000",
// "type":"deposit",
// "user_id":"1022127",
// "confirmed":false,
// "txid":"86cfbe4dc507d7eec4ade1aa1bf6347d984a3b4c0fae232ce87e3d9fba51b6c0-0",
// "timestamp":"2016-02-12T16:04:48.297Z",
// "acredited":true,
// "address":null,
// "ip":null},

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

    public int getId() {
        return id;
    }

    public String getAmount() {

        double satoshi = Long.valueOf(amount);

        DecimalFormat format = new DecimalFormat("0.00000000");

        String resultBTC = format.format(satoshi / 100000000);
        resultBTC = resultBTC.replace(",", ".");

        return resultBTC;
    }

    public String getType() {
        return type;
    }

    public String getUserID() {
        return userID;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getTxid() {
        return txid;
    }

    public String getTimestamp() {
        return timestamp.substring(0,10);
    }

    public boolean isAcredited() {
        return acredited;
    }

    public String getAddress() {
        return address;
    }

    public String getIp() {
        return ip;
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
