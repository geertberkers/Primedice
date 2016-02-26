package geert.berkers.primedice.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

import geert.berkers.primedice.Activity.BetInformationActivity;
import geert.berkers.primedice.Data.Bet;
import geert.berkers.primedice.Activity.PlayerInformationActivity;
import geert.berkers.primedice.R;

/**
 * Primedice Application Created by Geert on 26-1-2016.
 */
public class BetAdapter extends BaseAdapter {
    private Context context;

    private ArrayList<Bet> betArrayList;

    // Create BetAdapter
    public BetAdapter(Context context, ArrayList<Bet> recentBets) {
        this.context = context;
        this.betArrayList = recentBets;
    }

    @Override
    public int getCount() {
        return betArrayList.size();
    }

    @Override
    public Bet getItem(int position) {
        return betArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.bet_betlist_layout, parent, false);
        } else {
            row = convertView;
        }

        final TextView betID = (TextView) row.findViewById(R.id.txtBetID);
        final TextView player = (TextView) row.findViewById(R.id.txtPlayer);
        final TextView profit = (TextView) row.findViewById(R.id.txtProfit);

        betID.setPaintFlags(betID.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        player.setPaintFlags(player.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        betID.setText(betArrayList.get(position).getIDString());
        player.setText(betArrayList.get(position).getPlayer());
        profit.setText(betArrayList.get(position).getProfit());

        // Change color to red if lost and green if won
        if (!betArrayList.get(position).getWinOrLose()) {
            profit.setTextColor(Color.RED);
        } else {
            profit.setTextColor(Color.argb(255, 0, 100, 0));
        }

        // Even numbers a light background color
        if (position % 2 != 0) {
            row.setBackgroundResource(R.color.background_bet);
        } else {
            row.setBackgroundResource(R.color.white);

        }

        // Add onclick function. This opens the bet information
        betID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent betInfoIntent = new Intent(v.getContext(), BetInformationActivity.class);
                betInfoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                betInfoIntent.putExtra("bet", betArrayList.get(position));
                v.getContext().startActivity(betInfoIntent);
            }
        });

        // Add onclick function. This opens the player information
        player.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent playerInfoIntent = new Intent(v.getContext(), PlayerInformationActivity.class);
                playerInfoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                playerInfoIntent.putExtra("playerName", betArrayList.get(position).getPlayer());
                v.getContext().startActivity(playerInfoIntent);
            }
        });

        return row;
    }
}