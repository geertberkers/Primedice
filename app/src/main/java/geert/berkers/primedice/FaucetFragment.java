package geert.berkers.primedice;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Primedice Application Created by Geert on 2-2-2016.
 */
//TODO: Implement faucet
public class FaucetFragment extends Fragment {

    private View view;
    private MainActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_profile, container, false);

        initControls();

        return view;
    }


    private void initControls() {
        activity = (MainActivity) getActivity();

        //TextView txtWins = (TextView)  view.findViewById(R.id.txtWins);
    }

    public void updateBalance(final String balance) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //txtBalance.setText(balance);
            }
        });
    }

}

