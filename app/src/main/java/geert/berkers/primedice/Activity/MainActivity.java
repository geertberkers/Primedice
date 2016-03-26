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
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.Socket;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.integration.android.IntentIntegrator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import geert.berkers.primedice.Data.Message;
import geert.berkers.primedice.Data.URL;
import geert.berkers.primedice.DataHandler.GetFromServerTask;
import geert.berkers.primedice.DataHandler.PostToServerTask;
import geert.berkers.primedice.DataHandler.SocketIO;
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

    // <editor-fold defaultstate="collapsed" desc="Fields...">
    private Socket socket;
    private static User user;
    private static int betErrorCounter;
    private static String access_token;
    private static TextView notification;
    private static ImageView closeNotification;

    private int betsStart;
    private Long wageredStart;
    private double profitStart;

    private static Activity activity;
    private LinearLayout drawer;
    private ListView menuListView;
    private static FragmentManager manager;
    private MenuAdapter menuAdapter;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerListener;

    private StatsFragment statsFragment;
    private ProfileFragment profileFragment;
    private ProvablyFairFragment provablyFairFragment;

    private static BetFragment betFragment;
    private static ChatFragment chatFragment;
    private static FaucetFragment faucetFragment;
    private static AutomatedBetFragment automatedBetFragment;

    private AutomatedBetThread automatedBetThread;

    public static ArrayList<String> allowedLinksInChat = new ArrayList<>();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="onCreate methods...">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("MainActivity Created");
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
        Bundle b = getIntent().getExtras();

        betErrorCounter = 0;

        try {
            user = b.getParcelable("userParcelable");
            access_token = b.getString("access_token");

            System.out.println("User created");
            if (user != null) {
                this.wageredStart = user.getWageredAsLong();
                this.profitStart = user.getProfit();
                this.betsStart = user.getBets();

                Log.i("LOGGED_IN_USER", user.toString());

                FragmentTransaction transaction = manager.beginTransaction();
                transaction.add(R.id.content_frame, betFragment, "Bet");
                transaction.addToBackStack("Bet");
                transaction.commit();
                System.out.println("Betfragment commited");

                GetFromServerTask getAllowedLinksForChat = new GetFromServerTask();
                String result = getAllowedLinksForChat.execute(URL.GET_ALLOWED_CHAT_LINKS).get();

                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonSite = jsonArray.getJSONObject(i);
                    allowedLinksInChat.add(jsonSite.getString("site"));
                }

                System.out.println("Allowed Links created");
                chatFragment.setMessagesBeforeCreate(access_token, this);


                System.out.println("Start sockets");
                // Wait for everything to get loaded, then start sockets
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        socket = SocketIO.getInstance();
                        socket.connect();
                        socket.emit("user", access_token);
                        socket.emit("chat");
                        socket.emit("stats");
                        System.out.println("Sockets started!");
                    }
                }, 5000);

            } else throw new Exception("User is null");
        } catch (Exception ex) {
            ex.printStackTrace();

            Intent loginActivityIntent = new Intent(this, LoginActivity.class);
            loginActivityIntent.putExtra("info", "User not found. Log in.");
            startActivity(loginActivityIntent);
            this.finish();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Override and layout methods...">
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

    // Get result from QR Code scanner
    @Override
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

            if (socket != null) {
                socket.disconnect();
            }

            stopAutomatedBetThread();

            // If account isn't remembered, logout
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            boolean rememberMe = sharedPref.getBoolean("remember_me", false);

            if (!rememberMe) {
                logOut(this);
            }

            super.onBackPressed();
        }
    }

    // Set title and opened menu
    private void setTitleAndOpenedMenuItem(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            menuAdapter.setSelectedMenuItem(title);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters...">
    public static User getUser() {
        return user;
    }

    public String getAccess_token() {
        return access_token;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Log out methods...">
    // Log out without password set
    private void logOutNoPasswordSet(final Activity a) {
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
    private void logOutPasswordSet(final Activity a) {
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
    private void logOut(Activity a) {
        try {
            PostToServerTask logOutTask = new PostToServerTask();
            String result = logOutTask.execute(URL.LOG_OUT + access_token, null).get();

            if (result != null) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.clear();
                editor.apply();

                Intent loginActivityIntent = new Intent(a, LoginActivity.class);
                loginActivityIntent.putExtra("info", "Successfully logged out.");
                startActivity(loginActivityIntent);
                a.finish();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Automatic bets methods...">

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

                    boolean highRoller = false;
                    if (bet.getProfit() >= 10000000) {
                        highRoller = true;
                    }
                    addBetAndBalanceInOpenedFragment(bet, highRoller, true);

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

    // Notify fragments that automatic betting stopped
    private void notifyAutomatedBetStopped() {
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

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Static methods...">
    // Get user from server
    public static void updateUser() {
        user = LoginActivity.getUser(access_token);
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
    public static void tipUser(final Activity a, final String userToTip) {
        final Activity temp;
        if (a == null) {
            temp = activity;
        } else {
            temp = a;
        }

        final EditText edTipAmount = createEditTextTipAmount(temp);

        AlertDialog.Builder tipDialog = new AlertDialog.Builder(temp);
        tipDialog.setTitle("TIP " + userToTip.toUpperCase());
        tipDialog.setMessage("Tipping is an irreversible action.\nEnter amount:");
        tipDialog.setView(edTipAmount);
        tipDialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        doTip(edTipAmount, userToTip);
                    }
                }
        );
        tipDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        tipDialog.show();
    }

    private static EditText createEditTextTipAmount(Activity a) {
        String sathosiBaseTip = "0.00050001";
        final boolean[] firstEdit = {true};
        final EditText edTipAmount = new EditText(a);
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

        return edTipAmount;
    }

    // Tip the user
    private static void doTip(EditText edTipAmount, String username) {
        try {
            double doubleTipValue = Double.valueOf(edTipAmount.getText().toString());

            double toSatoshiMultiplier = 100000000;
            double satoshi = doubleTipValue * toSatoshiMultiplier;

            // Dirty fix for rounding math problems
            if (satoshi - Double.valueOf(satoshi).intValue() >= 0.999) {
                satoshi += 0.1;
            }

            int satoshiTip = (int) satoshi;

            if (satoshiTip < 50001) {
                Toast.makeText(edTipAmount.getContext(), "Tip must be at least 0.00050001 BTC!", Toast.LENGTH_LONG).show();
            } else if (user.getBalance() < satoshi) {
                showNotification(true, "Insufficient funds", 5);
            } else {
                String urlParameters = "username=" + URLEncoder.encode(username, "UTF-8") +
                        "&amount=" + URLEncoder.encode(String.valueOf(satoshiTip), "UTF-8");

                PostToServerTask tipDeveloperTask = new PostToServerTask();

                try {
                    String result = tipDeveloperTask.execute((URL.TIP + access_token), urlParameters).get();
                    Log.i("Result", result);

                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject jsonUser = jsonObject.getJSONObject("user");
                    user.updateUserBalance(jsonUser.getString("balance"));

                    updateBalanceInOpenedFragment();

                    Toast.makeText(edTipAmount.getContext(), "Tipped " + edTipAmount.getText().toString() + " BTC to GeertDev.", Toast.LENGTH_LONG).show();
                } catch (InterruptedException | ExecutionException | JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Show notification with text x for y seconds.
    // Use 0 if user must click it away him/herself
    public static void showNotification(final boolean error, final String text, final int seconds) {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                notification.setText(text);
                notification.setVisibility(View.VISIBLE);
                closeNotification.setVisibility(View.VISIBLE);

                if (error) {
                    notification.setBackgroundResource(R.drawable.error);
                } else {
                    notification.setBackgroundResource(R.drawable.notification);
                }

                if (text.equals("Bet error") || text.equals("Betting to fast")) {
                    betErrorCounter++;

                    if (betErrorCounter >= 5) {
                        notification.setText("Change seed!");
                        betErrorCounter = 0;
                    }
                } else {
                    betErrorCounter = 0;
                }

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
    public static void addBetAndBalanceInOpenedFragment(Bet bet, boolean highRoller, boolean ownBet) {
        String currentFragment = manager.getBackStackEntryAt(manager.getBackStackEntryCount() - 1).getName();

        String balance = user.getBalanceAsString();

        switch (currentFragment) {
            case "Bet":
                betFragment.addBet(bet, highRoller, ownBet);
                break;
            case "Automated betting":
                if (ownBet) {
                    automatedBetFragment.updateBalance(balance);
                    automatedBetFragment.addBet(bet);
                }
                break;
            case "Faucet":
                faucetFragment.updateBalance(balance);
                break;
            default:
                break;
        }
    }

    // Add message if fragment is opened
    public static void addMessageShowIfOpened(Message message) {
        String currentFragment = manager.getBackStackEntryAt(manager.getBackStackEntryCount() - 1).getName();

        boolean showMessages = false;
        if (currentFragment.equals("Chat")) {
            showMessages = true;
        }
        chatFragment.addMessage(message, showMessages);
    }
    //</editor-fold>

    // TODO: General things to complete this application:

    // Update UI (style/font)
    // - Check website style
    // - Check fonts

    // Maybe for a later version:
    // Notification tip/deposit
    // Settings screen (alert time/notifications/faucet timer)

    // Reset session wagered information
    public void resetSessionStats() {
        this.wageredStart = user.getWageredAsLong();
        this.profitStart = user.getProfit();
        this.betsStart = user.getBets();
    }

    // Tip developer (Extra else
    private void tipDeveloper() {
        final EditText edTipAmount = createEditTextTipAmount(this);

        AlertDialog.Builder tipDialog = new AlertDialog.Builder(this);
        tipDialog.setTitle("Tip developer");
        tipDialog.setMessage("Enter amount:");
        tipDialog.setView(edTipAmount);
        tipDialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        doTip(edTipAmount, "GeertDev");
                        setTitleAndOpenedMenuItem(manager.getBackStackEntryAt(manager.getBackStackEntryCount() - 1).getName());
                    }
                }
        );
        tipDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                setTitleAndOpenedMenuItem(manager.getBackStackEntryAt(manager.getBackStackEntryCount() - 1).getName());
            }
        });
        tipDialog.show();
    }

    public void openFaucetFragment(View v) {
        int backStackIndex = manager.getBackStackEntryCount() - 1;
        setTitleAndOpenedMenuItem("Faucet");
        showFragment(faucetFragment, backStackIndex, "Faucet");
    }
}

