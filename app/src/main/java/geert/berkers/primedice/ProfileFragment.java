package geert.berkers.primedice;

import android.app.Fragment;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Primedice Application Created by Geert on 2-2-2016.
 */
//TODO: Implement profile
public class ProfileFragment extends Fragment {

    private View view;
    private MainActivity activity;

    private User user;
    private LinearLayout showedLogLayout, showedAffiliateLayout;
    private RelativeLayout securityLayout, showedSecurityLayout, logLayout, affiliateLayout;

    private TextView txtUsername, txtDateJoined, lblEmail, txtTwoFactorSet, txtEmergencyAddressSet, securityPlus, logPlus, affiliatePlus, txtAffiliateLink, txtAffiliateInformation;

    private Button btnSetPassword, btnSetEmail, btnSetTwoFactor, btnSetEmergencyAddress, btnTransactionLog, btnTipLog, btnContactSupport;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            this.view = inflater.inflate(R.layout.fragment_profile, container, false);

            initControls();
            setListeners();
            checkToHide();
        }

        setInformation();

        return view;
    }

    private void initControls() {
        activity = (MainActivity) getActivity();

        securityLayout = (RelativeLayout) view.findViewById(R.id.securityLayout);
        showedSecurityLayout = (RelativeLayout) view.findViewById(R.id.showedSecurityLayout);
        logLayout = (RelativeLayout) view.findViewById(R.id.logsLayout);
        showedLogLayout = (LinearLayout) view.findViewById(R.id.showedLogsLayout);
        affiliateLayout = (RelativeLayout) view.findViewById(R.id.affiliateLayout);
        showedAffiliateLayout = (LinearLayout) view.findViewById(R.id.showedAffiliateLayout);

        logPlus = (TextView) view.findViewById(R.id.logsPlus);
        lblEmail = (TextView) view.findViewById(R.id.lblEmail);
        txtUsername = (TextView) view.findViewById(R.id.txtUsername);
        securityPlus = (TextView) view.findViewById(R.id.securityPlus);
        affiliatePlus = (TextView) view.findViewById(R.id.affiliatePlus);
        txtDateJoined = (TextView) view.findViewById(R.id.txtDateJoined);
        txtAffiliateLink = (TextView) view.findViewById(R.id.affiliateLink);
        txtTwoFactorSet = (TextView) view.findViewById(R.id.txtTwoFactorSet);
        txtEmergencyAddressSet = (TextView) view.findViewById(R.id.txtEmergencyAddressSet);
        txtAffiliateInformation = (TextView) view.findViewById(R.id.showAffiliateInformation);

        btnTipLog = (Button) view.findViewById(R.id.btnTipLog);
        btnSetEmail = (Button) view.findViewById(R.id.btnEmail);
        btnSetTwoFactor = (Button) view.findViewById(R.id.btnTwoFactor);
        btnSetPassword = (Button) view.findViewById(R.id.btnSetPassword);
        btnContactSupport = (Button) view.findViewById(R.id.btnContactSupport);
        btnTransactionLog = (Button) view.findViewById(R.id.btnTransactionLog);
        btnSetEmergencyAddress = (Button) view.findViewById(R.id.btnSetEmergencyAddress);

        showedLogLayout.setVisibility(View.GONE);
        showedSecurityLayout.setVisibility(View.GONE);
        showedAffiliateLayout.setVisibility(View.GONE);
    }

    private void setListeners() {
        securityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showedSecurityLayout.getVisibility() == View.VISIBLE) {
                    showedSecurityLayout.setVisibility(View.GONE);
                    securityPlus.setText("+");
                } else {
                    showedSecurityLayout.setVisibility(View.VISIBLE);
                    securityPlus.setText("-");
                }
            }
        });

        logLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showedLogLayout.getVisibility() == View.VISIBLE) {
                    showedLogLayout.setVisibility(View.GONE);
                    logPlus.setText("+");
                } else {
                    showedLogLayout.setVisibility(View.VISIBLE);
                    logPlus.setText("-");
                }
            }
        });

        affiliateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showedAffiliateLayout.getVisibility() == View.VISIBLE) {
                    showedAffiliateLayout.setVisibility(View.GONE);
                    affiliatePlus.setText("+");
                } else {
                    showedAffiliateLayout.setVisibility(View.VISIBLE);
                    affiliatePlus.setText("-");
                }
            }
        });

        btnSetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPassword();
            }
        });

        btnSetEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEmail();
            }
        });

        btnSetTwoFactor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTwoFactor();
            }
        });

        btnSetEmergencyAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEmergencyAddress();
            }
        });

        btnTransactionLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTransactionLog();
            }
        });

        btnTipLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTipLog();
            }
        });

        txtAffiliateInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAffiliateInfo();
            }
        });

        txtAffiliateLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyAffiliateLink();
            }
        });

        btnContactSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactSupport();
            }
        });
    }

    private void checkToHide() {
        user = activity.getUser();

        if (user.address_enabled) {
            Log.w("Address", "Enabled");
            btnSetEmergencyAddress.setVisibility(View.INVISIBLE);
            txtEmergencyAddressSet.setVisibility(View.VISIBLE);
        } else {
            btnSetEmergencyAddress.setVisibility(View.VISIBLE);
            txtEmergencyAddressSet.setVisibility(View.INVISIBLE);
            Log.w("Address", "User need to set a address!");
        }

        if (user.email_enabled) {
            Log.w("Email", "Enabled");
            lblEmail.setVisibility(View.GONE);
            btnSetEmail.setVisibility(View.GONE);
            View emailDivider = view.findViewById(R.id.emailDivider);
            emailDivider.setVisibility(View.GONE);
        } else {
            Log.w("Email", "User need to set a email!");
        }

        if (user.password) {
            Log.w("Password", "Enabled");
            btnSetPassword.setText("CHANGE");
        } else {
            Log.w("Password", "User need to set a password!");
            btnSetPassword.setText("SET");
        }

        if (user.otp_enabled) {
            Log.w("OTP", "Enabled");
            btnSetTwoFactor.setVisibility(View.INVISIBLE);
            txtTwoFactorSet.setVisibility(View.VISIBLE);
        } else {
            btnSetTwoFactor.setVisibility(View.VISIBLE);
            txtTwoFactorSet.setVisibility(View.INVISIBLE);
        }

    }

    private void setInformation() {
        txtDateJoined.setText(user.getRegistered());
        txtUsername.setText(user.getUsername() + "'s Account");
        txtAffiliateLink.setText("https://primedice.com/?ref=" + user.getUsername());
    }

    private void setPassword() {
        //TODO: Set password
    }

    private void setEmail() {
        //TODO: Set email
    }

    private void setTwoFactor() {
        //TODO: Set TwoFactor
    }

    private void setEmergencyAddress() {
        //TODO: Set EmergencyAddress
    }

    private void showTransactionLog() {
        //TODO: Shop TransactionLog
    }

    private void showTipLog() {
        //TODO: Show tiplog
    }

    private void openAffiliateInfo() {
        //TODO: Open screen with affiliate information
    }

    private void copyAffiliateLink() {
        //TODO: Set affiliateLink to clipboard
    }

    private void contactSupport() {
        //TODO: Contact Support
    }
}
