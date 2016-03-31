package geert.berkers.primedice.DataHandler;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import geert.berkers.primedice.Activity.MainActivity;
import geert.berkers.primedice.Data.Message;
import geert.berkers.primedice.Data.URLS;
import geert.berkers.primedice.Fragment.ChatFragment;

/**
 * Primedice Application Created by Geert on 30-3-2016.
 */
public class LoadChatTask extends AsyncTask<String, Void, String> {

    private int room;
    private String access_token;
    private MainActivity activity;
    private APIConnector APIConnector;
    private ProgressDialog progressDialog;
    private ArrayList<Message> englishMessages;
    private ArrayList<Message> russianMessages;
    private ArrayList<String> allowedLinksInChat;

    public LoadChatTask(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(String... params) {
        this.access_token = params[0];

        room = getRoom();
        englishMessages = getMessages("English");
        russianMessages = getMessages("Russian");

        getAllowedLinksInChat();

        return null;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(activity, "Loading", "Downloading messages.");
    }

    @Override
    protected void onPostExecute(String result) {
        progressDialog.dismiss();

        activity.allowedLinksInChat = allowedLinksInChat;
        activity.initChatFragment(room, englishMessages, russianMessages);
        activity.startSockets();

    }

    public void getAllowedLinksInChat() {
        allowedLinksInChat = new ArrayList<>();

        try {
            java.net.URL url = new java.net.URL(URLS.GET_ALLOWED_CHAT_LINKS);
            APIConnector = new APIConnector(url, "GET");
            String result = APIConnector.getResult();

            if (result != null) {
                try {
                    JSONArray jsonArray = new JSONArray(result);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonSite = jsonArray.getJSONObject(i);
                        allowedLinksInChat.add(jsonSite.getString("site"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            APIConnector.closeConnection();
        }
    }

    public int getRoom() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        if (sharedPref.getString("message_room", "English").equals("English")) {
            System.out.println("English room set.");
            return Message.ENGLISH;
        } else {
            System.out.println("Russian room set");
            return Message.RUSSIAN;
        }
    }

    public ArrayList<Message> getMessages(String room){
        ArrayList<Message> messages = new ArrayList<>();

        try {
            String url = (URLS.RECEIVE_MESSAGE + access_token + "&room=" + room);

            URL URL = new URL(url);
            APIConnector = new APIConnector(URL, "GET");
            String result = APIConnector.getResult();

            if (result != null) {
                JSONObject json = new JSONObject(result);
                JSONArray msgArray = json.getJSONArray("messages");

                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject jsonMessage = msgArray.getJSONObject(i);
                    messages.add(ChatFragment.createMessageFromJSON(jsonMessage));
                }
                System.out.println(room + " messages set.");

                return messages;
            }
        } catch (MalformedURLException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
