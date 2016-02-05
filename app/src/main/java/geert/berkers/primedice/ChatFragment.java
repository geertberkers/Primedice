package geert.berkers.primedice;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Primedice Application Created by Geert on 2-2-2016.
 */
public class ChatFragment extends Fragment {

    private View view;
    private MainActivity activity;
    private String access_token, sendMessageURL, receiveMessageURL, currentRoom;

    private EditText mInputMessageView;
    private RecyclerView mMessagesView;
    private List<Message> mMessages = new ArrayList<>();
    private MessageAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_chat, container, false);

        initControls();

        //TODO: Change rooms
        return view;
    }

    private void initControls() {
        activity = (MainActivity) getActivity();
        mAdapter = new MessageAdapter(mMessages);

        mMessagesView = (RecyclerView) view.findViewById(R.id.messages);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mMessagesView.setLayoutManager(layoutManager);
        mMessagesView.setAdapter(mAdapter);

        ImageButton sendButton = (ImageButton) view.findViewById(R.id.send_button);
        mInputMessageView = (EditText) view.findViewById(R.id.message_input);

        currentRoom = "English";
        sendMessageURL = "https://api.primedice.com/api/send?access_token=";
        receiveMessageURL = "https://api.primedice.com/api/messages?access_token=";

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        String result = "NoResult";
        try {
            GetJSONResultFromURLTask sendMessage = new GetJSONResultFromURLTask();
            result = sendMessage.execute((receiveMessageURL + access_token + "&room=" + currentRoom)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        if(result != null || !result.equals("NoResult")) {
            try {
                JSONObject json = new JSONObject(result);
                JSONArray msgArray = json.getJSONArray("messages");

                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject msg = msgArray.getJSONObject(i);

                    String room = msg.getString("room");
                    String message = msg.getString("message");
                    String sender = msg.getString("username");
                    String time = msg.getString("timestamp");

                    // {"room":"English","userid":"1180493","username":"kamleshwaran","message":"For me it's slow maybe becoz I m on phone","toUsername":null,"timestamp":"2016-02-05T15:15:01.568Z","admin":false,"prefix":false,"id":3632800,"you":false,"linked":false},
                    addMessage(room,message,sender,time);
                }

                scrollToBottom();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        mSocket.connect();
    }

    private final Emitter.Listener socketioConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.i("ChatFragment", "Socket connected");
            mSocket.emit("user", access_token);
            mSocket.emit("chat");
            mSocket.emit("stats");
        }
    };

    private final Emitter.Listener socketioMSG = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {

            final JSONObject obj = (JSONObject) args[0];
            Log.d("ChatFragment: ", "message back: " + obj.toString());

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //TODO: Get all other information (vip/mod)
                        String room = obj.getString("room");
                        String message = obj.getString("message");
                        String sender = obj.getString("username");
                        String time = obj.getString("timestamp");

                        removeMessage();
                        addMessage(room, message, sender, time);

                    } catch (JSONException e) {
                        // return;
                    }
                }
            });
        }
    };

    //TODO: Handle TIP
    private final Emitter.Listener socketioTip = new Emitter.Listener() {
            @Override
        public void call(Object... args) {
            JSONObject obj = (JSONObject) args[0];
            Log.w("ChatFragment", "TIP RResult: " + obj.toString());
            // Tip response: {"userid":"990311","user":"GeertBerkers","amount":50001,"sender":"GeertBank","senderid":"1022127"}
        }
    };

    //TODO: Handle PM
    private final Emitter.Listener socketioPM = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject obj = (JSONObject) args[0];
            Log.w("ChatFragment", "PM Result: " + obj.toString());
            // PM response {"room":"English","userid":"1004533","username":"testqwerty3","message":"Testing PM :)","toUsername":"GeertBerkers","timestamp":"2016-02-05T12:52:19.706Z","admin":false,"prefix":"PM"}
        }
    };

    //TODO: Handle Bet
    private final Emitter.Listener socketioBet = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject obj = (JSONObject) args[0];
            //This gets all new bets for all bets tab.
            //Log.w("ChatFragment", "Bet Result: " + obj.toString());
            //Bet Result: {"id":8738847242,"player":"otax2","player_id":"1225873","amount":1024000,"target":50.49,"profit":1024000,"win":true,"condition":">","roll":84.72,"nonce":1470,"client":"e91be697d8f2256e4b8d656836cd68b5","multiplier":2,"timestamp":"2016-02-05T14:48:28.213Z","jackpot":false,"server":"eb484667a46b7afbaa7b185921e2a34e85efac881e8d90ba8dbcefff44883ffd"}
        }
    };

    //TODO: Handle Deposit
    private final Emitter.Listener socketioDeposit = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject obj = (JSONObject) args[0];
            Log.w("ChatFragment", "Deposit Result: " + obj.toString());
        }
    };

    //TODO: Handle Alert
    private final Emitter.Listener socketioAlert = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject obj = (JSONObject) args[0];
            Log.w("ChatFragment", "Alert Result: " + obj.toString());
        }
    };

    //TODO: Handle Stats
    private final Emitter.Listener socketioStats = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject obj = (JSONObject) args[0];
            // This gets stats (BTC WON LAST 24 HOURS)
            //Log.w("ChatFragment", "Stats Result: " + obj.toString());
            //Stats Result: {"bets24":19462106,"wagered24":1.1444498764200024E11}
        }
    };

    //TODO: Handle Success
    private final Emitter.Listener socketioSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject obj = (JSONObject) args[0];
            Log.w("ChatFragment", "Succes Result: " + obj.toString());
        }
    };

    //TODO: Handle Error
    private final Emitter.Listener socketioError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject obj = (JSONObject) args[0];
            Log.w("ChatFragment", "Error Result: " + obj.toString());
        }
    };

    private final Emitter.Listener socketioDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("Chatfragment", "Socket disconnected");
        }
    };

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("https://sockets.primedice.com");
            mSocket.on(Socket.EVENT_CONNECT, socketioConnect)
                    .on("msg", socketioMSG)
                    .on("tip", socketioTip)
                    .on("pm", socketioPM)
                    .on("bet", socketioBet)
                    .on("deposit", socketioDeposit)
                    .on("alert", socketioAlert)
                    .on("stats", socketioStats)
                    .on("success", socketioSuccess)
                    .on("err", socketioError)
                    .on(Socket.EVENT_DISCONNECT, socketioDisconnect);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        //TODO: Handle tips/pm's while sending messages
        String message = mInputMessageView.getText().toString().trim();
        mInputMessageView.setText("");

        String result = "NoResult";
        String toUsername = null; //TODO: Get this when sending PM
        try {
            SendMessageTask sendMessage = new SendMessageTask();
            result = sendMessage.execute((sendMessageURL + access_token), currentRoom, message, toUsername).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        Log.w("Result", result);
    }

    private void removeMessage(){


    }
    private void addMessage(String roomString, String message, String sender, String time) {

        int room = Message.ENGLISH;
        if (roomString.equals("Russian")) {
            room = Message.RUSSIAN;
        }


        // Add message
        mMessages.add(new Message.Builder(room, Message.TYPE_MESSAGE).message(message, sender, time).build());

        // Remove oldest messages when reaches over 50
        while(mMessages.size() > 50){
            Log.w("Remove",mMessages.get(0).getMessage());
            mMessages.remove(0);
        }

        mAdapter.setUpdatedList(mMessages);
        scrollToBottom();
    }

    private void scrollToBottom() {
        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    public void setInformation(String access_token) {
        this.access_token = access_token;
    }

    @Override
    public void onStop() {
        mSocket.disconnect();
        super.onStop();
    }
}