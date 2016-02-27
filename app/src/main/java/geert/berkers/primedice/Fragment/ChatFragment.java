package geert.berkers.primedice.Fragment;

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
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import geert.berkers.primedice.Adapter.MessageAdapter;
import geert.berkers.primedice.Data.Message;
import geert.berkers.primedice.Activity.MainActivity;
import geert.berkers.primedice.DataHandler.PostToServerTask;
import geert.berkers.primedice.R;
import geert.berkers.primedice.DataHandler.GetJSONResultFromURLTask;

/**
 * Primedice Application Created by Geert on 2-2-2016.
 */
public class ChatFragment extends Fragment {

    private View view;
    private Socket socket;
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

        if(result != null) {
            if (!result.equals("NoResult")) {
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
                        addMessage(room, message, sender, time);
                    }

                    scrollToBottom();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        Log.w("ChatFragment", "Socket subscribed on message");
        socket.on("msg",socketioMSG);
        socket.on("pm", socketioPM);
    }

    private final Emitter.Listener socketioMSG = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {

            final JSONObject obj = (JSONObject) args[0];
            Log.d("ChatFragment ", "Message: " + obj.toString());

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //TODO: Get all other information (vip/mod)
                        String room = obj.getString("room");
                        String message = obj.getString("message");
                        String sender = obj.getString("username");
                        String time = obj.getString("timestamp");

                        addMessage(room, message, sender, time);

                    } catch (JSONException e) {
                        // return;
                    }
                }
            });
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

    private void sendMessage() {
        //TODO: Handle tips/pm's while sending messages
        String message = mInputMessageView.getText().toString().trim();
        mInputMessageView.setText("");

        String result = "NoResult";
        String toUsername = null; //TODO: Get this when sending PM
        try {
            String urlParameters =
                    "room=" + URLEncoder.encode(currentRoom, "UTF-8") +
                            "&message=" + URLEncoder.encode(message, "UTF-8");

            if(toUsername != null) {
                if (toUsername.length() >= 3 && toUsername.length() <= 12) {
                    urlParameters += "&toUsername=" + URLEncoder.encode(toUsername, "UTF-8");
                }
            }
            PostToServerTask sendMessage = new PostToServerTask();
            result = sendMessage.execute((sendMessageURL + access_token), urlParameters).get();
        } catch (InterruptedException | ExecutionException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Log.i("Result", result);
    }


    // Add message to the list
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

    // Scroll list to bottom
    private void scrollToBottom() {
        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    // Set needed information for this fragment
    public void setInformation(String access_token, Socket socket) {
        this.access_token = access_token;
        this.socket = socket;
    }

    // TODO: Use onDestroy else socket will stop after other tab/screen turned off
    @Override
    public void onStop() {
        Log.w("ChatFragment", "Socket de-subscribed on message");
        socket.off("msg",socketioMSG);
        socket.off("pm", socketioPM);
        super.onStop();
    }
}