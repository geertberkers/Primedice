package geert.berkers.primedice.Activity;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;

import geert.berkers.primedice.Adapter.PaymentAdapter;
import geert.berkers.primedice.Data.Payment;
import geert.berkers.primedice.R;

/**
 * Primedice Application Created by Geert on 20-2-2016.
 */
public class PaymentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payments_layout);

        ListView paymentsListView = (ListView) findViewById(R.id.paymentsListView);

        Bundle b = getIntent().getExtras();

        try {
            String title = b.getString("title");
            ArrayList<Payment> paymentsList = b.getParcelableArrayList("payments");

            if (title != null) {
                getSupportActionBar().setTitle(title);
            } else throw new Exception("Title is null");

            if (paymentsList != null) {
                PaymentAdapter paymentsAdapter = new PaymentAdapter(this, paymentsList);
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