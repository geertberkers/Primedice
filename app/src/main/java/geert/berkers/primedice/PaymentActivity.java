package geert.berkers.primedice;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Primedice Application Created by Geert on 20-2-2016.
 */
public class PaymentActivity extends AppCompatActivity {

    private ListView paymentsListView;
    private PaymentAdapter paymentsAdapter;

    private String title;
    private ArrayList<Payment> paymentsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payments_layout);

        paymentsListView = (ListView) findViewById(R.id.paymentsListView);

        Bundle b = getIntent().getExtras();

        try {
            title = b.getString("title");
            paymentsList = b.getParcelableArrayList("payments");

            if (title != null) {
                getSupportActionBar().setTitle(title);
            } else throw new Exception("Title is null");

            if (paymentsList != null) {
                paymentsAdapter = new PaymentAdapter(this, paymentsList);
                paymentsListView.setAdapter(paymentsAdapter);
            } else throw new Exception("Payments is null");

        } catch (Exception ex) {
            Log.e("PaymentsException", ex.toString());

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