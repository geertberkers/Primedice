package geert.berkers.primedice.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import geert.berkers.primedice.Activity.BetInformationActivity;
import geert.berkers.primedice.Activity.PlayerInformationActivity;
import geert.berkers.primedice.Adapter.MessageAdapter;
import geert.berkers.primedice.Data.Bet;
import geert.berkers.primedice.Data.Message;
import geert.berkers.primedice.Activity.MainActivity;
import geert.berkers.primedice.Data.URL;
import geert.berkers.primedice.DataHandler.PostToServerTask;
import geert.berkers.primedice.R;
import geert.berkers.primedice.DataHandler.GetJSONResultFromURLTask;

/**
 * Primedice Application Created by Geert on 2-2-2016.
 */
public class ChatFragment extends Fragment {

    private View view;
    private Socket socket;
    private static MainActivity mainActivity;
    private static String access_token;

    private static int currentRoom;
    private Spinner languageSpinner;
    private EditText mInputMessageView;
    private RecyclerView mMessagesView;

    private MessageAdapter mAdapter;
    private List<Message> englishMessages = new ArrayList<>();
    private List<Message> russianMessages = new ArrayList<>();
    private static List<String> ignoredUsers = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            this.view = inflater.inflate(R.layout.fragment_chat, container, false);
            initControls();
        }

        return view;
    }

    private void initControls() {
        //activity = getActivity();
        mainActivity = (MainActivity) getActivity();

        mMessagesView = (RecyclerView) view.findViewById(R.id.messages);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mainActivity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mMessagesView.setLayoutManager(layoutManager);

        ImageButton sendButton = (ImageButton) view.findViewById(R.id.send_button);
        mInputMessageView = (EditText) view.findViewById(R.id.message_input);

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
            result = sendMessage.execute((URL.RECEIVE_MESSAGE + access_token + "&room=" + room)).get();
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
            String toUsername = jsonMessage.getString("toUsername");

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

            if (!ignoredUsers.contains(sender.toLowerCase())) {
                addMessage(messageRoom, messageType, messageTag, message, sender, toUsername, time);
            }
        } catch (JSONException e) {
            // return;
        }
    }

    // Send Message to server
    private void sendMessage() {
        String message = mInputMessageView.getText().toString().trim();
        mInputMessageView.setText("");

        String toUsername = null;
        if (message.startsWith("/pm ")) {
            String temp = message.substring(4);

            int length = temp.indexOf(" ");

            message = temp.substring(length);
            toUsername = temp.substring(0, length);
        } else if (message.startsWith("/tip ")) {
            toUsername = message.substring(5);
            MainActivity.tipUser(null,toUsername);
            return;
        } else if (message.startsWith("/ignore ")) {
            toUsername = message.substring(8);
            ignoredUsers.add(toUsername.toLowerCase());
            MainActivity.showNotification(false, toUsername + " ignored", 5);
            return;
        } else if (message.startsWith("/unignore ")) {
            toUsername = message.substring(10);
            ignoredUsers.remove(toUsername.toLowerCase());
            MainActivity.showNotification(false, toUsername + " unignored", 5);
            return;
        } else if (message.startsWith("/reset")) {
            ignoredUsers = new ArrayList<>();
            MainActivity.showNotification(false, "Ignore list reset", 5);
            return;
        } else if (message.startsWith("/user ")) {
            toUsername = message.substring(6);

            int length = toUsername.indexOf(" ");
            if (length != -1) {
                toUsername = toUsername.substring(0, length);
            }

            Intent playerInfoIntent = new Intent(mainActivity, PlayerInformationActivity.class);
            playerInfoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            playerInfoIntent.putExtra("playerName", toUsername);
            mainActivity.startActivity(playerInfoIntent);
            return;
        } else if (message.startsWith("/bet ")) {
            String bet = message.substring(5);

            int length = bet.indexOf(" ");
            if (length != -1) {
                bet = bet.substring(0, length);
            }

            try {
                GetJSONResultFromURLTask getBetsTask = new GetJSONResultFromURLTask();

                String result = getBetsTask.execute((URL.BET_LOOKUP + bet.replace(",", ""))).get();

                //TODO: Fix bet date.
                if (result != null) {
                    JSONObject jsonBet = new JSONObject(result);
                    Bet b = new Bet(jsonBet.getJSONObject("bet"));
                    //b.setDate()
                    Intent betInfoIntent = new Intent(mainActivity, BetInformationActivity.class);
                    betInfoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    betInfoIntent.putExtra("bet", b);
                    mainActivity.startActivity(betInfoIntent);
                }
            } catch (InterruptedException | ExecutionException | JSONException e) {
                e.printStackTrace();
            }
            return;
        } else if (message.startsWith("/")) {
            MainActivity.showNotification(true, "Chat error: Try again!", 5);
            return;
        }

        String result = "NoResult";
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
            result = sendMessage.execute((URL.SEND_MESSAGE + access_token), urlParameters).get();
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
    private void addMessage(int room, int messageType, int messageTag, String message, String sender, String toUsername, String time) {
        final List<Message> messages;

        if (room == Message.ENGLISH) {
            messages = englishMessages;
        } else {
            messages = russianMessages;
        }

        messages.add(new Message(room, messageType, messageTag, message, sender, toUsername, time));

        // Remove oldest messages when reaches over 50
        while (messages.size() > 50) {
            messages.remove(0);
        }

        if (currentRoom == room) {
            mainActivity.runOnUiThread(new Runnable() {
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

    // Receive message from socketio server
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

    // Receive PM from socketio server
    private final Emitter.Listener socketioPM = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject jsonPM = (JSONObject) args[0];

            addMessageFromJSON(jsonPM);
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
        mainActivity.runOnUiThread(new Runnable() {
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
                            String result = sendMessage.execute((URL.SEND_MESSAGE + access_token), urlParameters).get();
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

    // Ignore user
    public static void ignoreOrUnignoreUser(String user) {
        if(ignoredUsers.contains(user.toLowerCase())) {
            ignoredUsers.add(user.toLowerCase());
        }
        else{
            ignoredUsers.remove(user.toLowerCase());
        }
    }

    public static String getUserIgnoreString(String user) {
        if(ignoredUsers.contains(user.toLowerCase())) {
            return "UNIGNORE";
        }
        else{
            return "IGNORE";
        }
    }
}