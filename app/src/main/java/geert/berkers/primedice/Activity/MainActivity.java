package geert.berkers.primedice.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.FragmentManager;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.integration.android.IntentIntegrator;

import org.json.JSONObject;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import geert.berkers.primedice.DataHandler.PostToServerTask;
import geert.berkers.primedice.R;
import geert.berkers.primedice.Data.Bet;
import geert.berkers.primedice.Data.User;
import geert.berkers.primedice.Adapter.MenuAdapter;
import geert.berkers.primedice.Fragment.BetFragment;
import geert.berkers.primedice.Fragment.ChatFragment;
import geert.berkers.primedice.Fragment.StatsFragment;
import geert.berkers.primedice.Fragment.FaucetFragment;
import geert.berkers.primedice.Fragment.ProfileFragment;
import geert.berkers.primedice.Fragment.AutomatedBetFragment;
import geert.berkers.primedice.Fragment.ProvablyFairFragment;
import geert.berkers.primedice.Thread.AutomatedBetThread;

/**
 * Primedice Application Created by Geert on 2-2-2016.
 */
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private User user;
    private String access_token, tipURL, logOutURL;

    private int betsStart, betCounter;
    private Long wageredStart;
    private double profitStart;
    private TextView notification;
    private ImageView closeNotification;

    private Activity activity;
    private LinearLayout drawer;
    private ListView menuListView;
    private FragmentManager manager;
    private MenuAdapter menuAdapter;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerListener;

    private BetFragment betFragment;
    private ChatFragment chatFragment;
    private StatsFragment statsFragment;
    private FaucetFragment faucetFragment;
    private ProfileFragment profileFragment;
    private ProvablyFairFragment provablyFairFragment;
    private AutomatedBetFragment automatedBetFragment;

    private AutomatedBetThread automatedBetThread;

    public User getUser() {
        return user;
    }

    public String getAccess_token() {
        return access_token;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initControls();
        setInformation();
    }

    // Set all views
    private void initControls() {
        activity = this;

        drawer = (LinearLayout) findViewById(R.id.drawer);
        menuListView = (ListView) findViewById(R.id.drawer_list);
        notification = (TextView) findViewById(R.id.txtNotification);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        closeNotification = (ImageView) findViewById(R.id.closeNotification);

        notification.setVisibility(View.INVISIBLE);
        closeNotification.setVisibility(View.INVISIBLE);

        closeNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notification.setVisibility(View.INVISIBLE);
                closeNotification.setVisibility(View.INVISIBLE);
            }
        });

        menuAdapter = new MenuAdapter(this);
        menuListView.setAdapter(menuAdapter);
        menuListView.setItemChecked(0, true);
        menuListView.setOnItemClickListener(this);
        menuListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        drawerListener = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.setDrawerListener(drawerListener);

        manager = getFragmentManager();

        betFragment = new BetFragment();
        chatFragment = new ChatFragment();
        statsFragment = new StatsFragment();
        faucetFragment = new FaucetFragment();
        profileFragment = new ProfileFragment();
        provablyFairFragment = new ProvablyFairFragment();
        automatedBetFragment = new AutomatedBetFragment();

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.primedice);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // Set the information from user
    private void setInformation() {
        setTitle("Bet");

        betCounter = 0;
        tipURL = "https://api.primedice.com/api/tip?access_token=";
        logOutURL = "https://api.primedice.com/api/logout?access_token=";

        Bundle b = getIntent().getExtras();

        try {
            user = b.getParcelable("userParcelable");
            access_token = b.getString("access_token");

            if (user != null) {
                this.wageredStart = user.getWageredAsLong();
                this.profitStart = user.getProfit();
                this.betsStart = user.getBets();

                Log.i("User", user.toString());

                FragmentTransaction transaction = manager.beginTransaction();
                transaction.add(R.id.content_frame, betFragment, "Bet");
                transaction.addToBackStack("Bet");
                transaction.commit();

                mSocket.connect();
            } else throw new Exception("User is null");
        } catch (Exception ex) {
            Log.e("NoUserFound", "Didn't find a user!");
            Log.e("NoUserFound", ex.toString());

            Intent loginActivityIntent = new Intent(this, LoginActivity.class);
            loginActivityIntent.putExtra("info", "User not found. Log in.");
            startActivity(loginActivityIntent);
            this.finish();
        }
    }

    // Tip the developer
    private void tipDeveloper() {
        String sathosiBaseTip = "0.00050001";
        final boolean[] firstEdit = {true};
        final EditText edTipAmount = new EditText(this);
        edTipAmount.setText(sathosiBaseTip);
        edTipAmount.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        edTipAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firstEdit[0]) {
                    edTipAmount.setText(null);
                    firstEdit[0] = false;
                }
            }
        });

        AlertDialog.Builder tipDialog = new AlertDialog.Builder(this);
        tipDialog.setTitle("Tip developer");
        tipDialog.setMessage("Enter amount:");
        tipDialog.setView(edTipAmount);
        tipDialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            double doubleTipValue = Double.valueOf(edTipAmount.getText().toString());

                            double toSatoshiMultiplier = 100000000;
                            double satoshi = doubleTipValue * toSatoshiMultiplier;

                            // Dirty fix for rounding math problems
                            if (satoshi - Double.valueOf(satoshi).intValue() >= 0.999) {
                                satoshi += 0.1;
                            }

                            int satoshiTip = (int) satoshi;

                            if (user.getBalance() < satoshi) {
                                Toast.makeText(getApplicationContext(), "Not enough balance to tip!", Toast.LENGTH_LONG).show();
                            } else if (satoshiTip < 50001) {
                                Toast.makeText(getApplicationContext(), "Tip must be at least 0.00050001 BTC!", Toast.LENGTH_LONG).show();
                            } else {
                                String urlParameters = "username=GeertDev" +
                                        "&amount=" + URLEncoder.encode(String.valueOf(satoshiTip), "UTF-8");

                                PostToServerTask tipDeveloperTask = new PostToServerTask();

                                try {
                                    String result = tipDeveloperTask.execute((tipURL + access_token), urlParameters).get();
                                    Log.i("Result", result);

                                    JSONObject jsonObject = new JSONObject(result);
                                    JSONObject jsonUser = jsonObject.getJSONObject("user");
                                    user.updateUserBalance(jsonUser.getString("balance"));

                                    updateBalanceInOpenedFragment();

                                    Toast.makeText(getApplicationContext(), "Tipped " + edTipAmount.getText().toString() + " BTC to GeertDev.", Toast.LENGTH_LONG).show();
                                } catch (InterruptedException | ExecutionException | JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
        tipDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }
        );
        tipDialog.show();
    }

    // Log out
    private void logOut(final Activity a) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Log out");
        alertDialog.setMessage("Are you sure?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {

                String result = "NoResult";
                try {
                    PostToServerTask logOutTask = new PostToServerTask();

                    result = logOutTask.execute(logOutURL + access_token, null).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

                if (result != null || result.equals("NoResult")) {
                    if (result != null || result.equals("NoResult")) {
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.clear();
                        editor.apply();

                        Intent loginActivityIntent = new Intent(a, LoginActivity.class);
                        loginActivityIntent.putExtra("info", "Succesfully logged out.");
                        startActivity(loginActivityIntent);
                        a.finish();
                    }
                }
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }
        );
        alertDialog.show();
    }

    // Start automated betting
    public void startAutomatedBetThread(int amount, String target, String condition, int numberOfRolls, boolean increaseOnWin, boolean increaseOnLoss, double increaseOnWinValue, double increaseOnLossValue) {
        automatedBetThread = new AutomatedBetThread(this, amount, target, condition, numberOfRolls, increaseOnWin, increaseOnLoss, increaseOnWinValue, increaseOnLossValue);

        Thread thread = new Thread() {
            @Override
            public void run() {
                automatedBetThread.startBetting();
            }
        };

        thread.start();
    }

    public boolean isBettingAutomatic() {
        if (automatedBetThread != null) {
            return true;
        } else {
            return false;
        }
    }

    public void stopAutomatedBetThread() {
        if (automatedBetThread != null) {
            automatedBetThread.betMade(true);
            automatedBetThread.requestStop();
            automatedBetThread = null;

            notifyAutomatedBetStopped();
        }
    }

    public Bet makeBet(int amount, String target, String condition) {
        automatedBetThread.betMade(false);

        if (amount > (int) user.getBalance()) {
            //TODO: Red alert with insufficient balance

            stopAutomatedBetThread();
            return null;
        } else {
            try {
                String urlParameters = "amount=" + URLEncoder.encode(String.valueOf(amount), "UTF-8") +
                        "&target=" + URLEncoder.encode(target, "UTF-8") +
                        "&condition=" + URLEncoder.encode(condition, "UTF-8");

                PostToServerTask postToServerTask = new PostToServerTask();
                String result = postToServerTask.execute(("https://api.primedice.com/api/bet?access_token=" + access_token), urlParameters).get();

                if (result != null) {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject jsonBet = jsonObject.getJSONObject("bet");
                    JSONObject jsonUser = jsonObject.getJSONObject("user");

                    user.updateUser(jsonUser);

                    Bet bet =  new Bet(jsonBet);
                    addBetAndBalanceInOpenedFragment(bet);

                    if(automatedBetThread != null) {
                        automatedBetThread.betMade(true);
                    }

                    return bet;
                }
                else{
                    //TODO: Inform user
                    //Result of bet was null. So it failed

                    stopAutomatedBetThread();
                    return null;
                }
            } catch (UnsupportedEncodingException | ExecutionException | InterruptedException | JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        menuListView.setItemChecked(position, true);
        getSupportActionBar().setTitle(menuAdapter.getItem(position));

        int backStack = manager.getBackStackEntryCount();
        int backStackIndex = backStack - 1;

        if (menuAdapter.getItem(position).equals("Bet")) {
            manager.popBackStack("Bet", 0);

        } else if (menuAdapter.getItem(position).equals("Profile")) {
            showFragment(profileFragment, backStackIndex, "Profile");
        } else if (menuAdapter.getItem(position).equals("Stats")) {
            showStats(backStackIndex);
        } else if (menuAdapter.getItem(position).equals("Chat")) {
            chatFragment.setInformation(access_token, mSocket);
            showFragment(chatFragment, backStackIndex, "Chat");
        } else if (menuAdapter.getItem(position).equals("Automated betting")) {
            showFragment(automatedBetFragment, backStackIndex, "Automated betting");
        } else if (menuAdapter.getItem(position).equals("Provably fair")) {
            showFragment(provablyFairFragment, backStackIndex, "Provably fair");
        } else if (menuAdapter.getItem(position).equals("Faucet")) {
            showFragment(faucetFragment, backStackIndex, "Provably fair");
        } else if (menuAdapter.getItem(position).equals("Tip Developer")) {
            tipDeveloper();
        } else if (menuAdapter.getItem(position).equals("Log out")) {
            logOut(this);
        }

        if (!menuAdapter.getItem(position).equals("Tip Developer")) {
            menuAdapter.setSelectedMenuItem(menuAdapter.getItem(position));
        }

        drawerLayout.closeDrawer(drawer);
    }

    private void showStats(int backStackIndex) {
        DecimalFormat format = new DecimalFormat("0.00000000");

        double wageredSessionDouble = (double) user.getWageredAsLong() - wageredStart;
        double profitSessionDouble = user.getProfit() - profitStart;
        int betsSessionInt = user.getBets() - betsStart;

        String wageredSession = format.format(wageredSessionDouble / 100000000);
        wageredSession = wageredSession.replace(",", ".");

        String profitSession = format.format(profitSessionDouble / 100000000);
        profitSession = profitSession.replace(",", ".");

        String betsSession = String.valueOf(betsSessionInt);

        statsFragment.setInformation(user, wageredSession, profitSession, betsSession);
        showFragment(statsFragment, backStackIndex, "Stats");
    }

    // Show the fragment
    private void showFragment(Fragment fragment, int backStackIndex, String fragmentName) {
        if (!manager.getBackStackEntryAt(backStackIndex).getName().equals(fragmentName)) {
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.content_frame, fragment, fragmentName);
            transaction.addToBackStack(fragmentName);
            transaction.commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerListener.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerListener.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(drawer)) {
            drawerLayout.closeDrawer(drawer);
        } else if (manager.getBackStackEntryCount() > 1) {
            manager.popBackStack();
            int index = manager.getBackStackEntryCount() - 2;
            String menuItem = manager.getBackStackEntryAt(index).getName();
            getSupportActionBar().setTitle(menuItem);

            menuAdapter.setSelectedMenuItem(menuItem);
        } else {
            Log.i("MainActivity", "Close");
            stopAutomatedBetThread();
            mSocket.disconnect();
            super.onBackPressed();
        }
    }

    // Get result from QR Code scanner
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanResult != null) {

            String currentFragment = manager.getBackStackEntryAt(manager.getBackStackEntryCount() - 1).getName();

            // These fragments have no current balance: Profile, Stats, Chat, Provably fair
            switch (currentFragment) {
                case "Bet":
                    betFragment.setWithdrawalAdress(scanResult.getContents());
                    break;
                case "Profile":
                    profileFragment.setEmergencyAddress(scanResult.getContents());
                    break;
                default:
                    break;
            }
        }
    }

    public void updateUser(User user) {
        this.user = user;
    }

    // Reset session wagered information
    public void resetSessionStats() {
        this.wageredStart = user.getWageredAsLong();
        this.profitStart = user.getProfit();
        this.betsStart = user.getBets();
    }

    // Show notification with text x for y seconds.
    // Use 0 if user must click it away him/herself
    private void showNotification(final String text, final int seconds) {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notification.setVisibility(View.VISIBLE);
                closeNotification.setVisibility(View.VISIBLE);
                notification.setText(text);

                if (seconds != 0) {
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                sleep(seconds * 1000);
                                hideNotification();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    thread.start();
                }
            }
        });
    }

    // Close (hide) current the notification
    private void hideNotification() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notification.setVisibility(View.INVISIBLE);
                closeNotification.setVisibility(View.INVISIBLE);
            }
        });
    }

    // Update balance in opened fragment
    private void updateBalanceInOpenedFragment() {
        String currentFragment = manager.getBackStackEntryAt(manager.getBackStackEntryCount() - 1).getName();

        String balance = user.getBalanceAsString();

        // These fragments have no current balance: Profile, Stats, Chat, Provably fair
        switch (currentFragment) {
            case "Bet":
                betFragment.updateBalance(balance);
                break;
            case "Automated betting":
                automatedBetFragment.updateBalance(balance);
                break;
            case "Faucet":
                faucetFragment.updateBalance(balance);
                break;
            default:
                break;
        }
    }

    // Add bet and update balance in opened fragment
    public void addBetAndBalanceInOpenedFragment(Bet bet){
        String currentFragment = manager.getBackStackEntryAt(manager.getBackStackEntryCount() - 1).getName();

        String balance = user.getBalanceAsString();

        switch (currentFragment) {
            case "Bet":
                betFragment.updateBalance(balance);
                betFragment.addBet(bet, false, true);
                break;
            case "Automated betting":
                automatedBetFragment.updateBalance(balance);
                automatedBetFragment.addBet(bet);
                break;
            case "Faucet":
                faucetFragment.updateBalance(balance);
                break;
            default:
                break;
        }
    }

    // Notify fragments that automatic betting stopped
    public void notifyAutomatedBetStopped(){
        String currentFragment = manager.getBackStackEntryAt(manager.getBackStackEntryCount() - 1).getName();

        switch (currentFragment) {
            case "Bet":
                betFragment.notifyAutomatedBetStopped();
                break;
            case "Automated betting":
                automatedBetFragment.notifyAutomatedBetStopped();
                break;
            default:
                break;
        }
    }

    private final Emitter.Listener socketioConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.i("MainActivity", "Socket connected");
            mSocket.emit("user", access_token);
            mSocket.emit("chat");
            mSocket.emit("stats");
            mSocket.emit("alert");
        }
    };

    private final Emitter.Listener socketioTip = new Emitter.Listener() {
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
                String notificationInformation = "Received tip of " + amountString + " BTC from " + sendername;

                double balanceBeforeTip = user.getBalance();
                double newBalance = balanceBeforeTip + amountDouble;
                user.setBalance(newBalance);

                updateBalanceInOpenedFragment();
                showNotification(notificationInformation, 15);

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    };

    // Get all new bets (all and HR)
    private final Emitter.Listener socketioBet = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject obj = (JSONObject) args[0];

            try {
                Bet b = new Bet(obj);

                // Check if bet is HR bet or Normal bet
                if (b.getAmount() >= 10000000) {
                    betCounter++;
                    betFragment.addBet(b, true, false);
                } else {
                    //Only add 1/3 else to fast for application
                    betCounter++;
                    if (betCounter == 3) {
                        betFragment.addBet(b, false, false);
                        betCounter = 0;
                    }
                }
            } catch (Exception ex) {
                Log.e("BetException", ex.toString());
            }
        }
    };

    private final Emitter.Listener socketioDeposit = new Emitter.Listener() {
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

                if (!acredited) {
                    // Show notification for receiving
                    String text = "Received deposit of " + amountString + " BTC - Awaiting one confirmation";
                    showNotification(text, 0);
                } else {
                    // Show notification for confirmed deposit
                    double balanceBeforeTip = user.getBalance();
                    double newBalance = balanceBeforeTip + amountDouble;
                    user.setBalance(newBalance);

                    String text = "Confirmed deposit of " + amount + " BTC - Amount credited";
                    updateBalanceInOpenedFragment();

                    // Maybe even a notification in android!
                    showNotification(text, 15);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    private final Emitter.Listener socketioAlert = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject obj = (JSONObject) args[0];
            Log.w("MainActivity", "Alert Result: " + obj.toString());
        }
    };

    private final Emitter.Listener socketioSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject obj = (JSONObject) args[0];
            Log.w("MainActivity", "Succes Result: " + obj.toString());
        }
    };

    private final Emitter.Listener socketioError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject obj = (JSONObject) args[0];
            Log.w("MainActivity", "Error Result: " + obj.toString());
        }
    };

    private final Emitter.Listener socketioDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("MainActivity", "Socket disconnected");
        }
    };

    private Socket mSocket;

    {
        try {
            mSocket = IO.socket("https://sockets.primedice.com");

            mSocket.on(Socket.EVENT_CONNECT, socketioConnect)         // Connect sockets
                    .on("tip", socketioTip)                           // Get tip
                    .on("bet", socketioBet)                           // Add bets to all bets or highrollers
                    .on("deposit", socketioDeposit)                   // Get information about deposit
                            //        .on("msg", socketioMSG)                           // Get new messages
                            //        .on("pm", socketioPM)                             // Handle PM's

                    .on("alert", socketioAlert)                       // ...??
                    .on("success", socketioSuccess)                   // ...??
                    .on("err", socketioError)                         // ...??
                    .on("onError", socketioError)                         // ...??
                    .on(Socket.EVENT_DISCONNECT, socketioDisconnect); // Disconnect sockets
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    // TODO: General things to complete this application:
    // Encrypt access_token
    // Improve chat
    // Remove all toasts for alerts
    // Bet failed multiple times in a row? -> Change seed!
    // Notification tip/deposit
    // Settings screen (alert time/notification/faucet timer/time settings)
    // Register account (Add in login activity)
    // Timer faucet

    // Update UI
    // - Check differences with site and fix it
    // - Update chat UI
    // - Scroll to new bet when adding it
}

