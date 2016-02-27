package geert.berkers.primedice.Fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;

import geert.berkers.primedice.Adapter.BetAdapter;
import geert.berkers.primedice.Data.Bet;
import geert.berkers.primedice.DataHandler.MySQLiteHelper;
import geert.berkers.primedice.Activity.MainActivity;
import geert.berkers.primedice.R;

/**
 * Primedice Application Created by Geert on 2-2-2016.
 */
public class AutomatedBetFragment extends Fragment {

    private View view;
    private MainActivity activity;
    private MySQLiteHelper db;

    private boolean betHigh;
    private int numberOfRolls, betAmount;
    private Double betMultiplier, betPercentage, target;

    private ArrayList<Bet> bets;
    private ListView betsListView;
    private BetAdapter betAdapter;
    private String btnRollBtnText;
    private TextView txtBalance, autobethelp;
    private LinearLayout setNumberOfRollsLayout;
    private Button btnHighLow, btnMultiplier, btnPercentage, btnRollDice;
    private EditText edBetAmount, edLimitRolls, edIncreaseLoss, edIncreaseWin;
    private CheckBox cbLimitRolls, cbReturnBaseLoss, cbIncreaseLoss, cbReturnBaseWin, cbIncreaseWin;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            this.view = inflater.inflate(R.layout.fragment_auto_bet, container, false);

