package geert.berkers.primedice.DataHandler;

import android.content.Intent;
import android.net.Uri;
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
public class ChatObject extends ClickableSpan {

    public final static int BET = 0;
    public final static int USER = 1;
    public final static int LINK = 2;

    private final String value;
    private final int chatObject;

    public ChatObject(int chatObject, String value) {
        this.chatObject = chatObject;
        this.value = value;
    }

    @Override
    public void onClick(View widget) {
        if (chatObject == BET) {
            try {
                GetFromServerTask getBetsTask = new GetFromServerTask();
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
        } else  if (chatObject == USER){
            Intent playerInfoIntent = new Intent(widget.getContext(), PlayerInformationActivity.class);
            playerInfoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            playerInfoIntent.putExtra("playerName", value);
            widget.getContext().startActivity(playerInfoIntent);
        } else{
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(value));
            widget.getContext().startActivity(browserIntent);
        }
    }
}
