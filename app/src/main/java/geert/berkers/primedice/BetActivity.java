package geert.berkers.primedice;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Geert on 23-1-2016.
 */
public class BetActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private User user;
    private boolean betHigh;
    private DecimalFormat format;
    private ArrayList<Bet> recentBets, myBets;
    private String betURL, access_token;

    private int betAmount;
    private double betMultiplier, betPercentage;

    private ListView listView;
    private MenuAdapter menuAdapter;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerListener;

    private ListView betListView;
    private EditText edBetAmount, edProfitonWin;
    private TextView txtBalance, txtMyBets, txtAllBets, txtHighRollers;
    private Button btnHalf, btnDouble, btnMax, btnHighLow, btnMultiplier, btnPercentage, btnRollDice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bet);

        listView = (ListView) findViewById(R.id.drawerList);
        txtBalance = (TextView) findViewById(R.id.txtBalance);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        txtMyBets = (TextView) findViewById(R.id.txtMyBets);
        txtAllBets = (TextView) findViewById(R.id.txtAllBets);
        txtHighRollers = (TextView) findViewById(R.id.txtHighRollers);

        betListView = (ListView) findViewById(R.id.betsListView);

        edBetAmount = (EditText) findViewById(R.id.edBetAmount);
        edProfitonWin = (EditText) findViewById(R.id.edProfitonWin);

        
        btnHalf = (Button) findViewById(R.id.btnHalfBet);
        btnDouble = (Button) findViewById(R.id.btnDoubleBet);
        btnMax = (Button) findViewById(R.id.btnMaxBet);
        btnHighLow = (Button) findViewById(R.id.btnHighLow);
        btnMultiplier = (Button) findViewById(R.id.btnMultiplier);
        btnPercentage = (Button) findViewById(R.id.btnPercentage);
        btnRollDice = (Button) findViewById(R.id.btnRollDice);

        menuAdapter = new MenuAdapter(this);
        listView.setAdapter(menuAdapter);
        listView.setOnItemClickListener(this);

        drawerListener = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
        };

        drawerLayout.setDrawerListener(drawerListener);

        setTitle("Home");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.primedice);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.primedicecolor)));

        betHigh = false;
        betAmount = 0;
        betMultiplier = 2.0;
        betPercentage = 49.50;
        format = new DecimalFormat("0.00000000");

        myBets = new ArrayList<>();
        recentBets = new ArrayList<>();
        betURL = "https://api.primedice.com/api/bet?access_token=";

        Bundle b = getIntent().getExtras();
        try {
            user = b.getParcelable("userParcelable");
            access_token = b.getString("access_token");

            if (user != null) {
                txtBalance.setText(format.format(((user.balance / 100000000))));
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

    public void claimFaucet(View v) {
        //TODO: Find a way for implementing faucet
    }

    public void deposit(View v) {
        //TODO: Find a way to deposit
    }

    public void withdraw(View v) {
        //TODO: Ask amount and btc adress
        //TODO: Implement withdraw
    }

    // Set the textboxes with bet/win amounts
    public void updateBetAmountWinOnProfit() {
        String betAmountString = format.format((double) betAmount / 100000000);
        betAmountString = betAmountString.replace(",", ".");
        edBetAmount.setText(betAmountString);

        String winOnProfit = format.format(((betAmount * betMultiplier) - betAmount) / 100000000);
        winOnProfit = winOnProfit.replace(",", ".");
        edProfitonWin.setText(winOnProfit);
    }

    // Halve the betAmount
    public void halfBet(View v) {
        betAmount = betAmount / 2;
        updateBetAmountWinOnProfit();
    }

    // Double the betAmount
    public void doubleBet(View v) {
        betAmount = betAmount * 2;
        updateBetAmountWinOnProfit();
    }

    // Set betAmount to balance of user
    public void maxBet(View v) {
        betAmount = (int) user.balance;
        updateBetAmountWinOnProfit();
    }

    // Switch High/Low bet
    public void changeHighLow(View v) {
        DecimalFormat overUnderFormat = new DecimalFormat("0.00");

        if (betHigh) {
            betHigh = false;
            btnHighLow.setText("Under\n" + overUnderFormat.format(betPercentage));
        } else {
            betHigh = true;
            btnHighLow.setText("Over\n" + overUnderFormat.format(99.99 - betPercentage));
        }
    }

    public void changeMultiplier(View v) {
        //TODO: Set multiplier
        //betPercentage = 99 / betMultiplier

        changeHighLow(v);
    }

    public void changePercentage(View v) {
        //TODO: Set percentage
        //betMultiplier == 99 / betPercentage;

        changeHighLow(v);
    }

    public void rollDice(View v) {
        BetTask betTask = new BetTask();

        String condition;
        String amount = String.valueOf(betAmount);
        String target = String.valueOf(betPercentage);

        if (betHigh) {
            condition = ">";
        } else {
            condition = "<";
        }

        String betResult;
        try {
            betResult = betTask.execute((betURL + access_token), amount, target, condition).get();

            //TODO: Fix fast betting problem. It crashes becayse betresult isnt loaded yet.
            JSONObject jsonObject = new JSONObject(betResult);
            JSONObject jsonBet = jsonObject.getJSONObject("bet");
            JSONObject jsonUser = jsonObject.getJSONObject("user");

            user.updateUser(jsonUser);
            myBets.add(0, new Bet(jsonBet));

            if (myBets.size() == 31) {
                myBets.remove(30);
            }

        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }

        txtBalance.setText(format.format(((user.balance / 100000000))));
        showMyBets(v);
    }

    public void showMyBets(View v) {
        //TODO: Save bets
        showBets(myBets);
    }

    public void showAllBets(View v) {
        GetBetsTask getBetsTask = new GetBetsTask();

        String URL = "https://api.primedice.com/api/bets";
        try {
            recentBets = getBetsTask.execute(URL).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        showBets(recentBets);
    }

    public void showHighRollers(View v) {
        GetBetsTask getBetsTask = new GetBetsTask();

        String URL = "https://api.primedice.com/api/highrollers";
        try {
            recentBets = getBetsTask.execute(URL).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        showBets(recentBets);
    }

    public void showBets(ArrayList<Bet> bets) {
        BetAdapter betAdapter = new BetAdapter(this.getApplicationContext(), bets);
        betListView.setAdapter(betAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectItem(position);
    }

    public void selectItem(int position) {
        listView.setItemChecked(position, true);

        if (menuAdapter.getItem(position).equals("Home")) {
            setTitle("Home");
        } else if (menuAdapter.getItem(position).equals("Profile")) {
            setTitle("Profile");
        } else if (menuAdapter.getItem(position).equals("Stats")) {
            setTitle("Stats");
        } else if (menuAdapter.getItem(position).equals("Chat")) {
            setTitle("Chat");
        } else if (menuAdapter.getItem(position).equals("Automated betting")) {
            setTitle("Automated betting");
        } else if (menuAdapter.getItem(position).equals("Provably fair")) {
            setTitle("Provably fair");
        } else if (menuAdapter.getItem(position).equals("Faucet")) {
            setTitle("Faucet");
        }
        menuAdapter.setSelectedMenuItem(getTitle().toString());
        drawerLayout.closeDrawer(listView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (drawerListener.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerListener.syncState();
    }
}
