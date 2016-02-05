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
    private String access_token, sendMessageURL;

    private EditText mInputMessageView;
    private RecyclerView mMessagesView;
    private List<Message> mMessages = new ArrayList<>();
    private RecyclerView.Adapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_chat, container, false);

        initControls();

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

        sendMessageURL = "https://api.primedice.com/api/send?access_token=";

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        //TODO: Get latest chat messages
        /*
        GET /messages
        A URL parameter room can be used to specify which room, such as "en". Defaults to "en" for the english chat room. Requires authentication.
        */
        mSocket.connect();
    }

    private Socket mSocket;

    {
        try {
            mSocket = IO.socket("https://sockets.primedice.com");
            mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.d("ChatFragment: ", "socket connected");
                    mSocket.emit("user", access_token);
                    mSocket.emit("chat");
                    mSocket.emit("stats");
                }

            }).on("msg", new Emitter.Listener() {

                @Override
                public void call(final Object... args) {

                    final JSONObject obj = (JSONObject) args[0];
                    Log.d("ChatFragment: ", "message back: " + obj.toString());

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //TODO: Get alll other information
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

            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                }

            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String message = mInputMessageView.getText().toString().trim();
        mInputMessageView.setText("");

        String result = "NoResult";
        String room = "English"; //TODO: Set room in ChatFragment
        String toUsername = null; //TODO: Get this when sending PM
        try {
            SendMessageTask sendMessage = new SendMessageTask();
            result = sendMessage.execute((sendMessageURL + access_token), room, message, toUsername).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        Log.w("Result", result);
        //TODO: Implement REST API POST message
/*
POST /send
Expects a room and message, optionally specify a username in toUsername to send a PM. Requires authentication.
*/
    }

    private void addMessage(String roomString, String message, String sender, String time) {

        int room = Message.ENGLISH;
        if (roomString.equals("Russian")) {
            room = Message.RUSSIAN;
        }
        //TODO: Remove latest messages if over 30
        mMessages.add(new Message.Builder(room, Message.TYPE_MESSAGE)
                .message(message, sender, time).build());
        mAdapter = new MessageAdapter(mMessages);
        mAdapter.notifyItemInserted(0);
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
        //TODO: Stop when close
        super.onStop();
    }

}

/*
I implemented the socket.io protocol for PD in C++ easily enough

you just emit user and emit chat

as long as the token is valid you will get msgs/pms

sock = io.connect('https://sockets.primedice.com').
on("connect", sockioConnect).
on("disconnect", sockioDisconnect).
on("tip", sockioTip).
on("msg", sockioMSG).
on("pm", sockioPM).
on("bet", sockioBet).
on("deposit", sockioDeposit).
on("alert", sockioAlert).
on("stats", sockioStats).
on("success", sockioSuccess).
on("err", sockioErr);

function sockioConnect() {
console.log('socket.io connected');

sock.emit("user", access_token);
sock.emit("chat");
sock.emit("stats");
}
21:16:07
that's about it
 */