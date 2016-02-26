package geert.berkers.primedice.Fragment;

import android.app.Fragment;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import geert.berkers.primedice.Activity.MainActivity;
import geert.berkers.primedice.R;
import geert.berkers.primedice.Data.User;

/**
 * Primedice Application Created by Geert on 2-2-2016.
 */
public class StatsFragment extends Fragment {

    private View view;
    private MainActivity activity;

    private String wins, losses, luck, wagered, profit, bets, messages, sessionWagered, sessionProfit, sessionBets;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_stats, container, false);

        initControls();

        return view;
    }

    private void initControls() {
        activity = (MainActivity) getActivity();

        TextView txtWins = (TextView) view.findViewById(R.id.txtWins);
        TextView txtLosses = (TextView) view.findViewById(R.id.txtLosses);
        TextView txtLuck = (TextView) view.findViewById(R.id.txtLuck);

        TextView txtWagered = (TextView) view.findViewById(R.id.txtWagered);
        TextView txtProfit = (TextView) view.findViewById(R.id.txtProfit);
        TextView txtBetsMade = (TextView) view.findViewById(R.id.txtBetsMade);
        TextView txtMessages = (TextView) view.findViewById(R.id.txtMessages);

        TextView txtReset = (TextView) view.findViewById(R.id.resetSession);

        final TextView txtSessionWagered = (TextView) view.findViewById(R.id.txtSessionWagered);
        final TextView txtSessionProfit = (TextView) view.findViewById(R.id.txtSessionProfit);
        final TextView txtSessionBetsMade = (TextView) view.findViewById(R.id.txtSessionBetsMade);

        txtReset.setPaintFlags(txtReset.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txtReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.resetSessionStats();

                txtSessionWagered.setText("0.00000000");
                txtSessionProfit.setText("0.00000000");
                txtSessionBetsMade.setText("0");
            }
        });

        txtWins.setText(wins);
        txtLosses.setText(losses);
        txtLuck.setText(luck);

        txtWagered.setText(wagered);
        txtProfit.setText(profit);
        txtBetsMade.setText(bets);
        txtMessages.setText(messages);

        txtSessionWagered.setText(sessionWagered);
        txtSessionProfit.setText(sessionProfit);
        txtSessionBetsMade.setText(sessionBets);
    }

    public void setInformation(User user, String sessionWagered, String sessionProfit, String sessionBets) {
        this.wins = user.getWins();
        this.losses = user.getLosses();
        this.luck = user.getLuck();
        this.wagered = user.getWageredAsString();
        this.profit = user.getProfitAsString();
        this.bets = user.getBetsAsString();
        this.messages = user.getMessages();
        this.sessionWagered = sessionWagered;
        this.sessionProfit = sessionProfit;
        this.sessionBets = sessionBets;
    }
}