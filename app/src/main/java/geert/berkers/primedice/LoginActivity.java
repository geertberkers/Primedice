package geert.berkers.primedice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Bundle b = getIntent().getExtras();
            String info = b.getString("info");
            if(info == null){
                throw new Exception("No info found");
            }
            txtResult.setText(info);
        } catch (Exception ex) {
            // Check if access_token is saved in SharedPreferences from your mobile
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            access_token = sharedPref.getString("access_token", null);
            if (access_token != null) {
                loginFromAccestoken(access_token);
            }
        }
    }

    // TODO: Important:
    // Auto Bet
    // Chat
    // Tips

    // TODO: Later:
    // Register account
    // Set password
    // Set Email
    // Set Emergency adress
    // Use 2FA Authentication
    // Implement Affiliate information
    // Get deposits/withdrawals
    // Change seed
    // Site statistics
    // Claim faucet

    public void login(View v) {

        if (v.getId() == btnLogin.getId()) {
            String username = txtUsername.getText().toString();
            String password = txtPassword.getText().toString();

            if (username.length() == 0) {
                Toast.makeText(getApplicationContext(), "Fill in a username!", Toast.LENGTH_SHORT).show();
            } else if (password.length() == 0) {
                Toast.makeText(getApplicationContext(), "Fill in a password!", Toast.LENGTH_SHORT).show();
            } else {
                String loginResult = "NoResult";
                LoginTask login = new LoginTask();

                try {
                    loginResult = login.execute(loginUrl, username, password).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

                if (loginResult == null||loginResult.equals("NoResult")) {
                    loginResult = "Couldn't log in! Try again.";

                    txtResult.setText(loginResult);
                } else {
                    getAccestokenFromLoginResult(loginResult);

                    loginFromAccestoken(access_token);
                }
                Log.i("result", loginResult);
            }
        }
    }
    // Login with your accestoken
    private void loginFromAccestoken(String access_token) {
        String loginRegister = "Login or register!";
        if (access_token != null) {
            User user = getUser();

            if (user != null) {
                Intent betActivityIntent = new Intent(this, BetActivity.class);
                betActivityIntent.putExtra("userParcelable", user);
                betActivityIntent.putExtra("userURL", userURL);
                betActivityIntent.putExtra("access_token", access_token);
                startActivity(betActivityIntent);
                this.finish();
            } else {
                txtResult.setText(loginRegister);
            }
        } else{
            txtResult.setText(loginRegister);
        }
    }

    // Get the acces_token from the server response
    private String getAccestokenFromLoginResult(String loginResult) {
        try {
            JSONObject oneObject = new JSONObject(loginResult);

            access_token = oneObject.optString("access_token");

            if (access_token != null) {
                // Save access_token in shared preferences for automatic login next time
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                //this.getPreferences(Context.MODE_PRIVATE);
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
        String userResult = "NoResult";

        GetJSONResultFromURLTask userTask = new GetJSONResultFromURLTask();

        try {
            userResult = userTask.execute(userURL + access_token).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if (userResult == null || userResult.equals("NoResult")) {
            user = null;
        } else {
            user = new User(userResult);
        }

        return user;
    }
}
