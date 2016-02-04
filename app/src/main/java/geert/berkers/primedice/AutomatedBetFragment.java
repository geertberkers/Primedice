package geert.berkers.primedice;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Primedice Application Created by Geert on 2-2-2016.
 */
public class AutomatedBetFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auto_bet, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        initControls();
    }

    private void initControls() {

    }
}
