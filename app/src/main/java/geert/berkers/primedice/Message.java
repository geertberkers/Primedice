package geert.berkers.primedice;

import java.util.Date;

/**
 * Created by Geert on 5-2-2016.
 */
public class Message {

    public static final int TYPE_MESSAGE = 0;
    public static final int TYPE_LOG = 1;
    public static final int TYPE_ACTION = 2;

    public static final int ENGLISH = 0;
    public static final int RUSSIAN = 1;

    private int mRoom;
    private int mType;
    private String mMessage;
    private String mSender;
    private String mTime;


    private Message() {}

    public int getRoom() {
        return mRoom;
    };

    public int getType() {
        return mType;
    };

    public String getMessage() {
        return mMessage;
    };

    public String getSender() {
        return mSender;
    }

    public String getTime() {
        return mTime;
    }

    public static class Builder {
        private final int mRoom;
        private final int mType;
        private String mMessage;
        private String mSender;
        private String mTime;

        public Builder(int room, int type) {
            mRoom = room;
            mType = type;
        }

        public Builder message(String message, String sender, String time) {
            mMessage = message;
            mSender = sender;
            mTime = time;
            return this;
        }

        public Message build() {
            Message message = new Message();
            message.mRoom = mRoom;
            message.mType = mType;
            message.mMessage = mMessage;
            message.mSender = mSender;
            message.mTime = mTime;
            return message;
        }
    }
}
