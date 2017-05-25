package de.m4lik.burningseries.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.m4lik.burningseries.R;
import de.m4lik.burningseries.api.API;
import de.m4lik.burningseries.api.APIInterface;
import de.m4lik.burningseries.database.MainDBHelper;
import de.m4lik.burningseries.services.SyncBroadcastReceiver;
import de.m4lik.burningseries.util.Settings;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static de.m4lik.burningseries.services.ThemeHelperService.theme;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements Callback<ResponseBody> {

    Boolean loginInProgress = false;

    @BindView(R.id.user)
    EditText userEditText;

    @BindView(R.id.password)
    EditText passwordEditText;

    @BindView(R.id.login_progress)
    View mProgressView;

    @BindView(R.id.email_login_form)
    View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(theme().basic);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        if (getApplicationContext().getResources().getBoolean(R.bool.isTablet))
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        else
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        ((ImageView) findViewById(R.id.login_logo_image))
                .setColorFilter(ContextCompat.getColor(LoginActivity.this, theme().primaryColor));

        super.onCreate(savedInstanceState);

        Button mEmailSignInButton = (Button) findViewById(R.id.login_button);
        mEmailSignInButton.setOnClickListener(view -> attemptLogin());
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        userEditText.setError(null);
        passwordEditText.setError(null);

        // Store values at the time of the login attempt.
        String user = userEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.error_password_empty));
            focusView = passwordEditText;
            cancel = true;
        }

        // Check for a valid user address.
        if (TextUtils.isEmpty(user)) {
            userEditText.setError(getString(R.string.error_user_empty));
            focusView = userEditText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            //Try to login
            loginInProgress = true;
            showProgress(true);
                API api = new API();
                api.setSession("");
                api.generateToken("login");
                APIInterface apii = api.getInterface();
                Call<ResponseBody> call = apii.login(api.getToken(), api.getUserAgent(), user, password);
                call.enqueue(this);
        }
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        showProgress(false);
        try {
            String json = response.body().string();
            if (json.contains("\"error\"")) {
                userEditText.setError(getString(R.string.error_invalid_credentials));
                Answers.getInstance().logLogin(new LoginEvent()
                        .putMethod("Username")
                        .putSuccess(false));
            } else {
                /*
                 * {"user":"name","session":"sessionstring"} -> sessionstring
                 */
                Answers.getInstance().logLogin(new LoginEvent()
                        .putMethod("Username")
                        .putSuccess(true));

                SyncBroadcastReceiver.scheduleNextSync(this);

                String[] data = json.split(",");
                String session = data[1].split(":")[1].replace("\"", "").replace("}", "").trim();
                String user = data[0].split(":")[1].replace("\"", "").replace("}", "").trim();
                Settings.of(this).raw().edit()
                        .putString("pref_session", session)
                        .putString("pref_user", user)
                        .commit();

                Context context = getApplicationContext();

                getApplicationContext().deleteDatabase(MainDBHelper.DATABASE_NAME);

                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Crashlytics.log(Log.ERROR, "LOGIN", "Error while login: " + e.toString());
            Crashlytics.logException(e);
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        showProgress(false);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}

