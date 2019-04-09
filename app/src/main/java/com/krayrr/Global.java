package com.krayrr;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.krayrr.Activity.MainActivity;
import com.krayrr.Helper.API;
import com.krayrr.Helper.AppController;
import com.krayrr.Helper.DbHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Shaikh Aquib on 26-Apr-18.
 */

public class Global {
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;
    public static String id = null;
    public static String uid = null;
    public static String ltype = null;
    public static String email = null;
    public static String mobile = null;
    public static String username = null;
    public static String carregno  = null;
    public static String fueltype = null;
    public static String carno    = null;
    public static String Sessionid = null;
    public static String client_id = null;
    public static String campaign_id = null;
    public static String ride_id     = null;
    public static String car_id     = null;
    public static String campaign_name  = null;
    public static String camp_start_date= null;
    public static String camp_end_date  = null;
    public static String campaign_purpose = null;


    public static int notificationcount = 0;

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String getDateCurrentTimeZone() {
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c);
        return formattedDate;
    }

    public static void diloge(final Context context ,String Title ,String Message){

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        builder.setCancelable(false);
        builder.setTitle(Title)
                .setMessage(Message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

   public static String Date(String date){
        try {
            SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            System.out.println(sdf2.format(sdf.parse(date)));
            date = sdf2.format(sdf.parse(date));
        }
        catch(Exception e) {
            //java.text.ParseException: Unparseable date: Geting error
            System.out.println("Excep"+e);
        }

        return date;
    }

    public static String CurrencyFormat(String Amount) {

        String number = Amount;
        double amount = Double.parseDouble(number);
        DecimalFormat formatter = new DecimalFormat("#,###.");
        String formatted = formatter.format(amount);

        //holder.amount.setText("₹"+formatted);

        return formatted;
    }

    public static void actionbar(Activity activity , ActionBar abar , String title){
        View viewActionBar = activity.getLayoutInflater().inflate(R.layout.actionbar, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams( //Center the textview in the ActionBar !
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        TextView textviewTitle = (TextView) viewActionBar.findViewById(R.id.actionbar_textview);
        textviewTitle.setText(title);
        abar.setCustomView(viewActionBar, params);
        abar.setDisplayShowCustomEnabled(true);
        abar.setDisplayShowTitleEnabled(false);
        abar.setHomeButtonEnabled(true);
    }



    public static void failedDilogue(final Context context ,String result) {

        final Dialog dialog = new Dialog(context);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.failediloge);
        Button button = dialog.findViewById(R.id.btnfailed);
        TextView textView = dialog.findViewById(R.id.failedreson);
        textView.setText(result);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    public static void successDilogue(final Context context , String result) {
        final Dialog dialog = new Dialog(context);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.success_dilogue);
        Button button = dialog.findViewById(R.id.btnsucces);
        TextView textView = dialog.findViewById(R.id.successtext);
        textView.setText(result);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }


    public static void car_ride_start(final String strlocation) {



        StringRequest stringRequest = new StringRequest(StringRequest.Method.POST, API.car_ride_start, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                try {
                    JSONObject object= new JSONObject(response);
                    if (object.getBoolean("status")){
                        Log.d("Start Ride",response);

                        JSONObject jsonObject = object.getJSONArray("ride_details").getJSONObject(0);


                        Global.mobile=jsonObject.getString("active_mobile_no");
                        Global.car_id=jsonObject.getString("ride_id");
                        Global.carno=jsonObject.getString("car_no");
                        Global.Sessionid=jsonObject.getString("session_id");
                        Global.ride_id=jsonObject.getString("ride_id");



                        //   Global.successDilogue(MapsActivity.this,object.getString("success_msg"));

                       //Toast.makeText(context, "Data Successfully uploaded", Toast.LENGTH_SHORT).show();
                       // sqLiteHandler.deleteride();
                    }else{
                      //  Global.successDilogue(context,object.getString("error_msg"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){            @Override
        protected Map<String, String> getParams() throws AuthFailureError {

            Map<String, String>  params = new HashMap<String, String>();

            params.put("user_id"         , Global.uid);
            params.put("session_id"      , Global.Sessionid);
            params.put("campaign_id"     , Global.campaign_id);
            params.put("car_no"          , Global.carno);
            params.put("active_mobile_no", Global.mobile);
            params.put("live_coordinate" , strlocation);
            params.put("status"          ,"start");
            params.put("kilometer"       ,"0");

            return params;
        }};

        AppController.getInstance().addToRequestQueue(stringRequest);

    }


}
