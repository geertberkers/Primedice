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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;


/**
 * Primedice Application Created by Geert on 2-2-2016.
 */
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private User user;
    private String access_token, tipURL, logOutURL;

    private int betsStart;
    private Long wageredStart;
    private double profitStart;

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
        drawer = (LinearLayout) findViewById(R.id.drawer);
        menuListView = (ListView) findViewById(R.id.drawer_list);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        menuAdapter = new MenuAdapter(this);
        menuListView.setAdapter(menuAdapter);
        menuListView.setItemChecked(0, true);
        menuListView.setOnItemClickListener(this);
        menuListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final InputMethodManager fimm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        drawerListener = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                fimm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
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

                Log.w("User", user.toString());

                FragmentTransaction transaction = manager.beginTransaction();
                transaction.add(R.id.content_frame, betFragment, "Bet");
                transaction.addToBackStack("Bet");
                transaction.commit();

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

                                    //TODO Update balance in opened fragment!
                                    //txtBalance.setText(user.getBalance());

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
            DecimalFormat format = new DecimalFormat("0.00000000");

            double wageredSessionDouble = (double) user.wagered - wageredStart;
            double profitSessionDouble = user.profit - profitStart;
            int betsSessionInt = user.bets - betsStart;

            String wageredSession = format.format(wageredSessionDouble / 100000000);
            wageredSession = wageredSession.replace(",",".");

            String profitSession = format.format(profitSessionDouble / 100000000);
            profitSession = profitSession.replace(",",".");

            String betsSession = String.valueOf(betsSessionInt);

            statsFragment.setInformation(user, wageredSession, profitSession, betsSession);
            showFragment(statsFragment, backStackIndex, "Stats");
        } else if (menuAdapter.getItem(position).equals("Chat")) {
            chatFragment.setInformation(access_token);
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
            Log.w("Backbutton", "Vorig fragment");

            manager.popBackStack();
            int index = manager.getBackStackEntryCount() - 2;
            String menuItem = manager.getBackStackEntryAt(index).getName();
            getSupportActionBar().setTitle(menuItem);

            menuAdapter.setSelectedMenuItem(menuItem);

        } else {
            Log.i("MainActivity", "Close");
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

    public void resetSessionStats(){
        this.wageredStart = user.wagered;
        this.profitStart = user.profit;
        this.betsStart = user.bets;
    }


}