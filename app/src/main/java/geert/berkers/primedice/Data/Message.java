package geert.berkers.primedice.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Primedice Application Created by Geert on 5-2-2016.
 */
public class Message {

    // Rooms:
    public static final int ENGLISH = 0;
    public static final int RUSSIAN = 1;

    // Types:
    public static final int PM = 0;
    public static final int BOT = 1;
    public static final int MESSAGE = 2;

    // Tags:
    public static final int VIP = 0;
    public static final int MOD = 1;
    public static final int USER = 2;
    public static final int ADMIN = 3;
    public static final int SUPPORT = 4;

    private int room;
    private int type;
    private int tag;

    private String message;
    private String sender;
    private String toUsername;
    private String time;

    public Message(int room, int type, int tag, String message, String sender, String toUsername, String time) {
        this.room = room;
        this.type = type;
        this.tag = tag;
        this.message = message;
        this.sender = sender;
        this.toUsername = toUsername;
        this.time = time;
    }

    public String getLocalTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date value = null;
        try {
            value = formatter.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat localFormat = new SimpleDateFormat("HH:mm");
        localFormat.setTimeZone(TimeZone.getDefault());
        return localFormat.format(value);
    }

    public String getSender() {
        return sender;
    }

    public String getToUsername() {
        return toUsername;
    }

    public String getMessage() {
        return message.replace("\n"," ");
    }

    public int getTag() {
        return tag;
    }

    public int getType() {
        return type;
    }
}