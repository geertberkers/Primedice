package geert.berkers.primedice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin;
    private TextView txtResult;
    private EditText txtUsername, txtPassword;

    private String access_token = null;

    private String loginUrl = "https://api.primedice.com/api/login";
    private String userURL = "https://api.primedice.com/api/users/1?access_token=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        txtResult = (TextView) findViewById(R.id.txtResult);
        txtUsername = (EditText) findViewById(R.id.etUsername);
        txtPassword = (EditText) findViewById(R.id.etPassword);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.primedice);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.primedicecolor)));

        //TODO FIX THIS. NEED TO LOGIN AFTER CRASH!
        try {
            Bundle b = getIntent().getExtras();
            txtResult.setText(b.getString("info"));
        } catch (Exception ex) {
            // Check if access_token is saved in SharedPreferences from your mobile
            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            access_token = sharedPref.getString("access_token", null);
            loginFromAccestoken(access_token);
        }
    }

    //TODO: Make something for registering a user!

    public void login(View v) {

        if (v.getId() == btnLogin.getId()) {
            String username = txtUsername.getText().toString();
            String password = txtPassword.getText().toString();

            String loginResult = "NoResult";
            LoginTask login = new LoginTask();

            try {
                loginResult = login.execute(loginUrl, username, password).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            if (loginResult == null) {
                loginResult = "null";
                txtResult.setText("Couldn't log in! Try again.");
            } else if (loginResult == "NoResult") {
                txtResult.setText("Couldn't log in! Try again.");
            } else {
                getAccestokenFromLoginResult(loginResult);

                loginFromAccestoken(access_token);
            }
            Log.i("result", loginResult);
        }
    }

    // Login with your accestoken
    private void loginFromAccestoken(String access_token) {
        if (access_token != null) {
            User user = getUser();

            if (user != null) {
                Intent betActivityIntent = new Intent(this, BetActivity.class);
                betActivityIntent.putExtra("userParcelable", user);
                startActivity(betActivityIntent);
                this.finish();
            } else {
                txtResult.setText("Log in or register!");
            }
        } else{
            txtResult.setText("Log in or register!");
        }
    }

    // Get the acces_token from the server response
    private String getAccestokenFromLoginResult(String loginResult) {
        try {
            JSONObject oneObject = new JSONObject(loginResult);

            access_token = oneObject.optString("access_token");

            if (access_token != null) {
                // Save accces_token in shared preferences for automatic login next time
                SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.clear();
                editor.putString("access_token", access_token);
                editor.apply();
            }
        } catch (JSONException e) {
            // Oops
            Log.e("error", e.toString());
        }

        return access_token;
    }

    // Create user object from server response
    public User getUser() {

        User user;
        String userResult = "NoUser";

        GetUserTask userTask = new GetUserTask();

        try {
            userResult = userTask.execute(userURL + access_token).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if (userResult == null || userResult == "NoUser") {
            user = null;
        } else {
            user = parseJSONToUser(userResult);
        }

        return user;
    }

    // Get information from server and create User object
    private User parseJSONToUser(String userResult) {
        User user = null;

        try {
            JSONObject json = new JSONObject(userResult);

            JSONObject jsonUser = json.getJSONObject("user");

            int userID = jsonUser.getInt("userid");
            String username = jsonUser.getString("username");
            double balance = jsonUser.getDouble("balance");
            boolean password = jsonUser.getBoolean("password");
            String address = jsonUser.getString("address");
            String registeredString = jsonUser.getString("registered");
            Date registered = parseDate(registeredString);
            boolean otp_enabled = jsonUser.getBoolean("otp_enabled");
            boolean email_enabled = jsonUser.getBoolean("email_enabled");
            boolean address_enabled = jsonUser.getBoolean("address_enabled");
            int wagered = jsonUser.getInt("wagered");
            double profit = jsonUser.getDouble("profit");
            int bets = jsonUser.getInt("bets");
            int wins = jsonUser.getInt("wins");
            int losses = jsonUser.getInt("losses");
            int win_risk = jsonUser.getInt("win_risk");
            int lose_risk = jsonUser.getInt("lose_risk");
            int messages = jsonUser.getInt("messages");
            int reffered = jsonUser.getInt("referred");
            int affiliate_total = jsonUser.getInt("affiliate_total");
            int nonce = jsonUser.getInt("nonce");
            String client = jsonUser.getString("client");
            String previous_server = jsonUser.getString("previous_server");
            String previous_client = jsonUser.getString("previous_client");
            String previous_server_hashed = jsonUser.getString("previous_server_hashed");
            String next_seed = jsonUser.getString("next_seed");
            String server = jsonUser.getString("server");
            String otp_token = jsonUser.getString("otp_token");
            String otp_qr = jsonUser.getString("otp_qr");

            user = new User(userID, username, balance, password, address, registered, otp_enabled, email_enabled, address_enabled, wagered, profit, bets, wins, losses, win_risk, lose_risk, messages, reffered, affiliate_total, nonce, client, previous_server, previous_client, previous_server_hashed, next_seed, server, otp_token, otp_qr);

            Log.i("User", user.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return user;
    }

    // Parse date-text to date-object
    // TODO: FIX MAKING A DATE FROM STRING. THIS STILL CRASHES
    public static Date parseDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss'Z'");
        try {
            Date date = format.parse(dateString);
            return date;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
