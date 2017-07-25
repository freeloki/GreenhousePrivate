package com.ardic.android.iotignite.greenhouse.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ardic.android.iotignite.greenhouse.Constants;
import com.ardic.android.iotignite.greenhouse.R;
import com.ardic.android.iotignite.greenhouse.controllers.LoginController;
import com.ardic.android.iotignite.greenhouse.utils.ValidationResult;
import com.ardic.android.iotignite.greenhouse.utils.ValidationUtils;

import java.util.concurrent.ExecutionException;


/**
 * Created by pmirkelam on 12.07.2017.
 */

public class LoginActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnFocusChangeListener, View.OnClickListener {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private String email;
    private String password;

    private EditText editTextMail;
    private EditText editTextPassword;
    private ToggleButton tbShowHidePassword;
    private Button btnSignIn;
    private TextView txtSignUpNow;
    private TextView txtForgotPassword;
    private ImageView imgMailValidate;
    private CheckBox cbRememberMe;
    private Toolbar toolbar;

    private LoginController mLoginController;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    private void initUI() {

        setContentView(R.layout.activity_login);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        editTextMail = (EditText) findViewById(R.id.activity_login_edt_email);
        imgMailValidate = (ImageView) findViewById(R.id.activity_login_img_mail_validation);
        editTextPassword = (EditText) findViewById(R.id.activity_login_edt_password);
        tbShowHidePassword = (ToggleButton) findViewById(R.id.activity_login_tb_show_hide_password);
        cbRememberMe = (CheckBox) findViewById(R.id.activity_login_cb_remember_me);
        btnSignIn = (Button) findViewById(R.id.activity_login_btn_sign_in);
        txtSignUpNow = (TextView) findViewById(R.id.activity_login_txt_sign_up_now);
        txtForgotPassword = (TextView) findViewById(R.id.activity_login_txt_forgot_password);

        //Set image 'tick' or 'cancel' according to validation of mail input
        editTextMail.setOnFocusChangeListener(this);
        editTextMail.setOnClickListener(this);

        editTextPassword.setOnClickListener(this);

        //Show or hide password characters.
        tbShowHidePassword.setOnCheckedChangeListener(this);


        //Click LOGIN then go to 'Register Gateway Activity'
        btnSignIn.setOnClickListener(this);

        //Go to 'Sign Up Activity' to create a new account
        txtSignUpNow.setOnClickListener(this);

        //Go to 'Forgot Password Activity' to create a new account
        txtForgotPassword.setOnClickListener(this);

        imgMailValidate.setVisibility(View.INVISIBLE);

        setSupportActionBar(toolbar);

        loadLoginInfo();

    }

    /**
     * @param menu adds items to the action bar if it is present
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    /**
     * Handle action bar item clicks here. The action bar will
     * automatically handle clicks on the Home/Up button, so long
     * as you specify a parent activity in AndroidManifest.xml.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_main_about) {
            Toast.makeText(this, "About Clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_main_buy_device) {
            Toast.makeText(this, "Buying Device Clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_main_faq) {
            Toast.makeText(this, "FAQ Clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onClick(View view) {

        if (view.equals(btnSignIn)) {

            if (editTextMail != null && !TextUtils.isEmpty(editTextMail.getText())) {
                email = editTextMail.getText().toString().trim();
            }
            if (editTextPassword != null && !TextUtils.isEmpty(editTextPassword.getText())) {
                password = editTextPassword.getText().toString();
            }

            // check validation
            ValidationResult result = ValidationUtils.checkLoginCredentials(email, password);

            if (result == ValidationResult.OK) {
                if (doLogin()) {
                    saveLoginInfoToPref();
                    startGatewayDashboardActivity();
                } else {
                    Toast.makeText(this, "Login failed please try again.", Toast.LENGTH_SHORT);
                }
            }

        } else if (view.equals(txtForgotPassword)) {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);

        } else if (view.equals(txtSignUpNow)) {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivityForResult(intent, Constants.CREATE_NEW_ACCOUNT);
        } else if (view.equals(editTextMail) || view.equals(editTextPassword)) {
            setCursors(true);
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {

        if (view.equals(editTextMail)) {
            email = editTextMail.getText().toString().trim();
            if (!hasFocus) {
                imgMailValidate.setVisibility(View.VISIBLE);
                if (email.matches(Constants.PATTERN_EMAIL) && email.length() > 0) {
                    imgMailValidate.setImageResource(R.drawable.tick);
                } else {
                    imgMailValidate.setVisibility(View.VISIBLE);
                    imgMailValidate.setImageResource(R.drawable.cancel);
                }
            }
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

        if (compoundButton.equals(tbShowHidePassword)) {
            if (isChecked) {
                editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT);
            } else {
                editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "Login Activity onActivityResult 1");
        if (resultCode == RESULT_OK) {
            Log.i(TAG, "Login Activity onActivityResult 2");
            email = data.getStringExtra(Constants.Extra.EXTRA_USERNAME);
            password = data.getStringExtra(Constants.Extra.EXTRA_PASSWORD);
            editTextMail.setText(email);
            editTextPassword.setText(password);
            setCursors(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private boolean doLogin() {

        boolean result;
        mLoginController = new LoginController(LoginActivity.this, email, password);
        mLoginController.execute();

        try {

            result = mLoginController.get();
            Log.i(TAG, "Login RESULT" + result);
        } catch (InterruptedException e) {
            result = false;
            Log.e(TAG, "InterruptedException on doLogin() function: " + e);
        } catch (ExecutionException e) {
            result = false;
            Log.e(TAG, "ExecutionException on doLogin() function: " + e);
        }
        return result;
    }

    private void startGatewayDashboardActivity() {
        Intent intent = new Intent(LoginActivity.this, GatewayDashboardActivity.class);
        intent.putExtra(Constants.Extra.EXTRA_USERNAME, email);
        intent.putExtra(Constants.Extra.EXTRA_PASSWORD, password);
        startActivity(intent);
    }


    private void saveLoginInfoToPref() {

        mSharedPreferences = getPreferences(Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();

        mEditor.putBoolean(Constants.IS_REMEMBER_ME_CHECKED, cbRememberMe.isChecked());

        if (cbRememberMe.isChecked()) {
            mEditor.putString(Constants.REMEMBER_MAIL, email);
            mEditor.putString(Constants.REMEMBER_PASSWORD, password);
        }
        mEditor.commit();
    }

    /**
     * loads login info if remember me is checked.
     */
    private void loadLoginInfo() {

        mSharedPreferences = getPreferences(Context.MODE_PRIVATE);

        boolean isChecked = mSharedPreferences.getBoolean(Constants.IS_REMEMBER_ME_CHECKED, false);
        cbRememberMe.setChecked(isChecked);

        if (isChecked) {
            email = mSharedPreferences.getString(Constants.REMEMBER_MAIL, null);
            password = mSharedPreferences.getString(Constants.REMEMBER_PASSWORD, null);
            editTextMail.setText(email);
            editTextPassword.setText(password);
            setCursors(false);
        }
    }

    private void setCursors(boolean state) {
        if (editTextMail != null) {
            editTextMail.setCursorVisible(state);
        }
        if (editTextPassword != null) {
            editTextPassword.setCursorVisible(state);
        }
    }
}
