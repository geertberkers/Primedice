package geert.berkers.primedice.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

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

    private static Activity activity;
    private View view;
    private Socket socket;
    private MainActivity mainActivity;
    private static String access_token;
    private static String sendMessageURL;
    private String receiveMessageURL;

    private static int currentRoom;
    private Spinner languageSpinner;
    private EditText mInputMessageView;
    private RecyclerView mMessagesView;

    private MessageAdapter mAdapter;
    private List<Message> englishMessages = new ArrayList<>();
    private List<Message> russianMessages = new ArrayList<>();

    public static Handler UIHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        this.view = inflater.inflate(R.layout.fragment_chat, container, false);
        initControls();

        return view;
    }

    private void initControls() {
        activity = getActivity();
        mainActivity = (MainActivity) getActivity();

        mMessagesView = (RecyclerView) view.findViewById(R.id.messages);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mainActivity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mMessagesView.setLayoutManager(layoutManager);

        ImageButton sendButton = (ImageButton) view.findViewById(R.id.send_button);
        mInputMessageView = (EditText) view.findViewById(R.id.message_input);

        sendMessageURL = "https://api.primedice.com/api/send?access_token=";
        receiveMessageURL = "https://api.primedice.com/api/messages?access_token=";

        mAdapter = new MessageAdapter(new ArrayList<Message>());

        getMessages("English");
        getMessages("Russian");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        if (sharedPref.getString("message_room", "English").equals("English")) {
            currentRoom = Message.ENGLISH;
            mAdapter = new MessageAdapter(englishMessages);
        } else {
            currentRoom = Message.RUSSIAN;
            mAdapter = new MessageAdapter(russianMessages);
        }

        mMessagesView.setAdapter(mAdapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        addSpinnerListener();

        Log.w("ChatFragment", "Socket subscribed on message");
        socket.on("msg", socketioMSG);
        socket.on("pm", socketioPM);
    }

    // Get latest messages
    private void getMessages(String room) {
        String result = "NoResult";
        try {
            GetJSONResultFromURLTask sendMessage = new GetJSONResultFromURLTask();
            result = sendMessage.execute((receiveMessageURL + access_token + "&room=" + room)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        if (result != null) {
            if (!result.equals("NoResult")) {
                try {
                    JSONObject json = new JSONObject(result);
                    JSONArray msgArray = json.getJSONArray("messages");

                    for (int i = 0; i < msgArray.length(); i++) {
                        JSONObject jsonMessage = msgArray.getJSONObject(i);

                        addMessageFromJSON(jsonMessage);
                    }

                    scrollToBottom();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Handle spinner clicks
    public void addSpinnerListener() {
        languageSpinner = (Spinner) view.findViewById(R.id.languageSpinner);

        List<String> list = new ArrayList<>();
        list.add("English");
        list.add("Russian");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(mainActivity, android.R.layout.simple_spinner_item, list);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(dataAdapter);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String room = String.valueOf(languageSpinner.getSelectedItem());

                if (room.equals("English")) {
                    currentRoom = Message.ENGLISH;
                    mAdapter.setUpdatedList(englishMessages);

                } else {
                    currentRoom = Message.RUSSIAN;
                    mAdapter.setUpdatedList(russianMessages);
                }
                scrollToBottom();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    // Create and add message from JSON
    private void addMessageFromJSON(JSONObject jsonMessage) {
        try {
            String room = jsonMessage.getString("room");
            String message = jsonMessage.getString("message");
            String sender = jsonMessage.getString("username");
            String time = jsonMessage.getString("timestamp");

            String tag = String.valueOf(jsonMessage.get("admin"));
            String type = String.valueOf(jsonMessage.get("prefix"));

            int messageRoom;
            int messageType;
            int messageTag;

            switch (room) {
                case "Russian":
                    messageRoom = Message.RUSSIAN;
                    break;
                case "English":
                    messageRoom = Message.ENGLISH;
                    break;
                default:
                    messageRoom = Message.ENGLISH;
                    break;
            }

            switch (type) {
                case "false":
                    messageType = Message.MESSAGE;
                    break;
                case "PM":
                    messageType = Message.PM;
                    break;
                case "BOT":
                    messageType = Message.BOT;
                    break;
                default:
                    messageType = Message.USER;
                    break;
            }

            switch (tag) {
                case "false":
                    messageTag = Message.USER;
                    break;
                case "M":
                    messageTag = Message.MOD;
                    break;
                case "S":
                    messageTag = Message.SUPPORT;
                    break;
                case "A":
                    messageTag = Message.ADMIN;
                    break;
                case "VIP":
                    messageTag = Message.VIP;
                    break;
                default:
                    messageTag = Message.USER;
                    break;
            }

            addMessage(messageRoom, messageType, messageTag, message, sender, time);
        } catch (JSONException e) {
            // return;
        }
    }

    // Send Message to server
    private void sendMessage() {
        //TODO: Handle tips/pm's while sending messages
        String message = mInputMessageView.getText().toString().trim();
        mInputMessageView.setText("");

        String result = "NoResult";
        String toUsername = null; //TODO: Get this when sending PM

        try {
            String urlParameters =
                    "room=" + URLEncoder.encode(getRoom(), "UTF-8") +
                            "&message=" + URLEncoder.encode(message, "UTF-8");

            if (toUsername != null) {
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

    private static String getRoom() {
        if (currentRoom == Message.ENGLISH) {
            return "English";
        } else {
            return "Russian";
        }
    }

    // Add message to the list
    private void addMessage(int room, int messageType, int messageTag, String message, String sender, String time) {
        final List<Message> messages;

        if (room == Message.ENGLISH) {
            messages = englishMessages;
        } else {
            messages = russianMessages;
        }

        messages.add(new Message(room, messageType, messageTag, message, sender, time));

        // Remove oldest messages when reaches over 50
        while (messages.size() > 50) {
            messages.remove(0);
        }

        if (currentRoom == room) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.setUpdatedList(messages);
                    scrollToBottom();
                }
            });

        }
    }

    // Scroll list to bottom
    private void scrollToBottom() {
        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    // Set needed information for this fragment
    public void setInformation(String access_token_value, Socket socket) {
        access_token = access_token_value;
        this.socket = socket;
    }

    private final Emitter.Listener socketioMSG = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            final JSONObject jsonMessage = (JSONObject) args[0];

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addMessageFromJSON(jsonMessage);
                }
            });
        }
    };

    private final Emitter.Listener socketioPM = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject jsonPM = (JSONObject) args[0];
            // Log.w("ChatFragment", "PM Result: " + obj.toString());
            //TODO: Handle PM

            addMessageFromJSON(jsonPM);
            // PM Result: {"room":"English","userid":"1022127","username":"GeertBank","message":"test","toUsername":"geertberkers","timestamp":"2016-03-04T14:24:10.677Z","admin":false,"prefix":"PM"}
            // PM Result: {"room":"English","userid":"990311","username":"GeertBerkers","message":"Test","toUsername":"GeertBank","timestamp":"2016-03-04T14:24:26.642Z","admin":false,"prefix":"PM"}

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        socket.off("msg", socketioMSG);
        socket.off("pm", socketioPM);

        Log.w("ChatFragment", "Socket de-subscribed on message");
    }

    public static void sendPM(final View v, final String toUsername) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final EditText edMessage = new EditText(v.getContext());

                AlertDialog.Builder pmDialog = new AlertDialog.Builder(v.getContext());
                pmDialog.setTitle("MESSAGE " + toUsername);
                pmDialog.setMessage("This message will only be seen by " + toUsername);
                pmDialog.setView(edMessage);
                pmDialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String message = edMessage.getText().toString();
                        try {
                            String urlParameters = "room=" + URLEncoder.encode(getRoom(), "UTF-8")
                                    + "&message=" + URLEncoder.encode(message, "UTF-8")
                                    + "&toUsername=" + URLEncoder.encode(toUsername, "UTF-8");

                            PostToServerTask sendMessage = new PostToServerTask();
                            String result = sendMessage.execute((sendMessageURL + access_token), urlParameters).get();
                            System.out.println(result);

                        } catch (InterruptedException | ExecutionException | UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                });
                pmDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                pmDialog.show();

            }
        });
    }
}