package geert.berkers.primedice.Fragment;

import android.app.Activity;
import android.app.Fragment;
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
import geert.berkers.primedice.DataHandler.GetFromServerTask;

/**
 * Primedice Application Created by Geert on 2-2-2016.
 */
public class ChatFragment extends Fragment {

    private View view;
    private static MainActivity mainActivity;

    private String access_token;
    private static int currentRoom;
    private Spinner languageSpinner;
    private EditText mInputMessageView;
    private RecyclerView mMessagesView;

    private MessageAdapter mAdapter;
    private List<Message> englishMessages;
    private List<Message> russianMessages;
    private static List<String> ignoredUsers = new ArrayList<>();

    public void setMessagesBeforeCreate(String access_token, Activity activity) {
        this.access_token = access_token;
        englishMessages = getMessages("English");
        russianMessages = getMessages("Russian");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        if (sharedPref.getString("message_room", "English").equals("English")) {
            currentRoom = Message.ENGLISH;
            System.out.println("ENGLISH Room set");
        } else {
            currentRoom = Message.RUSSIAN;
            System.out.println("RUSSIAN Room set");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            this.view = inflater.inflate(R.layout.fragment_chat, container, false);
            initControls();
        }

        return view;
    }

    private void initControls() {
        mainActivity = (MainActivity) getActivity();

        mMessagesView = (RecyclerView) view.findViewById(R.id.messages);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mainActivity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mMessagesView.setLayoutManager(layoutManager);

        ImageButton sendButton = (ImageButton) view.findViewById(R.id.send_button);
        mInputMessageView = (EditText) view.findViewById(R.id.message_input);

        if (currentRoom == Message.ENGLISH) {
            mAdapter = new MessageAdapter(englishMessages);

        } else {
            mAdapter = new MessageAdapter(russianMessages);
        }

        setAdapter(mAdapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        addSpinnerListener();
    }

    private void setAdapter(final MessageAdapter mAdapter) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMessagesView.setAdapter(mAdapter);
                scrollToBottom();
            }
        });
    }

    // Get latest messages
    private List<Message> getMessages(String room) {
        try {
            GetFromServerTask sendMessage = new GetFromServerTask();
            String result = sendMessage.execute((URL.RECEIVE_MESSAGE + access_token + "&room=" + room)).get();

            if (result != null) {
                JSONObject json = new JSONObject(result);
                JSONArray msgArray = json.getJSONArray("messages");

                ArrayList<Message> messages = new ArrayList<>();
                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject jsonMessage = msgArray.getJSONObject(i);
                    messages.add(createMessageFromJSON(jsonMessage));
                }

                return messages;
            }
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Handle spinner clicks
    private void addSpinnerListener() {
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
    public static Message createMessageFromJSON(JSONObject jsonMessage) {
        try {
            String room = jsonMessage.getString("room");
            String message = jsonMessage.getString("message");
            String sender = jsonMessage.getString("username");
            String time = jsonMessage.getString("timestamp");

            String tag = String.valueOf(jsonMessage.get("admin"));
            String type = String.valueOf(jsonMessage.get("prefix"));
            String toUsername = null;
            try {
                toUsername = String.valueOf(jsonMessage.get("toUsername"));
            } catch (Exception ex) {
                Log.i("MESSAGE", "This message isn't a pm");
            }
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

            return new Message(messageRoom, messageType, messageTag, message, sender, toUsername, time);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
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
            MainActivity.tipUser(null, toUsername);
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
                GetFromServerTask getBetsTask = new GetFromServerTask();
                String result = getBetsTask.execute((URL.BET_LOOKUP + bet.replace(",", ""))).get();

                if (result != null) {
                    JSONObject jsonBet = new JSONObject(result);
                    Bet b = new Bet(jsonBet.getJSONObject("bet"));

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
            String result = sendMessage.execute((URL.SEND_MESSAGE + access_token), urlParameters).get();

            Log.i("MESSAGE_RESULT", result);

        } catch (InterruptedException | ExecutionException | UnsupportedEncodingException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static String getRoom() {
        if (currentRoom == Message.ENGLISH) {
            return "English";
        } else {
            return "Russian";
        }
    }

    // Add message to the list
    public void addMessage(Message message, boolean showNewMessage) {
        final List<Message> messages;

        if (message.getRoom() == Message.ENGLISH) {
            messages = englishMessages;
        } else {
            messages = russianMessages;
        }

        if (!ignoredUsers.contains(message.getSender().toLowerCase())) {
            messages.add(message);
        }

        // Remove oldest messages when reaches over 50
        while (messages.size() > 50) {
            messages.remove(0);
        }

        if (showNewMessage) {
            if (message.getRoom() == currentRoom) {

                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.setUpdatedList(messages);
                        scrollToBottom();
                    }
                });
            }
        }
    }


    // Scroll list to bottom
    private void scrollToBottom() {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
            }
        });
    }

    // Ignore user
    public static void ignoreOrUnignoreUser(String user) {
        if (ignoredUsers.contains(user.toLowerCase())) {
            ignoredUsers.remove(user.toLowerCase());
            MainActivity.showNotification(false, user + " unignored.", 5);
        } else {
            ignoredUsers.add(user.toLowerCase());
            MainActivity.showNotification(false, user + " ignored.", 5);
        }
    }

    // Get ignored for message in chat
    public static String getUserIgnoreString(String user) {
        if (ignoredUsers.contains(user.toLowerCase())) {
            return "UNIGNORE";
        } else {
            return "IGNORE";
        }
    }


}