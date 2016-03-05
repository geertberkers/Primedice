package geert.berkers.primedice.DataHandler;

import android.content.Intent;
import android.text.style.ClickableSpan;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import geert.berkers.primedice.Activity.BetInformationActivity;
import geert.berkers.primedice.Activity.PlayerInformationActivity;
import geert.berkers.primedice.Data.Bet;
import geert.berkers.primedice.Data.URL;

/**
 * Pimedice Application Created by Geert on 4-3-2016.
 */
public class MyClickableSpan extends ClickableSpan {

    public final static int BET = 0;
    public final static int USER = 1;

    private String value;
    private int clickableObject;

    public MyClickableSpan(int clickableObject, String value) {
        this.clickableObject = clickableObject;
        this.value = value.toLowerCase();
    }


    @Override
    public void onClick(View widget) {
        if (clickableObject == BET) {
            try {
                GetJSONResultFromURLTask getBetsTask = new GetJSONResultFromURLTask();

                String result = getBetsTask.execute((URL.BET_LOOKUP + value.replace(",", ""))).get();

                if (result != null) {
                    JSONObject jsonBet = new JSONObject(result);
                    Bet bet = new Bet(jsonBet.getJSONObject("bet"));

                    Intent betInfoIntent = new Intent(widget.getContext(), BetInformationActivity.class);
                    betInfoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    betInfoIntent.putExtra("bet", bet);
                    widget.getContext().startActivity(betInfoIntent);
                }
            } catch (InterruptedException | ExecutionException | JSONException e) {
                e.printStackTrace();
            }

        } else {
            Intent playerInfoIntent = new Intent(widget.getContext(), PlayerInformationActivity.class);
            playerInfoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            playerInfoIntent.putExtra("playerName", value);
            widget.getContext().startActivity(playerInfoIntent);
        }
    }
}
