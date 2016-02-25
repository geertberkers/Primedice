package geert.berkers.primedice;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Primedice Application Created by Geert on 20-2-2016.
 */
public class TipActivity extends AppCompatActivity {

    private ListView tipsListView;
    private TipAdapter tipsAdapter;

    private ArrayList<Tip> tipsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tips_layout);

        tipsListView = (ListView) findViewById(R.id.tipsListView);
        getSupportActionBar().setTitle("Tips");

        Bundle b = getIntent().getExtras();

        try {
            tipsList = b.getParcelableArrayList("tips");

            if (tipsList != null) {
                tipsAdapter = new TipAdapter(this, tipsList);
                tipsListView.setAdapter(tipsAdapter);
            } else throw new Exception("Tips are null");

        } catch (Exception ex) {
            Log.e("TipsException", ex.toString());

            this.finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}