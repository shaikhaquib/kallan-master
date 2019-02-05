package com.krayrr.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.GoogleMap;
import com.krayrr.Fragement.DashboardFragment;
import com.krayrr.Fragement.HomeFragment;
import com.krayrr.Fragement.NotificationsFragment;
import com.krayrr.Global;
import com.krayrr.Helper.API;
import com.krayrr.Helper.AppController;
import com.krayrr.Helper.SQLiteHandler;
import com.krayrr.R;
import com.krayrr.Helper.SessionManager;
import com.krayrr.StartActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DashboardFragment.OnFragmentInteractionListener, HomeFragment.OnFragmentInteractionListener, NotificationsFragment.OnFragmentInteractionListener {
    private GoogleMap mMap;
    private SessionManager session;
    SQLiteHandler db;
    HashMap<String, String> user;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragment = new HomeFragment();
                    break;
                case R.id.navigation_dashboard:
                    fragment = new DashboardFragment();
                    break;
                case R.id.navigation_notifications:
                    fragment = new NotificationsFragment();
                    break;
            }
            return loadFragment(fragment);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db=new SQLiteHandler(this);
        getDBData();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //loading the default fragment
        loadFragment(new HomeFragment());
        session = new SessionManager(getApplicationContext());

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        checkActiveCampaign();
    }

    private void checkActiveCampaign() {
        // Tag used to cancel the request
        String tag_string_req = "req_dashboard";

        StringRequest strReq = new StringRequest(Request.Method.POST, API.asign_campaign_details, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    System.out.print("asign_campaign_details"+response);

                    if (jsonObject.getBoolean("status")){

                        startActivity(new Intent(getApplicationContext(),StartActivity.class));

                    }else {

                        if (jsonObject.getString("error").equals("Session Not Login")){


                            AlertDialog.Builder builder;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
                            } else {
                                builder = new AlertDialog.Builder(MainActivity.this);
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
                        }

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_campain) {
            // Handle the camera action
        } else if (id == R.id.nav_erning) {

        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(getApplicationContext(),StartActivity.class));

        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_contact) {

        } else if (id == R.id.nav_logout) {
            session.setLogin(false);
            db.deleteUsers();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

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