            initControls();
            setListeners();
            setInformation();
        } else {
            String btnText;

            if (activity.isBettingAutomatic()) {
                btnText = "STOP AUTOMATED BETTING";
            } else {
                btnText = "ROLL DICE";
            }

            bets = db.getAllBetsFromUser(activity.getUser().getUsername());
            betAdapter.setNewBetsList(bets);
            btnRollDice.setText(btnText);
        }

        txtBalance.setText(activity.getUser().getBalanceAsString());

        return view;
    }

    // init Controls
    private void initControls() {
        activity = (MainActivity) getActivity();

        txtBalance = (TextView) view.findViewById(R.id.txtBalance);
        autobethelp = (TextView) view.findViewById(R.id.txtAutomatedBetHelp);

        btnHighLow = (Button) view.findViewById(R.id.btnHighLow);
        btnMultiplier = (Button) view.findViewById(R.id.btnMultiplier);
        btnPercentage = (Button) view.findViewById(R.id.btnPercentage);
        btnRollDice = (Button) view.findViewById(R.id.btnRollDice);

        edBetAmount = (EditText) view.findViewById(R.id.edBetAmount);
        edLimitRolls = (EditText) view.findViewById(R.id.edNumberOfRolls);
        edIncreaseWin = (EditText) view.findViewById(R.id.edIncreaseBetWin);
        edIncreaseLoss = (EditText) view.findViewById(R.id.edIncreaseBetLoss);

        cbLimitRolls = (CheckBox) view.findViewById(R.id.cbLimitRolls);
        cbIncreaseWin = (CheckBox) view.findViewById(R.id.cbIncreaseWin);
        cbIncreaseLoss = (CheckBox) view.findViewById(R.id.cbIncreaseLoss);
        cbReturnBaseWin = (CheckBox) view.findViewById(R.id.cbReturnBaseWin);
        cbReturnBaseLoss = (CheckBox) view.findViewById(R.id.cbReturnBaseLoss);

        betsListView = (ListView) view.findViewById(R.id.betsListView);

        db = new MySQLiteHelper(activity);
        bets = db.getAllBetsFromUser(activity.getUser().getUsername());

        betAdapter = new BetAdapter(activity.getApplicationContext(), bets);
        betsListView.setAdapter(betAdapter);
        setNumberOfRollsLayout = (LinearLayout) view.findViewById(R.id.numberOfRollsSetLayout);
    }

    // Set onClick Listeners
    private void setListeners() {
        autobethelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
                alertDialog.setTitle("AUTOBET HELP");
                String message = "Automated betting allows you to effortlessly roll and carry out longer term strategies without the tediousness of having to continuously click roll dice.The base bet is initial bet amount which will be influenced by \"Increase bet by_%\". \n\nUsing increase bet by 100% on loss will result in your bet doubling every time you lose and thus performing a simple martingale sequence.\n\nUsers can also choose to limit their risk by making use of \"stop at certain profit/loss\" which will cease betting at a limit set by the user.\n\n\"Limit number of rolls\" is another useful function which will cause the auto-bet sequence to end after a set number of rolls, otherwise it will roll indefinitely or until there are too little funds to continue.";
                alertDialog.setMessage(message);
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertDialog.show();
            }
        });

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

        btnRollDice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rollDice();
            }
        });

        cbLimitRolls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cbLimitRolls.isChecked()) {
                    setNumberOfRollsLayout.setVisibility(View.VISIBLE);
                    edLimitRolls.setText(String.valueOf(numberOfRolls));
                } else {
                    setNumberOfRollsLayout.setVisibility(View.GONE);
                }
            }
        });

        cbReturnBaseWin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cbReturnBaseWin.isChecked()) {
                    edIncreaseWin.setEnabled(false);
                    cbIncreaseWin.setChecked(false);
                } else {
                    edIncreaseWin.setEnabled(true);
                    cbIncreaseWin.setChecked(true);
                }
            }
        });

        cbIncreaseWin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cbIncreaseWin.isChecked()) {
                    edIncreaseWin.setEnabled(true);
                    cbReturnBaseWin.setChecked(false);
                } else {
                    edIncreaseWin.setEnabled(false);
                    cbReturnBaseWin.setChecked(true);
                }

            }
        });

        cbReturnBaseLoss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cbReturnBaseLoss.isChecked()) {
                    edIncreaseLoss.setEnabled(false);
                    cbIncreaseLoss.setChecked(false);
                } else {
                    edIncreaseLoss.setEnabled(true);
                    cbIncreaseLoss.setChecked(true);
                }
            }
        });

        cbIncreaseLoss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cbIncreaseLoss.isChecked()) {
                    edIncreaseLoss.setEnabled(true);
                    cbReturnBaseLoss.setChecked(false);
                } else {
                    edIncreaseLoss.setEnabled(false);
                    cbReturnBaseLoss.setChecked(true);
                }
            }
        });
    }

    // Set information
    private void setInformation() {
        numberOfRolls = 10;
        betHigh = false;
        betAmount = 0;
        betMultiplier = 2.0;
        betPercentage = 49.50;
        target = betPercentage;

        cbLimitRolls.setChecked(false);
        setNumberOfRollsLayout.setVisibility(View.GONE);

        setListViewHeightBasedOnChildren(betsListView);
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
        String condition;

        if (activity.isBettingAutomatic()) {
            activity.stopAutomatedBetThread();
            btnRollBtnText = "ROLL DICE";
            btnRollDice.setText(btnRollBtnText);
        } else {
            btnRollBtnText = "STOP AUTOMATED BETTING";
            btnRollDice.setText(btnRollBtnText);

            if (betHigh) {
                condition = ">";
            } else {
                condition = "<";
            }

            try {
                if (cbLimitRolls.isChecked()) {
                    numberOfRolls = Integer.valueOf(edLimitRolls.getText().toString());
                } else {
                    numberOfRolls = -1;
                }

                boolean increaseOnWin = cbIncreaseWin.isChecked();
                boolean increaseOnLoss = cbIncreaseLoss.isChecked();

                double increaseOnWinValue = Double.valueOf(edIncreaseWin.getText().toString());
                double increaseOnLossValue = Double.valueOf(edIncreaseLoss.getText().toString());

                activity.startAutomatedBetThread(betAmount, String.valueOf(target), condition, numberOfRolls, increaseOnWin, increaseOnLoss, increaseOnWinValue, increaseOnLossValue);

            } catch (Exception e) {
                Toast.makeText(activity.getApplicationContext(), "Bet settings incorrect!", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    // Update balance
    public void updateBalance(final String balance) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtBalance.setText(balance);
            }
        });
    }

    // Set ListView Height
    private static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int totalHeight = 0;
            for (int i = 0; i < listAdapter.getCount(); i++) {
                View listItem = listAdapter.getView(i, null, listView);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }

            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = (totalHeight / 100 * 160) + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
            listView.setLayoutParams(params);
        }
    }

    // Add bet to the list and show if opened
    public void addBet(Bet bet) {

        bets.add(0, bet);
        db.addBet(bet);

        // Remove bet if saved more than 30 bets
        if (bets.size() > 30) {
            for (int i = 30; i < bets.size(); i++) {
                db.deleteBet(bets.get(i));
                bets.remove(i);
            }
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                betAdapter.setNewBetsList(bets);
            }
        });
    }

    // Notify automated betting stopped.
    public void notifyAutomatedBetStopped() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnRollBtnText = "ROLL DICE";
                btnRollDice.setText(btnRollBtnText);
            }
        });
    }
}