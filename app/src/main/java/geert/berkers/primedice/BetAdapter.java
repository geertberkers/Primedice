package geert.berkers.primedice;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Geert on 26-1-2016.
 */
public class BetAdapter extends BaseAdapter {
    private Context context;

    private ArrayList<Bet> betArrayList = new ArrayList<>();

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
            row = inflater.inflate(R.layout.bet_layout, parent, false);
        } else {
            row = convertView;
        }
        final TextView betID = (TextView) row.findViewById(R.id.txtBetID);
        final TextView player = (TextView) row.findViewById(R.id.txtPlayer);
        TextView profit = (TextView) row.findViewById(R.id.txtProfit);

        betID.setText(String.valueOf(betArrayList.get(position).getIDString()));
        player.setText(String.valueOf(betArrayList.get(position).getPlayer()));
        profit.setText(String.valueOf(betArrayList.get(position).getProfit()));

        betID.setPaintFlags(betID.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        player.setPaintFlags(player.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        boolean win = betArrayList.get(position).getWinOrLose();

        if (!win) {
            profit.setTextColor(Color.RED);
        } else {
            profit.setText("  " + profit.getText().toString());
            profit.setTextColor(Color.argb(255, 0, 100, 0));
        }

        if (position % 2 != 0) {
            row.setBackgroundResource(R.color.background_bet);
        } else {
            row.setBackgroundResource(R.color.white);

        }
        betID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Show bet
                Log.i("Clicked", betID.getText().toString());
            }
        });

        player.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Show Player
                Log.i("Clicked", player.getText().toString());
            }
        });
        return row;
    }
}