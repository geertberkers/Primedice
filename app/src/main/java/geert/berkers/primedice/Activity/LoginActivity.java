package geert.berkers.primedice.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

import geert.berkers.primedice.DataHandler.Encrypter;
import geert.berkers.primedice.Data.URL;
import geert.berkers.primedice.DataHandler.PostToServerTask;
import geert.berkers.primedice.R;
import geert.berkers.primedice.DataHandler.GetFromServerTask;
import geert.berkers.primedice.Data.User;

public class LoginActivity extends AppCompatActivity {

    private static final String PRIVATE_KEY = "GeertsPrivateKey";

    private Button btnLogin;
    private CheckBox cbRememberMe;
    private TextView txtResult, txtRegister;
    private EditText txtUsername, txtPassword, txtTFA;

    private boolean rememberMe = false;
    private static String access_token = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setBackground();

        btnLogin = (Button) findViewById(R.id.btnLogin);
        txtResult = (TextView) findViewById(R.id.txtResult);
        txtRegister = (TextView) findViewById(R.id.txtRegister);
        cbRememberMe = (CheckBox) findViewById(R.id.cbRememberMe);

        txtTFA = (EditText) findViewById(R.id.etTFA);
        txtUsername = (EditText) findViewById(R.id.etUsername);
        txtPassword = (EditText) findViewById(R.id.etPassword);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(R.mipmap.primedice);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.primedicecolor)));
        }

        txtRegister.setPaintFlags(txtRegister.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lbl;
                String button;

                if (txtRegister.getText().toString().equals("Register")) {
                    button = "GET STARTED";
                    lbl = "Login";
                    txtPassword.setVisibility(View.GONE);
                    txtTFA.setVisibility(View.GONE);
                } else {
                    button = "LOGIN";
                    lbl = "Register";
                    txtPassword.setVisibility(View.VISIBLE);
                    txtTFA.setVisibility(View.VISIBLE);
                }

                btnLogin.setText(button);
                txtRegister.setText(lbl);
            }
        });
    }

    private void setBackground() {
        LinearLayout loginActivity = (LinearLayout) findViewById(R.id.loginActivity);
        Bitmap backgroundImage = BitmapFactory.decodeResource(this.getResources(), R.drawable.pd_background_portrait_small);
        backgroundImage = resizeBackgroundImage(backgroundImage);
        BitmapDrawable background = new BitmapDrawable(backgroundImage);
        loginActivity.setBackgroundDrawable(background);
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            Bundle b = getIntent().getExtras();
            String info = b.getString("info");
            if (info == null) {
                throw new Exception("No info found");
            }
            txtResult.setText(info);
        } catch (Exception ex) {
            // Check if access_token is saved in SharedPreferences from your mobile
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            rememberMe = sharedPref.getBoolean("remember_me", false);
            cbRememberMe.setChecked(rememberMe);

            if (rememberMe) {
                try {
                    Encrypter encrypter = new Encrypter(PRIVATE_KEY);
                    String encryptedString = sharedPref.getString("access_token", null);

                    if (encryptedString != null) {

                        byte[] encrypted = Base64.decode(encryptedString, Base64.DEFAULT);
                        byte[] decrypted = encrypter.decrypt(encrypted);

                        access_token = new String(decrypted, "UTF-8");

                        loginFromAccessToken(access_token);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Register a user
    private void register(String username) {
        try {
            String urlParameters = "username=" + URLEncoder.encode(String.valueOf(username), "UTF-8") + "&affiliate=GeertDev";

            PostToServerTask postToServerTask = new PostToServerTask();
            String result = postToServerTask.execute(URL.REGISTER, urlParameters).get();

            if (result != null) {
                getAccestokenFromLoginResult(result);
                loginFromAccessToken(access_token);
            } else {
                String registerFailed = "Registering failed!";
                txtResult.setText(registerFailed);
            }
        } catch (UnsupportedEncodingException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Log in
    public void login(View v) {

        if (v.getId() == btnLogin.getId()) {

            rememberMe = cbRememberMe.isChecked();
            String username = txtUsername.getText().toString();

            if (btnLogin.getText().toString().equals("GET STARTED")) {
                register(username);
            } else {
                String tfa = txtTFA.getText().toString();
                String password = txtPassword.getText().toString();

                if (username.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Fill in a username!", Toast.LENGTH_SHORT).show();
                } else if (password.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Fill in a password!", Toast.LENGTH_SHORT).show();
                } else {

                    try {
                        String urlParameters = "username=" + URLEncoder.encode(username, "UTF-8")
                                + "&password=" + URLEncoder.encode(password, "UTF-8");

                        if (tfa.length() != 0) {
                            urlParameters = urlParameters + "&otp=" + URLEncoder.encode(tfa, "UTF-8");
                        }

                        PostToServerTask loginTast = new PostToServerTask();
                        String loginResult = loginTast.execute(URL.LOG_IN, urlParameters, tfa).get();

                        if (loginResult == null) {
                            loginResult = "Couldn't log in! Try again.";
                            txtResult.setText(loginResult);
                        } else {
                            getAccestokenFromLoginResult(loginResult);
                            loginFromAccessToken(access_token);
                        }

                        Log.i("LOGIN_RESULT", loginResult);

                    } catch (InterruptedException | ExecutionException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // Login with your access_token
    private void loginFromAccessToken(String access_token) {
        String loginRegister = "Login or register!";
        if (access_token != null) {
            User user = getUser(access_token);

            if (user != null) {
                Intent mainActivity = new Intent(this, MainActivity.class);
                mainActivity.putExtra("userParcelable", user);
                mainActivity.putExtra("access_token", access_token);
                startActivity(mainActivity);
                this.finish();
            } else {
                txtResult.setText(loginRegister);
            }
        } else {
            txtResult.setText(loginRegister);
        }
    }

    // Get the acces_token from the server response
    private void getAccestokenFromLoginResult(String loginResult) {
        try {
            JSONObject oneObject = new JSONObject(loginResult);

            access_token = oneObject.optString("access_token");

            if (rememberMe) {
                if (access_token != null) {
                    // Save access_token in shared preferences for automatic login next time
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.clear();
                    editor.putBoolean("remember_me", rememberMe);
                    try {
                        Encrypter encrypter = new Encrypter(PRIVATE_KEY);
                        byte[] encrypted = encrypter.encrypt(access_token.getBytes("UTF-8"));
                        String encryptedString = Base64.encodeToString(encrypted, Base64.DEFAULT);
                        editor.putString("access_token", encryptedString);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //editor.putString("access_token", access_token);
                    editor.apply();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Create user object from server response
    public static User getUser(String access_token) {
        try {
            GetFromServerTask userTask = new GetFromServerTask();
            String userResult = userTask.execute(URL.USER + access_token).get();

            if (userResult != null) {
                return new User(userResult);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Bitmap resizeBackgroundImage(Bitmap bitmap) {
        if (bitmap.getHeight() > 4096 || bitmap.getWidth() > 4096) {
            int width = (int) (bitmap.getWidth() * 0.9);
            int height = (int) (bitmap.getHeight() * 0.9);

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

            return resizeBackgroundImage(resizedBitmap);

        } else{
            return bitmap;
        }
    }
}
