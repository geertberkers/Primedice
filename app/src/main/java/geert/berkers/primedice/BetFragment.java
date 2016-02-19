package geert.berkers.primedice;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Primedice Application Created by Geert on 2-2-2016.
 */
public class BetFragment extends Fragment {

    private View view;
    private MainActivity activity;

    private int openedBet;
    private static int MY_BETS = 1;
    private static int ALL_BETS = 2;
    private static int HR_BETS = 3;

    private int betAmount;
    private MySQLiteHelper db;
    private Bitmap resultImage;
    private View withdrawalView;
    private DecimalFormat format;
    private ListView betListView;
    private boolean maxPressed, betHigh;
    private ArrayList<Bet> myBets, allBets, hrBets;
    private Double betMultiplier, betPercentage, target;
    private String depositURL, withdrawalURL, betURL, userURL;
    private EditText edBetAmount, edProfitonWin, edWithdrawalAdress;
    private TextView txtBalance, txtMyBets, txtAllBets, txtHighRollers;
    private Button btnDeposit, btnWithdraw, btnHalfBet, btnDoubleBet, btnMaxBet, btnHighLow, btnMultiplier, btnPercentage, btnRollDice;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            this.view = inflater.inflate(R.layout.fragment_bet, container, false);

            initControls();
            setListeners();
            setInformation();
        }

        txtBalance.setText(activity.getUser().getBalance());

        return view;
    }

    private void initControls() {
        activity = (MainActivity) getActivity();

        txtBalance = (TextView) view.findViewById(R.id.txtBalance);

        btnDeposit = (Button) view.findViewById(R.id.btnDeposit);
        btnWithdraw = (Button) view.findViewById(R.id.btnWithdraw);

        edBetAmount = (EditText) view.findViewById(R.id.edBetAmount);
        edProfitonWin = (EditText) view.findViewById(R.id.edProfitonWin);

        btnHalfBet = (Button) view.findViewById(R.id.btnHalfBet);
        btnDoubleBet = (Button) view.findViewById(R.id.btnDoubleBet);
        btnMaxBet = (Button) view.findViewById(R.id.btnMaxBet);

        btnHighLow = (Button) view.findViewById(R.id.btnHighLow);
        btnMultiplier = (Button) view.findViewById(R.id.btnMultiplier);
        btnPercentage = (Button) view.findViewById(R.id.btnPercentage);
        btnRollDice = (Button) view.findViewById(R.id.btnRollDice);

        txtMyBets = (TextView) view.findViewById(R.id.txtMyBets);
        txtAllBets = (TextView) view.findViewById(R.id.txtAllBets);
        txtHighRollers = (TextView) view.findViewById(R.id.txtHighRollers);

        betListView = (ListView) view.findViewById(R.id.betsListView);
    }

    private void setInformation() {
        maxPressed = false;
        betHigh = false;
        betAmount = 0;
        betMultiplier = 2.0;
        betPercentage = 49.50;
        target = betPercentage;

        myBets = new ArrayList<>();
        allBets = new ArrayList<>();
        hrBets = new ArrayList<>();

        openedBet = MY_BETS;

        db = new MySQLiteHelper(activity);
        format = new DecimalFormat("0.00000000");

        betURL = "https://api.primedice.com/api/bet?access_token=";
        userURL = "https://api.primedice.com/api/users/1?access_token=";
        depositURL = "https://api.primedice.com/api/deposit?access_token=";
        withdrawalURL = "https://api.primedice.com/api/withdraw?access_token=";

        txtBalance.setText(activity.getUser().getBalance());
        myBets = db.getAllBetsFromUser(activity.getUser().username);
        showBets(myBets);

        allBets = getBets("bets");
        hrBets = getBets("highrollers");

        downloadImage();
    }

    // Download image for deposits
    private void downloadImage() {
        try {
            String address = activity.getUser().address;

            if (address.equals("null") || address == null) {
                String result;
                try {
                    GetJSONResultFromURLTask getAddressTask = new GetJSONResultFromURLTask();
                    result = getAddressTask.execute(depositURL + activity.getAccess_token()).get();

                    JSONObject jsonResult = new JSONObject(result);
                    activity.getUser().address = jsonResult.getString("address");

                } catch (InterruptedException | ExecutionException | JSONException e) {
                    e.printStackTrace();
                }
            }

            DownloadImageTask downloadImageTask = new DownloadImageTask();
            resultImage = downloadImageTask.execute("https://chart.googleapis.com/chart?chs=500x500&cht=qr&chl=" + address).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    // Handle all button clicks
    private void setListeners() {
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
                    Toast.makeText(activity.getApplicationContext(), "Use numbers only!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

        });

        btnHalfBet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maxPressed = false;
                betAmount = betAmount / 2;
                updateBetAmount();
            }
        });

        btnDoubleBet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maxPressed = false;
                betAmount = betAmount * 2;
                updateBetAmount();
            }
        });

        btnMaxBet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btcBalance = activity.getUser().getBalance();
                betAmount = (int) (Double.valueOf(btcBalance) * 100000000);
                updateBetAmount();
                maxPressed = true;
            }
        });

        btnHighLow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeHighLow();
            }
        });

        btnMultiplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMultiplier();
            }
        });

        btnPercentage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePercentage();
            }
        });

        btnDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deposit();
            }
        });

        btnWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                withdraw();
            }
        });

        btnRollDice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rollDice();
            }
        });

        txtMyBets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBets(myBets);
                openedBet = MY_BETS;
            }
        });

        txtAllBets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBets(allBets);
                openedBet = ALL_BETS;
            }
        });

        txtHighRollers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBets(hrBets);
                openedBet = HR_BETS;
            }
        });
        openedBet = MY_BETS;
    }

    // Deposit some BTC!
    private void deposit() {
        ImageView depositAdressImage = new ImageView(activity);
        depositAdressImage.setImageBitmap(resultImage);

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setTitle("DEPOSIT");
        alertDialog.setView(depositAdressImage);
        alertDialog.setMessage("Your deposit adress is: " + activity.getUser().address);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.setNeutralButton("Copy", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("BTC adress", activity.getUser().address);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(activity.getApplicationContext(), "Copied BTC address!", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.setNegativeButton("Open", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("bitcoin:" + activity.getUser().address));
                    startActivity(browserIntent);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(activity.getApplicationContext(), "No apps to open this!", Toast.LENGTH_LONG).show();
                }
            }
        });
        alertDialog.show();
    }

    // Withdraw some btc!
    private void withdraw() {
        LayoutInflater factory = LayoutInflater.from(activity);

        if (withdrawalView != null) {
            ViewGroup parent = (ViewGroup) withdrawalView.getParent();
            if (parent != null) {
                parent.removeView(withdrawalView);
            }
        }
        try {
            withdrawalView = factory.inflate(R.layout.withdraw_layout, null);
        } catch (InflateException e) {
            e.printStackTrace();
        }

        edWithdrawalAdress = (EditText) withdrawalView.findViewById(R.id.edWithdrawalAdress);
        final EditText edWithdrawalAmount = (EditText) withdrawalView.findViewById(R.id.edWithdrawalAmount);

        final Button btnScan = (Button) withdrawalView.findViewById(R.id.btnScan);
        final Button btnMaxWithdawAmount = (Button) withdrawalView.findViewById(R.id.btnMaxWithdawAmount);

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.initiateScan();
            }
        });

        btnMaxWithdawAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edWithdrawalAmount.setText(activity.getUser().getBalance());
            }
        });

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setTitle("PLACE A WITHDRAWAL");
        alertDialog.setMessage("Withdrawal fee: 0.0001 BTC.");
        alertDialog.setView(withdrawalView);
        alertDialog.setPositiveButton("WITHDRAW", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String withdrawalAdress = edWithdrawalAdress.getText().toString();
                String withdrawalAmount = edWithdrawalAmount.getText().toString();

                if (withdrawalAdress.length() == 0) {
                    Toast.makeText(activity.getApplicationContext(), "Fill in a BTC Address!", Toast.LENGTH_SHORT).show();
                } else if (withdrawalAmount.length() == 0) {
                    Toast.makeText(activity.getApplicationContext(), "Fill in an amount!", Toast.LENGTH_SHORT).show();
                } else {

                    try {
                        withdrawalAmount = withdrawalAmount.replace(",", ".");
                        Double amountToWithdraw = Double.valueOf(withdrawalAmount);

                        double toSatoshiMultiplier = 100000000;
                        double satoshiDouble = amountToWithdraw * toSatoshiMultiplier;

                        // Dirty fix for rounding math problems
                        if (satoshiDouble - Double.valueOf(satoshiDouble).intValue() >= 0.999) {
                            satoshiDouble += 0.1;
                        }

                        int satoshiWithdrawAmount = (int) satoshiDouble;

                        if (activity.getUser().balance < satoshiDouble) {
                            Toast.makeText(activity.getApplicationContext(), "Not enough balance to withdraw!", Toast.LENGTH_LONG).show();
                        } else if (satoshiWithdrawAmount < 100000) {
                            Toast.makeText(activity.getApplicationContext(), "Withdrawal must be at least 0.0010000 BTC!", Toast.LENGTH_LONG).show();
                        } else {
                            try {

                                PlaceWithdrawalTask placeWithdrawalTask = new PlaceWithdrawalTask();
                                String result = placeWithdrawalTask.execute((withdrawalURL + activity.getAccess_token()), String.valueOf(satoshiWithdrawAmount), withdrawalAdress).get();
                                //String resultExample = "{\"amount\":100000,\"sent\":100000,\"txid\":\"8f0159c9af5ba2b325bf93085632e91a65595f0fb8e4bca2f9507bd1be619ddf\",\"address\":\"19ZgWmESFWmhcQKDP9ZxhUeLvTACXNyUjS\",\"confirmed\":true,\"timestamp\":\"2016-01-30T01:20:33.580Z\"}";

                                try {
                                    JSONObject jsonResult = new JSONObject(result);
                                    String txid = jsonResult.getString("txid");
                                    Toast.makeText(activity.getApplicationContext(), "Withdrawed " + withdrawalAmount + " BTC to " + withdrawalAdress + ".\nTXID: " + txid, Toast.LENGTH_LONG).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                String userResult = "NoResult";
                                try {
                                    GetJSONResultFromURLTask userTask = new GetJSONResultFromURLTask();
                                    userResult = userTask.execute("https://api.primedice.com/api/users/1?access_token=" + activity.getAccess_token()).get();
                                } catch (InterruptedException | ExecutionException e) {
                                    e.printStackTrace();
                                }

                                if (userResult != null || !userResult.equals("NoResult")) {
                                    activity.updateUser(new User(userResult));
                                    txtBalance.setText(activity.getUser().getBalance());
                                }
                            } catch (InterruptedException | ExecutionException e) {

                                Toast.makeText(activity.getApplicationContext(), "Withdrawal failed!", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }

    // Update bet amount
    private void updateBetAmount() {
        String rollDice = "ROLL DICE";
        btnRollDice.setText(rollDice);
        String betAmountString = format.format((double) betAmount / 100000000);
        betAmountString = betAmountString.replace(",", ".");
        edBetAmount.setText(betAmountString);
    }

    // Switch High/Low bet
    private void changeHighLow() {
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
    private void changeMultiplier() {
        final boolean[] firstEdit = {true};
        final EditText inputText = new EditText(activity);
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

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setTitle("Change multiplier");
        alertDialog.setMessage("Between 1.01202 and 9900.\nFor example: 2.0");
        alertDialog.setView(inputText);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                double newBetMultiplier = betMultiplier;
                try {
                    newBetMultiplier = Double.valueOf(inputText.getText().toString());
                } catch (Exception ex) {
                    Toast.makeText(activity.getApplicationContext(), "Insert numbers only!", Toast.LENGTH_LONG).show();
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
                    changeHighLow();
                } else {
                    Toast.makeText(activity.getApplicationContext(), "Multiplier not between 1.01202 and 9900!", Toast.LENGTH_LONG).show();
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
    private void changePercentage() {
        final boolean[] firstEdit = {true};
        final EditText inputText = new EditText(activity);
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

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setTitle("Change win percentage");
        alertDialog.setMessage("Between 0.01 and 98.\nFor example: 49.50");
        alertDialog.setView(inputText);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                double newBetPercentage = betPercentage;
                try {
                    newBetPercentage = Double.valueOf(inputText.getText().toString());
                } catch (Exception ex) {
                    Toast.makeText(activity.getApplicationContext(), "Insert numbers only!", Toast.LENGTH_LONG).show();
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
                    changeHighLow();
                } else {
                    Toast.makeText(activity.getApplicationContext(), "Bet percentage not between 0.01 and 98!", Toast.LENGTH_LONG).show();
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
    private void rollDice() {
        String rollDice;

        if (maxPressed) {
            maxPressed = false;
            rollDice = "Confirm MAX bet";
            btnRollDice.setText(rollDice);
        } else if (betAmount > (int) activity.getUser().balance) {
            Toast.makeText(activity.getApplicationContext(), "Insufficient balance", Toast.LENGTH_LONG).show();
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

            // Place bet
            try {
                String result = placeBetTask.execute((betURL + activity.getAccess_token()), amount, target, condition).get();

                // Check result
                if (result != null) {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject jsonBet = jsonObject.getJSONObject("bet");
                    JSONObject jsonUser = jsonObject.getJSONObject("user");

                    // Create bet and put in betlist/database
                    Bet bet = new Bet(jsonBet);
                    activity.getUser().updateUser(jsonUser);
                    myBets.add(0, bet);
                    db.addBet(bet);

                    // Remove bet if saved more than 30 bets
                    if (myBets.size() > 30) {
                        for (int i = 30; i < myBets.size(); i++) {
                            db.deleteBet(myBets.get(i));
                            myBets.remove(i);
                        }
                    }

                    openedBet = MY_BETS;
                } else {
                    GetJSONResultFromURLTask userTask = new GetJSONResultFromURLTask();
                    String userResult = userTask.execute(userURL + activity.getAccess_token()).get();

                    if (userResult != null || !userResult.equals("NoResult")) {
                        activity.updateUser(new User(userResult));

                        String error;
                        if (betAmount > (int) activity.getUser().balance) {
                            error = "Insufficient funds!";
                        } else {
                            error = "Betting to fast!";
                        }

                        Toast.makeText(activity.getApplicationContext(), error, Toast.LENGTH_LONG).show();
                    }
                }
            } catch (InterruptedException | ExecutionException | JSONException e) {
                e.printStackTrace();
            }

            txtBalance.setText(activity.getUser().getBalance());
            showBets(myBets);
        }
    }

    // Show bets
    private void showBets(ArrayList<Bet> bets) {
        BetAdapter betAdapter = new BetAdapter(activity.getApplicationContext(), bets);
        betListView.setAdapter(betAdapter);
    }

    // Get and show bets
    private ArrayList<Bet> getBets(String getThese) {
        String URL = "https://api.primedice.com/api/" + getThese;

        try {
            GetJSONResultFromURLTask getBetsTask = new GetJSONResultFromURLTask();
            String result = getBetsTask.execute(URL).get();
            if (getThese.equals("bets")) {
                allBets = getBetsListFromJSON(result, getThese);
                return allBets;
            } else {
                hrBets = getBetsListFromJSON(result, getThese);
                return hrBets;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Get bets from json
    private ArrayList<Bet> getBetsListFromJSON(String betsJSON, String getThese) {
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

    public void setWithdrawalAdress(String withdrawalAdress) {
        edWithdrawalAdress.setText(withdrawalAdress);
    }

    // Add bet to the list and show if opened
    public void addBet(Bet b, boolean highRoller) {
        if (highRoller) {
            this.hrBets.remove(hrBets.size() - 1);
            this.hrBets.add(0, b);

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (openedBet == HR_BETS) {
                        showBets(hrBets);
                    }
                }
            });
        } else {
            this.allBets.remove(allBets.size() - 1);
            this.allBets.add(0, b);

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (openedBet == ALL_BETS) {
                        showBets(allBets);
                    }
                }
            });
        }
    }

    public void updateBalance(final String balance) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtBalance.setText(balance);
            }
        });
    }
}
