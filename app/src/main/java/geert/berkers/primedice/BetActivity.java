package geert.berkers.primedice;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DecimalFormat;

/**
 * Created by Geert on 23-1-2016.
 */
public class BetActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private User user;
    private boolean betHigh;
    private double betAmount, betMultiplier, betPercentage;
    private DecimalFormat format;

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
        betAmount = 0.0;
        betMultiplier = 2.0;
        betPercentage = 49.50;
        format = new DecimalFormat("#.########");

        Bundle b = getIntent().getExtras();
        try {
            user = b.getParcelable("userParcelable");
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

    // Halve the betAmount
    public void halfBet(View v) {
        betAmount = betAmount / 2;
        edBetAmount.setText(format.format(betAmount));
        edProfitonWin.setText(format.format(betAmount * betMultiplier));
    }

    // Double the betAmount
    public void doubleBet(View v) {
        betAmount = betAmount * 2;
        edBetAmount.setText(format.format(betAmount));
        edProfitonWin.setText(format.format(betAmount * betMultiplier));
    }

    // Set betAmount to balance of user
    public void maxBet(View v) {
        betAmount = user.balance / 100000000;
        edBetAmount.setText(format.format(betAmount));
        edProfitonWin.setText(format.format(betAmount * betMultiplier));
    }

    public void changeHighLow(View v) {
        if (betHigh) {
            betHigh = false;
            btnHighLow.setText("Under\n" + betPercentage);
        } else {
            betHigh = true;
            btnHighLow.setText("Over\n" + betPercentage);
        }
    }

    public void changeMultiplier(View v) {
        //TODO: Set bet info
    }

    public void changePercentage(View v) {
        //TODO: Set bet info
    }

    public void rollDice(View v) {
        //TODO: Place bet!
    }

    public void showMyBets(View v) {
        //TODO: Get and show my bets
    }

    public void showAllBets(View v) {
        //TODO: Get and show all bets
    }

    public void showHighRollers(View v) {
        //TODO: Get and show HR
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
