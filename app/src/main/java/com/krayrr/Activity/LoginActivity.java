package com.krayrr.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.krayrr.Global;
import com.krayrr.Helper.API;
import com.krayrr.Helper.AppController;
import com.krayrr.R;
import com.krayrr.Helper.SQLiteHandler;
import com.krayrr.Helper.SessionManager;
import com.krayrr.StartActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    private UserLoginTask mAuthTask = null;
    SessionManager session;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    HashMap<String, String> user;

    SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        db = new SQLiteHandler(this);
     //   getData();
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();
        session = new SessionManager(getApplicationContext());
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
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
                Global.hideKeyboard(LoginActivity.this);
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity


            getDBData();
            checkActiveCampaign();

        }

    }


    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
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
           // mAuthTask = new UserLoginTask();
           new UserLoginTask().execute(email, password);
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

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<String, String, String> {

        private  String mEmail;
        private  String mPassword;
        HttpURLConnection conn;
        URL url = null;

/*
        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }
*/

        @Override
        protected String doInBackground(String... params) {
            try {
                url = new URL(API.LoginUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);


                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("username", params[0])
                        .appendQueryParameter("password", params[1])
                        ;
                String query = builder.build().getEncodedQuery();
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {

                int response_code = conn.getResponseCode();
                Log.d("responce Code",String.valueOf(response_code));

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    // Log.d("Result",result.toString());
                    return(result.toString());


                }else{

                    return("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return "exception";
            } finally {
                conn.disconnect();
            }


        }

        @Override
        protected void onPostExecute(String s) {
            showProgress(false);
            if (!s.equals("exception") && !s.equals("unsuccessful")){
                try {

                    JSONObject jsonObject =new JSONObject(s);
                    if (jsonObject.has("logged_in")){
                        Boolean status;
                        status = jsonObject.getBoolean("logged_in");
                        if (!jsonObject.getBoolean("logged_in")){
                            showProgress(false);
                            Snackbar.make(mEmailView, jsonObject.getString("login_error_msg"), Snackbar.LENGTH_LONG).show();
                        }else {
                            JSONObject object =jsonObject.getJSONObject("car_details");

                            Global.id =object.getString("user_id");
                            Global.Sessionid=object.getString("session_id");

                            db.addUser(
                                   object.getString("username"),  object.getString("user_id"),
                                   object.getString("login_type"),object.getString("email"),
                                   object.getString("mobile_number"),object.getString("car_registration_no"),
                                   object.getString("fuel_type"),    object.getString("car_no"),
                                   object.getString("registration_year"),object.getString("session_id")
                            );
                            showProgress(false);
                            session.setLogin(true);
                            getDBData();
                            checkActiveCampaign();
                   //         finish();
                        }}


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                showProgress(false);
                Snackbar.make(mEmailView, "Some thing went wrong! Check your Connection", Snackbar.LENGTH_INDEFINITE ).setAction("CLOSE", new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                }).show();

            }

        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private void checkActiveCampaign() {
        // Tag used to cancel the request
        String tag_string_req = "req_dashboard";
        showProgress(true);
        StringRequest strReq = new StringRequest(Request.Method.POST, API.asign_campaign_details, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    System.out.print("asign_campaign_details"+response);

                    if (jsonObject.getBoolean("status")){


                        JSONArray jsonArray = jsonObject.getJSONArray("assign_campaigns");
                        JSONObject object = jsonArray.getJSONObject(0);

                        Global.campaign_id= object.getString("campaign_id");
                        Global.client_id=object.getString("client_id");
                        Global.campaign_name=object.getString("campaign_name");
                        Global.campaign_purpose=object.getString("campaign_purpose");
                        Global.camp_start_date=object.getString("camp_start_date");
                        Global.camp_end_date=object.getString("camp_end_date");

                        startActivity(new Intent(getApplicationContext(),StartActivity.class));
                        finish();

                    }else {

                        if (jsonObject.getString("error").equals("Session Not Login")){


                            AlertDialog.Builder builder;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                builder = new AlertDialog.Builder(LoginActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
                            } else {
                                builder = new AlertDialog.Builder(LoginActivity.this);
                            }
                            builder.setTitle("Session Expired!")
                                    .setMessage("Your Login session has expired plese login again to continue.")
                                    .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // continue with delete
                                            session.setLogin(false);
                                            db.deleteUsers();
                                            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }else {
                            Intent intent = new Intent(LoginActivity.this, Errorpage.class);
                            startActivity(intent);
                            finish();
                        }

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar.make(mEmailView, "Some thing went wrong! Check your Connection !", Snackbar.LENGTH_INDEFINITE).show();
            }
        }){@Override
        protected Map<String, String> getParams() throws AuthFailureError {

            Map<String, String>  params = new HashMap<String, String>();

            params.put("user_id", Global.uid);
            params.put("session_id", Global.Sessionid);
            //   params.put("token", notificationToken);

            return params;
        }};

        // Adding request to request queue
        strReq.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
    private void getDBData() {
        user = db.getUserDetails();
        //  Global.id = user.get("id");
        //  Log.d("VAr",Global.id);
        Global.uid  = user.get("uid");
        Log.d("VAr",Global.uid);
        Global.ltype  = user.get("ltype");
        Log.d("VAr",Global.ltype);
        Global.email  = user.get("email");
        Log.d("VAr",Global.email);
        Global.mobile = user.get("mobile");
        Log.d("VAr",Global.mobile);
        Global.username = user.get("username");
        Log.d("VAr",Global.username);
        Global.carregno = user.get("carregno");
        Log.d("VAr",Global.carregno);
        Global.fueltype = user.get("fueltype");
        Log.d("VAr",Global.fueltype);
        Global.carno    = user.get("carno");
        Log.d("VAr",Global.carno);
        Global.Sessionid    = user.get("sessionid");
        Log.d("VAr",Global.Sessionid);
    }

}

