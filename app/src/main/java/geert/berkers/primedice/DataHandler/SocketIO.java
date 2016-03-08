package geert.berkers.primedice.DataHandler;

import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.DecimalFormat;

import geert.berkers.primedice.Activity.MainActivity;
import geert.berkers.primedice.Data.Bet;
import geert.berkers.primedice.Data.Message;
import geert.berkers.primedice.Data.URL;
import geert.berkers.primedice.Fragment.ChatFragment;

/**
 * Primedice Application Created by Geert on 8-3-2016.
 */
// Singleton Socket class
public class SocketIO {

    private static Socket socket;

    private static int betCounter;

    private static void initSocket() {
        try {
            betCounter = 0;

            socket = IO.socket(URL.SOCKETS);

            socket.on(Socket.EVENT_CONNECT, socketioConnect);       // Connect sockets
            socket.on(Socket.EVENT_DISCONNECT, socketioDisconnect); // Disconnect sockets

        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static Socket getInstance() {
        if (socket == null) {
            initSocket();
        }

        return socket;
    }

    private static final Emitter.Listener socketioConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.i("MainActivity", "Socket connected");

            socket.on("pm", socketioPM);                            // Get PMs
            socket.on("msg", socketioMSG);                          // Get messages
            socket.on("tip", socketioTip);                          // Get tip
            socket.on("bet", socketioBet);                          // Add bets to all bets or high rollers
            socket.on("err", socketioError);                        // Socket error
            socket.on("deposit", socketioDeposit);                  // Get information about deposit
        }
    };

    private static final Emitter.Listener socketioTip = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject obj = (JSONObject) args[0];
            Log.i("MainActivity", "TIP Result: " + obj.toString());

            try {
                String sendername = obj.getString("sender");
                int amount = obj.getInt("amount");
                double amountDouble = (double) amount;
                DecimalFormat format = new DecimalFormat("0.00000000");
                String amountString = format.format(amountDouble / 100000000);
                String notification = "Received tip of " + amountString + " BTC from " + sendername;

                MainActivity.updateUser();
                MainActivity.updateBalanceInOpenedFragment();
                MainActivity.showNotification(false, notification, 15);

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    };

    // Get all new bets (all and HR)
    private static final Emitter.Listener socketioBet = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject obj = (JSONObject) args[0];

            try {
                Bet bet = new Bet(obj);

                // Check if bet is HR bet or Normal bet
                if (bet.getProfit() >= 10000000) {
                    betCounter++;
                    MainActivity.addBetAndBalanceInOpenedFragment(bet, true, false);
                } else {

                    //Only add 1/3 else to fast for application
                    betCounter++;
                    if (betCounter >= 3) {
                        MainActivity.addBetAndBalanceInOpenedFragment(bet, false, false);
                        betCounter = 0;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    private static final Emitter.Listener socketioDeposit = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject obj = (JSONObject) args[0];
            Log.i("MainActivity", "Deposit Result: " + obj.toString());

            try {
                boolean acredited = obj.getBoolean("acredited");

                int amount = obj.getInt("amount");
                double amountDouble = (double) amount;
                DecimalFormat format = new DecimalFormat("0.00000000");
                String amountString = format.format(amountDouble / 100000000);

                // Show notification
                if (!acredited) {
                    String text = "Received deposit of " + amountString + " BTC - Awaiting one confirmation";
                    MainActivity.showNotification(false, text, 0);
                } else {
                    // Update balance
                    MainActivity.updateUser();
                    MainActivity.updateBalanceInOpenedFragment();

                    String notification = "Confirmed deposit of " + amount + " BTC - Amount credited";
                    MainActivity.showNotification(false, notification, 15);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    // Receive message from socketio server
    private static final Emitter.Listener socketioMSG = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            final JSONObject jsonMessage = (JSONObject) args[0];
            Message message = ChatFragment.createMessageFromJSON(jsonMessage);
            MainActivity.addMessageShowIfOpened(message);
        }
    };

    // Receive PM from socketio server
    private static final Emitter.Listener socketioPM = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject jsonPM = (JSONObject) args[0];
            Message message = ChatFragment.createMessageFromJSON(jsonPM);
            MainActivity.addMessageShowIfOpened(message);
        }
    };

    private static final Emitter.Listener socketioError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject obj = (JSONObject) args[0];
            Log.w("MainActivity", "Error Result: " + obj.toString());
            MainActivity.showNotification(true, "Socket.io error", 10);
        }
    };

    private static final Emitter.Listener socketioDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("MainActivity", "Socket disconnected");

            socket.off("user");
            socket.off("chat");
            socket.off("stats");
            socket.off("pm", socketioPM);
            socket.off("msg", socketioMSG);
            socket.off("tip", socketioTip);
            socket.off("bet", socketioBet);
            socket.off("err", socketioError);
            socket.off("deposit", socketioDeposit);
        }
    };
}
