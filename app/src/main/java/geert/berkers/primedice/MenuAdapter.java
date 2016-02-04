package geert.berkers.primedice;


import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Primedice Application Created by Geert on 24-1-2016.
 */
public class MenuAdapter extends BaseAdapter {
    private Context context;
    private String selectedMenuItem;

    private String[] sort = {"Bet", "Profile", "Stats", "Chat", "Automated betting", "Provably fair", "Faucet", "Log out","Tip Developer"};
    private Integer[] images = {R.drawable.home, R.drawable.profile, R.drawable.stats, R.drawable.chat, R.drawable.automatedbetting, R.drawable.provablyfair, R.drawable.faucet,R.drawable.logout, R.drawable.tipdeveloper};

    public MenuAdapter(Context context) {
        this.context = context;
        this.selectedMenuItem = "Bet";
    }

    @Override
    public int getCount() {
        return sort.length;
    }

    @Override
    public String getItem(int position) {
        return sort[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.menu_item_layout, parent, false);
        } else {
            row = convertView;
        }
        TextView titleMenuItem = (TextView) row.findViewById(R.id.menuItem);
        ImageView titleImageView = (ImageView) row.findViewById(R.id.menuPicture);

        if (selectedMenuItem.equals(sort[position])){
            row.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }
        else{
            row.setBackgroundColor(ContextCompat.getColor(context, R.color.primedicecolor));
        }

        titleMenuItem.setText(sort[position]);
        titleImageView.setImageResource(images[position]);

        return row;
    }

    public void setSelectedMenuItem(String selectedMenuItem) {
        this.selectedMenuItem = selectedMenuItem;
        notifyDataSetChanged();
    }
}