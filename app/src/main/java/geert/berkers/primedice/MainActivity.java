package geert.berkers.primedice;

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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;


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
                this.wageredStart = user.wagered;
                this.profitStart = user.profit;
                this.betsStart = user.bets;

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

                            if (user.balance < satoshi) {
                                Toast.makeText(getApplicationContext(), "Not enough balance to tip!", Toast.LENGTH_LONG).show();
                            } else if (satoshiTip < 50001) {
                                Toast.makeText(getApplicationContext(), "Tip must be at least 0.00050001 BTC!", Toast.LENGTH_LONG).show();
                            } else {
                                // if (satoshiTip >= 50001 && doubleTipValue > user.balance) {
                                TipDeveloperTask tipDeveloperTask = new TipDeveloperTask();

                                try {
                                    String result = tipDeveloperTask.execute((tipURL + access_token), "GeertDev", String.valueOf(satoshiTip)).get();
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
                    LogOutTask logOutTask = new LogOutTask();

                    result = logOutTask.execute(logOutURL + access_token).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

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
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }
        );
        alertDialog.show();
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

        menuAdapter.setSelectedMenuItem(menuAdapter.getItem(position));
        drawerLayout.closeDrawer(drawer);
    }

    private void showStats(int backStackIndex) {
        DecimalFormat format = new DecimalFormat("0.00000000");

        double wageredSessionDouble = (double) user.wagered - wageredStart;
        double profitSessionDouble = user.profit - profitStart;
        int betsSessionInt = user.bets - betsStart;

        String wageredSession = format.format(wageredSessionDouble / 100000000);
        wageredSession = wageredSession.replace(",", ".");

        String profitSession = format.format(profitSessionDouble / 100000000);
        profitSession = profitSession.replace(",", ".");

        String betsSession = String.valueOf(betsSessionInt);

        statsFragment.setInformation(user, wageredSession, profitSession, betsSession);
        showFragment(statsFragment, backStackIndex, "Stats");
    }

    // Show the fragment
    public void showFragment(Fragment fragment, int backStackIndex, String fragmentName) {
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
            Log.i("Backbutton", "Vorig fragment");

            manager.popBackStack();
            int index = manager.getBackStackEntryCount() - 2;
            String menuItem = manager.getBackStackEntryAt(index).getName();
            getSupportActionBar().setTitle(menuItem);

            menuAdapter.setSelectedMenuItem(menuItem);

        } else {
            Log.i("MainActivity", "Close");
            mSocket.disconnect();
            super.onBackPressed();
        }
    }

    // Get result from QR Code scanner
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            betFragment.setWithdrawalAdress(scanResult.getContents());
        }
    }

    public void updateUser(User user) {
        this.user = user;
    }

    // Reset session wagered information
    public void resetSessionStats() {
        this.wageredStart = user.wagered;
        this.profitStart = user.profit;
        this.betsStart = user.bets;
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

    private void updateBalanceInOpenedFragment() {
        //TODO: Update balance in opened fragment
    }

    private final Emitter.Listener socketioConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.i("ChatFragment", "Socket connected");
            mSocket.emit("user", access_token);
            mSocket.emit("chat");
            mSocket.emit("stats");
        }
    };

    private final Emitter.Listener socketioTip = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject obj = (JSONObject) args[0];
            Log.i("ChatFragment", "TIP Result: " + obj.toString());

            try {
                String sendername = obj.getString("sender");
                int amount = obj.getInt("amount");
                double amountDouble = (double) amount;
                DecimalFormat format = new DecimalFormat("0.00000000");
                String amountString = format.format(amountDouble / 100000000);
                String notificationInformation = "Received tip of " + amountString + " BTC from " + sendername;

                double balanceBeforeTip = user.balance;
                user.balance = balanceBeforeTip + amountDouble;

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
                    betFragment.addBet(b, true);
                } else {
                    //Only add 1/3 else to fast for application
                    betCounter++;
                    if (betCounter == 3) {
                        betFragment.addBet(b, false);
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
            Log.i("ChatFragment", "Deposit Result: " + obj.toString());

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
                    double balanceBeforeTip = user.balance;
                    user.balance = balanceBeforeTip + amountDouble;

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
            Log.w("ChatFragment", "Alert Result: " + obj.toString());
        }
    };

    private final Emitter.Listener socketioSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject obj = (JSONObject) args[0];
            Log.w("ChatFragment", "Succes Result: " + obj.toString());
        }
    };

    private final Emitter.Listener socketioError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject obj = (JSONObject) args[0];
            Log.w("ChatFragment", "Error Result: " + obj.toString());
        }
    };

    private final Emitter.Listener socketioDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("Chatfragment", "Socket disconnected");
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

                    .on("alert", socketioAlert)                       // ...??
                    .on("success", socketioSuccess)                   // ...??
                    .on("err", socketioError)                         // ...??

                    .on(Socket.EVENT_DISCONNECT, socketioDisconnect); // Disconnect sockets
                    //.on("stats", socketioStats)                     // Get stats from site (bets in 24h and wagered)

                /*
                    private final Emitter.Listener socketioStats = new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        JSONObject obj = (JSONObject) args[0];
                        // This gets stats (BTC WON LAST 24 HOURS)
                        //Stats Result: {"bets24":19462106,"wagered24":1.1444498764200024E11}
                        }
                    };
                */
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    // TODO: General things to complete this application:
    // Encrypt access_token
    // Improve chat
    // Automated betting
    // Register account
    // Set password
    // Set Email
    // Set Emergency adress
    // Use 2FA Authentication
    // Implement Affiliate information
    // Get deposits/withdrawals
    // Change seed
    // Claim faucet
}

