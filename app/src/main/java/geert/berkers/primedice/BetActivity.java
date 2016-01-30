package geert.berkers.primedice;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Primedice Application Created by Geert on 23-1-2016.
 */
public class BetActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private User user;
    private boolean betHigh;
    private DecimalFormat format;
    private ArrayList<Bet> recentBets, myBets;
    private String tipURL, betURL, withdrawalURL, access_token;

    Bitmap resultImage;
    private int betAmount;
    private boolean maxPressed;
    private double betMultiplier, betPercentage, target;

    private ListView listView;
    private MenuAdapter menuAdapter;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerListener;

    private MySQLiteHelper db;

    private ListView betListView;
    private EditText edBetAmount, edProfitonWin;
    private TextView txtBalance;
    private Button btnHighLow, btnMultiplier, btnPercentage, btnRollDice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bet);

        initControls();

        setInformation();
    }

    private void initControls() {
        listView = (ListView) findViewById(R.id.drawerList);
        betListView = (ListView) findViewById(R.id.betsListView);

        txtBalance = (TextView) findViewById(R.id.txtBalance);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        edBetAmount = (EditText) findViewById(R.id.edBetAmount);
        edProfitonWin = (EditText) findViewById(R.id.edProfitonWin);
        edBetAmount.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String betAmountString = edBetAmount.getText().toString();
                    betAmountString = betAmountString.replace(",", ".");
                    double betAmountDouble = Double.valueOf(betAmountString);

                    double toSatoshiMultiplier = 100000000;
                    double satoshi = betAmountDouble * toSatoshiMultiplier;

                    // Dirty fix for rounding math problems
                    if (satoshi - Double.valueOf(satoshi).intValue() >= 0.999) {
                        satoshi += 0.1;
                    }
                    betAmount = (int) satoshi;

                    String winOnProfit = format.format(((betAmount * betMultiplier) - betAmount) / 100000000);
                    winOnProfit = winOnProfit.replace(",", ".");
                    edProfitonWin.setText(winOnProfit);
                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), "Use numbers only!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

        });

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

        db = new MySQLiteHelper(this);
        format = new DecimalFormat("0.00000000");

        setTitle("Home");

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.primedice);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setInformation() {
        maxPressed = false;
        betHigh = false;
        betAmount = 0;
        betMultiplier = 2.0;
        betPercentage = 49.50;
        target = betPercentage;

        tipURL = "https://api.primedice.com/api/tip?access_token=";
        betURL = "https://api.primedice.com/api/bet?access_token=";
        withdrawalURL = "https://api.primedice.com/api/withdraw?access_token=";

        recentBets = new ArrayList<>();

        Bundle b = getIntent().getExtras();

        try {
            user = b.getParcelable("userParcelable");
            access_token = b.getString("access_token");

            if (user != null) {
                txtBalance.setText(user.getBalance());
                myBets = db.getAllBetsFromUser(user.username);
                showBets(myBets);

                try {
                    DownloadImageTask downloadImageTask = new DownloadImageTask();
                    resultImage = downloadImageTask.execute("https://chart.googleapis.com/chart?chs=500x500&cht=qr&chl=" + user.address).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
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

    // Deposit some BTC!
    public void deposit(View v) {
        ImageView depositAdressImage = new ImageView(this);
        depositAdressImage.setImageBitmap(resultImage);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("DEPOSIT");
        builder.setMessage("Your deposit adress is: " + user.address);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNeutralButton("Copy", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("BTC adress", user.address);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(getApplicationContext(), "Copied BTC address!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Open", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("bitcoin:" + user.address));
                    startActivity(browserIntent);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(getApplicationContext(), "No apps to open this!", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setView(depositAdressImage);
        builder.show();
    }

    // Withdraw some btc!
    //TODO SCAN QR CODE FOR ADRESS
    public void withdraw(View v) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText edWithdrawalAdress = new EditText(this);
        edWithdrawalAdress.setHint("BTC Address");
        layout.addView(edWithdrawalAdress);

        final EditText edWithdrawalAmount = new EditText(this);
        edWithdrawalAmount.setHint("Amount");
        layout.addView(edWithdrawalAmount);

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("PLACE A WITHDRAWAL");
        alertDialog.setMessage("Withdrawal fee: 0.0001 BTC.");
        alertDialog.setView(layout);

        alertDialog.setPositiveButton("WITHDRAW", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String withdrawalAdress = edWithdrawalAdress.getText().toString();
                String withdrawalAmount = edWithdrawalAmount.getText().toString();

                withdrawalAmount = withdrawalAmount.replace(",", ".");
                Double amountToWithdraw = Double.valueOf(withdrawalAmount);

                double toSatoshiMultiplier = 100000000;
                double satoshiDouble = amountToWithdraw * toSatoshiMultiplier;
                // Dirty fix for rounding math problems
                if (satoshiDouble - Double.valueOf(satoshiDouble).intValue() >= 0.999) {
                    satoshiDouble += 0.1;
                }
                int satoshiWithdrawAmount = (int) satoshiDouble;

                if(user.balance < satoshiDouble){
                    Toast.makeText(getApplicationContext(), "Not enough balance to withdraw!", Toast.LENGTH_LONG).show();
                }
                else {
                    try {
                        PlaceWithdrawalTask placeWithdrawalTask = new PlaceWithdrawalTask();

                        String result = placeWithdrawalTask.execute((withdrawalURL + access_token), String.valueOf(satoshiWithdrawAmount), withdrawalAdress).get();
                        //{"amount":100000,"sent":100000,"txid":"8f0159c9af5ba2b325bf93085632e91a65595f0fb8e4bca2f9507bd1be619ddf","address":"19ZgWmESFWmhcQKDP9ZxhUeLvTACXNyUjS","confirmed":true,"timestamp":"2016-01-30T01:20:33.580Z"}

                        //TODO: Check what to change after withdrawal. For now only balance
                        // Give more information as feedback (toast?)
                        user.balance = user.balance - satoshiDouble;
                        txtBalance.setText(user.getBalance());
                    } catch (InterruptedException | ExecutionException e) {

                        Toast.makeText(getApplicationContext(), "Withdrawal failed!", Toast.LENGTH_LONG).show();

                        e.printStackTrace();
                    }
                }
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Do nothing
            }
        });
        alertDialog.show();
    }

    // Update bet amount
    public void updateBetAmount() {
        String rollDice = "ROLL DICE";
        btnRollDice.setText(rollDice);
        String betAmountString = format.format((double) betAmount / 100000000);
        betAmountString = betAmountString.replace(",", ".");
        edBetAmount.setText(betAmountString);
    }

    // Halve the betAmount
    public void halfBet(View v) {
        maxPressed = false;
        betAmount = betAmount / 2;
        updateBetAmount();
    }

    // Double the betAmount
    public void doubleBet(View v) {
        maxPressed = false;
        betAmount = betAmount * 2;
        updateBetAmount();
    }

    // Set betAmount to balance of user
    public void maxBet(View v) {
        String btcBalance = user.getBalance();
        betAmount = (int) (Double.valueOf(btcBalance) * 100000000);
        updateBetAmount();
        maxPressed = true;
    }

    // Switch High/Low bet
    public void changeHighLow(View v) {
        DecimalFormat overUnderFormat = new DecimalFormat("0.00");

        String betOverUnder;
        if (betHigh) {
            betHigh = false;
            target = betPercentage;
            betOverUnder = "Under\n" + overUnderFormat.format(target).replace(",", ".");
        } else {
            betHigh = true;
            target = 99.99 - betPercentage;
            betOverUnder = "Over\n" + overUnderFormat.format(target).replace(",", ".");
        }
        btnHighLow.setText(betOverUnder);
    }

    // Change multiplier
    public void changeMultiplier(View v) {
        final boolean[] firstEdit = {true};
        final EditText inputText = new EditText(this);
        inputText.setText(String.valueOf(betMultiplier));
        inputText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        inputText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firstEdit[0]) {
                    inputText.setText(null);
                    firstEdit[0] = false;
                }
            }
        });

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Change multiplier");
        alertDialog.setMessage("Between 1.01202 and 9900.\nFor example: 2.0");
        alertDialog.setView(inputText);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                double newBetMultiplier = betMultiplier;
                try {
                    newBetMultiplier = Double.valueOf(inputText.getText().toString());
                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), "Insert numbers only!", Toast.LENGTH_LONG).show();
                }

                if (newBetMultiplier >= 1.01202 && newBetMultiplier <= 9900) {
                    betMultiplier = newBetMultiplier;
                    betPercentage = 99 / betMultiplier;

                    DecimalFormat df = new DecimalFormat("0.00");
                    String percentage = df.format(betPercentage).replace(",", ".") + "%";

                    df = new DecimalFormat("0.000");
                    double percentageDouble = Double.valueOf(percentage.replace("%", ""));
                    newBetMultiplier = 99 / percentageDouble;
                    String multiplier = df.format(newBetMultiplier).replace(",", ".");
                    if (multiplier.indexOf(".") == 4) {
                        multiplier = multiplier.substring(0, 4);
                    } else {
                        multiplier = multiplier.substring(0, 5);
                    }
                    multiplier = multiplier + "x";

                    btnMultiplier.setText(multiplier);
                    btnPercentage.setText(percentage);
                    changeHighLow(null);
                } else {
                    Toast.makeText(getApplicationContext(), "Multiplier not between 1.01202 and 9900!", Toast.LENGTH_LONG).show();
                }
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.setIcon(R.drawable.change);
        alertDialog.show();
    }

    // Change percentage
    public void changePercentage(View v) {
        final boolean[] firstEdit = {true};
        final EditText inputText = new EditText(this);
        inputText.setText(String.valueOf(betPercentage));
        inputText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        inputText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firstEdit[0]) {
                    inputText.setText(null);
                    firstEdit[0] = false;
                }
            }
        });

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Change win percentage");
        alertDialog.setMessage("Between 0.01 and 98.\nFor example: 49.50");
        alertDialog.setView(inputText);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                double newBetPercentage = betPercentage;
                try {
                    newBetPercentage = Double.valueOf(inputText.getText().toString());
                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), "Insert numbers only!", Toast.LENGTH_LONG).show();
                }

                if (newBetPercentage >= 0.01 && newBetPercentage <= 98) {
                    betPercentage = newBetPercentage;
                    betMultiplier = 99 / betPercentage;

                    DecimalFormat df = new DecimalFormat("0.000");
                    String multiplier = df.format(betMultiplier).replace(",", ".");
                    if (multiplier.indexOf(".") == 4) {
                        multiplier = multiplier.substring(0, 4);
                    } else {
                        multiplier = multiplier.substring(0, 5);
                    }
                    multiplier = multiplier + "x";

                    df = new DecimalFormat("0.00");
                    String percentage = df.format(betPercentage).replace(",", ".") + "%";

                    btnMultiplier.setText(multiplier);
                    btnPercentage.setText(percentage);
                    changeHighLow(null);
                } else {
                    Toast.makeText(getApplicationContext(), "Bet percentage not between 0.01 and 98!", Toast.LENGTH_LONG).show();
                }
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.setIcon(R.drawable.change);
        alertDialog.show();
    }

    // Roll dice
    public void rollDice(View v) {

        String rollDice;
        if (maxPressed) {
            maxPressed = false;
            rollDice = "Confirm MAX bet";
            btnRollDice.setText(rollDice);
        } else if (betAmount > (int) user.balance) {
            Toast.makeText(this.getApplicationContext(), "Insufficient balance", Toast.LENGTH_LONG).show();
        } else {
            rollDice = "ROLL DICE";
            btnRollDice.setText(rollDice);
            PlaceBetTask placeBetTask = new PlaceBetTask();

            String condition;
            String amount = String.valueOf(betAmount);
            String target = String.valueOf(this.target);

            if (betHigh) {
                condition = ">";
            } else {
                condition = "<";
            }

            try {
                String result = placeBetTask.execute((betURL + access_token), amount, target, condition).get();

                if (result != null) {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject jsonBet = jsonObject.getJSONObject("bet");
                    JSONObject jsonUser = jsonObject.getJSONObject("user");

                    // Create bet and put in betslist/database
                    Bet bet = new Bet(jsonBet);
                    user.updateUser(jsonUser);
                    myBets.add(0, bet);
                    db.addBet(bet);

                    // Remove bet if saved more than 30 bets
                    if (myBets.size() > 30) {
                        for (int i = 30; i < myBets.size(); i++) {
                            db.deleteBet(myBets.get(i));
                            myBets.remove(i);
                        }
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Betting to fast!",Toast.LENGTH_LONG).show();
                }
                }catch(InterruptedException | ExecutionException | JSONException e){
                    e.printStackTrace();
                }

                txtBalance.setText(user.getBalance());
                showBets(myBets);
            }

    }

    // Show my bets
    public void showMyBets(View v) {
        showBets(myBets);
    }

    // Get and show bets
    public void getAndShowBets(View v) {
        String URL;
        String getThese;
        if (v.getId() == R.id.txtHighRollers) {
            getThese = "highrollers";
            URL = "https://api.primedice.com/api/highrollers";
        } else {
            getThese = "bets";
            URL = "https://api.primedice.com/api/bets";
        }

        try {
            GetJSONResultFromURLTask getBetsTask = new GetJSONResultFromURLTask();

            String result = getBetsTask.execute(URL).get();

            recentBets = getBetsListFromJSON(result, getThese);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        showBets(recentBets);
    }

    // Get bets from json
    public ArrayList<Bet> getBetsListFromJSON(String betsJSON, String getThese) {
        ArrayList<Bet> betArrayList = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(betsJSON);
            JSONArray jsonArray = jsonObject.getJSONArray(getThese);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonBet = jsonArray.getJSONObject(i);
                betArrayList.add(new Bet(jsonBet));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return betArrayList;
    }

    // Show bets
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
        } else if (menuAdapter.getItem(position).equals("Tip Developer")) {
            TipDeveloperTask tipDeveloperTask = new TipDeveloperTask();

            try {
                String result = tipDeveloperTask.execute((tipURL + access_token), "GeertBank", "50001").get();
                Log.i("Result", result);

                JSONObject jsonObject = new JSONObject(result);
                JSONObject jsonUser = jsonObject.getJSONObject("user");
                user.updateUserBalance(jsonUser.getString("balance"));
                txtBalance.setText(user.getBalance());
            } catch (InterruptedException | ExecutionException | JSONException e) {
                e.printStackTrace();
            }

        }
        menuAdapter.setSelectedMenuItem(getTitle().toString());
        drawerLayout.closeDrawer(listView);
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
}
