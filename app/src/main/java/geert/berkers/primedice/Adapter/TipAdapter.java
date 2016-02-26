package geert.berkers.primedice.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import geert.berkers.primedice.Data.Tip;
import geert.berkers.primedice.Activity.PlayerInformationActivity;
import geert.berkers.primedice.R;

/**
 * Primedice Application Created by Geert on 20-2-2016.
 */
public class TipAdapter extends BaseAdapter {
    private Context context;

    private ArrayList<Tip> tipsList;

    // Create PaymentAdapter
    public TipAdapter(Context context, ArrayList<Tip> tipsList) {
        this.context = context;
        this.tipsList = tipsList;
    }

    @Override
    public int getCount() {
        return tipsList.size();
    }

    @Override
    public Tip getItem(int position) {
        return tipsList.get(position);
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
            row = inflater.inflate(R.layout.tip_tipslist_layout, parent, false);
        } else {
            row = convertView;
        }

        final TextView amount = (TextView) row.findViewById(R.id.txtAmount);
        final TextView username = (TextView) row.findViewById(R.id.txtUsername);
        final TextView receiver = (TextView) row.findViewById(R.id.txtReceiver);
        final TextView date = (TextView) row.findViewById(R.id.txtDate);

        amount.setText(tipsList.get(position).getAmount());
        username.setText(tipsList.get(position).getUsername());
        receiver.setText(tipsList.get(position).getReceiver());
        date.setText(tipsList.get(position).getTimestamp());

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent playerInfoIntent = new Intent(v.getContext(), PlayerInformationActivity.class);
                playerInfoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                playerInfoIntent.putExtra("playerName", tipsList.get(position).getUsername());
                v.getContext().startActivity(playerInfoIntent);
            }
        });

        receiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent playerInfoIntent = new Intent(v.getContext(), PlayerInformationActivity.class);
                playerInfoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                playerInfoIntent.putExtra("playerName", tipsList.get(position).getReceiver());
                v.getContext().startActivity(playerInfoIntent);
            }
        });

        return row;
    }

}