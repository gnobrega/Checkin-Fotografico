package br.com.plux.checkinfotografico;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import br.com.plux.checkinfotografico.bean.UserBean;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final AppCompatActivity thisActivity = this;

        //Sincroniza a base de usuários
        Connection.activity = this;
        if( Connection.checkConnection(this.getApplicationContext()) ) {

            //Permite o acesso à interface através de Thread
            final Handler handler = new Handler() {

                @Override
                public void handleMessage(Message msg) {
                    populeFieldEmail();
                }
            };

            //Executa a atividade em background
            new Thread(new Runnable() {
                @Override
                public void run() {

                    //Busca a lista de usuários no servidor
                    String sResp = Connection.get(App.SERVER_GET_USERS);

                    try {
                        if( sResp != null ) {

                            //Limpa a base local de usuários
                            DataBase db = new DataBase(thisActivity.getApplicationContext());

                            db.clearUsers();
                            JSONObject jResp = new JSONObject(sResp);
                            JSONArray aUsers = (JSONArray)jResp.get("data");
                            for( int i = 0; i < aUsers.length(); i ++ ) {
                                JSONObject jUser = aUsers.getJSONObject(i);
                                int userId = jUser.getInt("id");
                                String userName = jUser.getString("name");
                                String userEmail = jUser.getString("email");
                                String userPass = jUser.getString("password");
                                db.insertUser(userId, userName, userEmail, userPass);
                            }

                            //Mantém a comunicação com o campo de email. Popula o campo
                            Message message = new Message();
                            handler.sendMessage(message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {

            //Popula o campo de email
            populeFieldEmail();
        }

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Verifica se já possui usuário na sessão
        UserBean userBean = Util.getUserSession(getApplicationContext());
        if( userBean != null ) {
            this.toMainActivity();
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
    }

    //Popula o campo (autocomplete) email
    public void populeFieldEmail() {
        DataBase db = new DataBase(this.getApplicationContext());
        ArrayList<UserBean> lstUsers = db.loadUsers();
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        int length = lstUsers.size();
        if( length > 0 ) {
            String[] lstEmails = new String[length];
            for (int i = 0; i < lstUsers.size(); i++) {
                UserBean userBean = lstUsers.get(i);
                lstEmails[i] = userBean.getEmail();
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,lstEmails);
            mEmailView.setAdapter(adapter);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password, this);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
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
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    public void toMainActivity() {
        Intent intentMain = new Intent(LoginActivity.this, MainActivity.class);
        LoginActivity.this.startActivity(intentMain);
        finish();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private Integer mId = null;
        private final String mEmail;
        private final String mPassword;
        private AppCompatActivity mLoginActivity;

        UserLoginTask(String email, String password, AppCompatActivity loginActivity) {
            mEmail = email;
            mPassword = password;
            mLoginActivity = loginActivity;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            //Verifica se a senha é válida
            if(!TextUtils.isEmpty(mEmail) && !TextUtils.isEmpty(mPassword)) {
                DataBase db = new DataBase(mLoginActivity.getApplicationContext());
                UserBean userBean = db.getUserAuth(mEmail, mPassword);
                if( userBean != null ) {
                    mId = userBean.getId();

                    //Armazena o usuário na sessão
                    SharedPreferences sharedpreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putInt("id", userBean.getId());
                    editor.putString("name", userBean.getName());
                    editor.putString("email", userBean.getEmail());
                    editor.commit();
                } else {
                    return false;
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                toMainActivity();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_email_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

}

