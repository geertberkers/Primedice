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

import geert.berkers.primedice.Data.URL;
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

    private static User user;
    private static String access_token;

    private int betsStart, betCounter;
    private Long wageredStart;
    private double profitStart;
    private static TextView notification;
    private static ImageView closeNotification;

    private static Activity activity;
    private LinearLayout drawer;
    private ListView menuListView;
    private static FragmentManager manager;
    private MenuAdapter menuAdapter;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerListener;

    private static BetFragment betFragment;
    private ChatFragment chatFragment;
    private StatsFragment statsFragment;
    private static FaucetFragment faucetFragment;
    private ProfileFragment profileFragment;
    private ProvablyFairFragment provablyFairFragment;
    private static AutomatedBetFragment automatedBetFragment;

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
                if (getCurrentFocus() != null) {
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setIcon(R.mipmap.primedice);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    // Set the information from user
    private void setInformation() {
        setTitleAndOpenedMenuItem("Bet");

        betCounter = 0;

        Bundle b = getIntent().getExtras();

        try {
            user = b.getParcelable("userParcelable");
            access_token = b.getString("access_token");

            if (user != null) {
                this.wageredStart = user.getWageredAsLong();
                this.profitStart = user.getProfit();
                this.betsStart = user.getBets();

                Log.i("LOGGED_IN_USER", user.toString());

                FragmentTransaction transaction = manager.beginTransaction();
                transaction.add(R.id.content_frame, betFragment, "Bet");
                transaction.addToBackStack("Bet");
                transaction.commit();

                mSocket.connect();
            } else throw new Exception("User is null");
        } catch (Exception ex) {
            ex.printStackTrace();

            Intent loginActivityIntent = new Intent(this, LoginActivity.class);
            loginActivityIntent.putExtra("info", "User not found. Log in.");
            startActivity(loginActivityIntent);
            this.finish();
        }
    }

    // send PM
    public static void sendPM(Activity a, final String toUsername) {
        if (a == null) {
            a = activity;
        }

        final EditText edMessage = new EditText(a);

        AlertDialog.Builder pmDialog = new AlertDialog.Builder(a);
        pmDialog.setTitle("MESSAGE " + toUsername);
        pmDialog.setMessage("This message will only be seen by " + toUsername);
        pmDialog.setView(edMessage);
        pmDialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                String message = edMessage.getText().toString();
                try {
                    String urlParameters = "room=" + URLEncoder.encode(ChatFragment.getRoom(), "UTF-8")
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

    // Tip user
    public static boolean tipUser(final Activity a, final String userToTip) {
        final boolean[] firstEdit = {true};
        final boolean[] tipFinished = {false};

        final Activity temp;
        if (a == null) {
            temp = activity;
        } else {
            temp = a;
        }

        String sathosiBaseTip = "0.00050001";
        final EditText edTipAmount = new EditText(temp);

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

        AlertDialog.Builder tipDialog = new AlertDialog.Builder(temp);
        tipDialog.setTitle("TIP " + userToTip.toUpperCase());
        tipDialog.setMessage("Tipping is an irreversible action.\nEnter amount:");
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
                                showNotification(true, "Insufficient funds", 5);
                            } else if (satoshiTip < 50001) {
                                Toast.makeText(activity, "Tip must be at least 0.00050001 BTC!", Toast.LENGTH_LONG).show();
                            } else {
                                String urlParameters = "username=" + URLEncoder.encode(userToTip, "UTF-8")
                                        + "&amount=" + URLEncoder.encode(String.valueOf(satoshiTip), "UTF-8");

                                PostToServerTask tipDeveloperTask = new PostToServerTask();

                                try {
                                    String result = tipDeveloperTask.execute((URL.TIP + access_token), urlParameters).get();
                                    Log.i("TIP_RESULT", result);

                                    JSONObject jsonObject = new JSONObject(result);
                                    JSONObject jsonUser = jsonObject.getJSONObject("user");
                                    user.updateUserBalance(jsonUser.getString("balance"));

                                    updateBalanceInOpenedFragment();

                                    tipFinished[0] = true;

                                    Toast.makeText(activity, "Tipped " + edTipAmount.getText().toString() + " BTC to GeertDev.", Toast.LENGTH_LONG).show();
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
                tipFinished[0] = false;
            }
        });
        tipDialog.show();

        return tipFinished[0];
    }

    // Tip the developer
    private void tipDeveloper() {

        if (!tipUser(null, "GeertDev")) {
            setTitleAndOpenedMenuItem(manager.getBackStackEntryAt(manager.getBackStackEntryCount() - 1).getName());
        }
    }

    // Log out without password set
    public void logOutNoPasswordSet(final Activity a) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Are you sure?");
        alertDialog.setMessage("It looks like your account doesn't have a password set. Logging out of an unpassworded acccount means it will be forever gone and inaccessible.");
        alertDialog.setPositiveButton("LOGOUT", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                logOut(a);
            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                setTitleAndOpenedMenuItem(manager.getBackStackEntryAt(manager.getBackStackEntryCount() - 1).getName());
            }
        });
        alertDialog.setNeutralButton("SET PASSWORD", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                setTitleAndOpenedMenuItem("Profile");
                showFragment(profileFragment, manager.getBackStackEntryCount() - 1, "Profile");
            }
        });
        alertDialog.show();
    }

    // Log out with password set
    public void logOutPasswordSet(final Activity a) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Log out");
        alertDialog.setMessage("Are you sure?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                logOut(a);
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                setTitleAndOpenedMenuItem(manager.getBackStackEntryAt(manager.getBackStackEntryCount() - 1).getName());
            }
        });
        alertDialog.show();
    }

    // Log out
    public void logOut(Activity a) {
        String result = "NoResult";
        try {
            PostToServerTask logOutTask = new PostToServerTask();

            result = logOutTask.execute(URL.LOG_OUT + access_token, null).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        if (result != null) {
            if (!result.equals("NoResult")) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.clear();
                editor.apply();

                Intent loginActivityIntent = new Intent(a, LoginActivity.class);
                loginActivityIntent.putExtra("info", "Successfully logged out.");
                startActivity(loginActivityIntent);
                a.finish();
            }
        }
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

    // Check if application is betting automatic
    public boolean isBettingAutomatic() {
        return automatedBetThread != null;
    }

    // Stop automatic betting
    public void stopAutomatedBetThread() {
        if (automatedBetThread != null) {
            automatedBetThread.betMade(true);
            automatedBetThread.requestStop();
            automatedBetThread = null;

            notifyAutomatedBetStopped();
        }
    }

    // Place the automatic bet
    public Bet makeBet(int amount, String target, String condition) {
        automatedBetThread.betMade(false);

        if (amount > (int) user.getBalance()) {
            showNotification(true, "Insufficient funds", 5);
            stopAutomatedBetThread();
            return null;
        } else {
            try {
                String urlParameters = "amount=" + URLEncoder.encode(String.valueOf(amount), "UTF-8") +
                        "&target=" + URLEncoder.encode(target, "UTF-8") +
                        "&condition=" + URLEncoder.encode(condition, "UTF-8");

                PostToServerTask postToServerTask = new PostToServerTask();
                String result = postToServerTask.execute((URL.BET + access_token), urlParameters).get();

                if (result != null) {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject jsonBet = jsonObject.getJSONObject("bet");
                    JSONObject jsonUser = jsonObject.getJSONObject("user");

                    user.updateUser(jsonUser);

                    Bet bet = new Bet(jsonBet);
                    addBetAndBalanceInOpenedFragment(bet);

                    if (automatedBetThread != null) {
                        automatedBetThread.betMade(true);
                    }

                    return bet;
                } else {
                    showNotification(true, "Bet error", 5);

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
        setTitleAndOpenedMenuItem(menuAdapter.getItem(position));

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
            showFragment(faucetFragment, backStackIndex, "Faucet");
        } else if (menuAdapter.getItem(position).equals("Tip Developer")) {
            tipDeveloper();
        } else if (menuAdapter.getItem(position).equals("Log out")) {
            // Check if password set
            if (!user.getPasswordSet()) {
                logOutNoPasswordSet(this);
            } else {
                logOutPasswordSet(this);
            }
        }

        drawerLayout.closeDrawer(drawer);
    }

    // Show statistics and set information
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
            setTitleAndOpenedMenuItem(menuItem);
        } else {

            Log.i("MainActivity", "Close application");
            stopAutomatedBetThread();
            mSocket.disconnect();

            // If account isn't remembered, logout
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            boolean rememberMe = sharedPref.getBoolean("remember_me", false);

            if (!rememberMe) {
                logOut(this);
            }

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

    public void updateUser(User updatedUser) {
        user = updatedUser;
    }

    // Reset session wagered information
    public void resetSessionStats() {
        this.wageredStart = user.getWageredAsLong();
        this.profitStart = user.getProfit();
        this.betsStart = user.getBets();
    }

    // Show notification with text x for y seconds.
    // Use 0 if user must click it away him/herself
    public static void showNotification(final boolean error, final String text, final int seconds) {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (error) {
                    notification.setBackgroundResource(R.drawable.error);
                } else {
                    notification.setBackgroundResource(R.drawable.notification);
                }

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
    private static void hideNotification() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notification.setVisibility(View.INVISIBLE);
                closeNotification.setVisibility(View.INVISIBLE);
            }
        });
    }

    // Update balance in opened fragment
    public static void updateBalanceInOpenedFragment() {
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
    public void addBetAndBalanceInOpenedFragment(Bet bet) {
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
    public void notifyAutomatedBetStopped() {
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

    // Set title and opened menu
    public void setTitleAndOpenedMenuItem(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            menuAdapter.setSelectedMenuItem(title);
        }
    }

    public static String getUserName(){
        return user.getUsername();
    }
    private final Emitter.Listener socketioConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.i("MainActivity", "Socket connected");
            mSocket.emit("user", access_token);
            mSocket.emit("chat");
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
                showNotification(false, notificationInformation, 15);

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
                ex.printStackTrace();
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
                    showNotification(false, text, 0);
                } else {
                    // Show notification for confirmed deposit
                    double balanceBeforeTip = user.getBalance();
                    double newBalance = balanceBeforeTip + amountDouble;
                    user.setBalance(newBalance);

                    String text = "Confirmed deposit of " + amount + " BTC - Amount credited";
                    updateBalanceInOpenedFragment();

                    // Maybe even a notification in android!
                    showNotification(false, text, 15);
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
            mSocket = IO.socket(URL.SOCKETS);

            mSocket.on(Socket.EVENT_CONNECT, socketioConnect)         // Connect sockets
                    .on("tip", socketioTip)                           // Get tip
                    .on("bet", socketioBet)                           // Add bets to all bets or highrollers
                    .on("deposit", socketioDeposit)                   // Get information about deposit
                    .on("alert", socketioAlert)                       // ...??
                    .on("success", socketioSuccess)                   // ...??
                    .on("err", socketioError)                         // ...??
                    .on(Socket.EVENT_DISCONNECT, socketioDisconnect); // Disconnect sockets
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    // TODO: General things to complete this application:
    // Bet failed multiple times in a row? -> Change seed!

    // Clickable links in chat (With API for allowed sites)
    // Notification tip/deposit
    // Settings screen (alert time/notification/faucet timer/time settings)

    // Update UI
    // - Check differences with site and fix it
    // - Update chat UI
    // - Scroll to new bet when adding it
}

