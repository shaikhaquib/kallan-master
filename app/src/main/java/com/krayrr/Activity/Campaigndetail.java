package com.krayrr.Activity;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.krayrr.Global;
import com.krayrr.Helper.API;
import com.krayrr.Helper.AppController;
import com.krayrr.R;
import com.krayrr.ViewPagerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Campaigndetail extends AppCompatActivity {

    String [] trip = {"5 JAN 2018","5 April 2018", "5 May 2018"} ;
    String [] earning ={"RS.250", "RS.450", "RS.500"};
    String [] distance  ={"20 KM", "45 KM", "50 KM"};
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy-MM-dd");
    RecyclerView recyclerView;
    TextView name , strDate, prgendDate, prgstrDate, purpose ;
    View parentLayout;
    String[] imagearray ;
    ViewPager viewPager;
    LinearLayout sliderDotspanel;
    private int dotscount;
    private ImageView[] dots;
    String currentversion;

    AppController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campaigndetail);

        //controller =new AppController();
        name = findViewById(R.id.detailcampaignName);
        strDate=findViewById(R.id.strDate);
        prgstrDate=findViewById(R.id.prgstrDate);
        prgendDate=findViewById(R.id.prgendDate);
        purpose=findViewById(R.id.detailPurpose);

        recyclerView = findViewById(R.id.rvEarningOverview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.earningoverview,parent,false);
                Holder holder =new Holder(view);
                return holder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                Holder holder1=(Holder) holder;
                holder1.trip.setText(trip[position]);
                holder1.eraning.setText(earning[position]);
                holder1.distance.setText(distance[position]);
            }

            @Override
            public int getItemCount() {
                return earning.length;
            }
            class Holder extends RecyclerView.ViewHolder{
                TextView trip , eraning ,distance ;
                public Holder(View itemView) {
                    super(itemView);
                    trip=itemView.findViewById(R.id.ovTrip);
                    eraning=itemView.findViewById(R.id.ovEarning);
                    distance=itemView.findViewById(R.id.ovDistance);
                }
            }
        });

        details();
    }

    private void details() {
        // Tag used to cancel the request
        String tag_string_req = "req_dashboard";

        StringRequest strReq = new StringRequest(Request.Method.POST, API.CampaignDetailUrl, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
          //      response ="{\"campaigns\":{\"campaign_data\":[{\"campaign_id\":\"1\",\"client_id\":\"1\",\"campaign_name\":\"Campaign Demo\",\"camp_start_date\":\"2018-07-01\",\"camp_end_date\":\"2018-07-30\",\"campaign_purpose\":\"Test Campaign\",\"status\":\"active\"}],\"campaign_images\":[{\"camp_img_id\":\"1\",\"campaign_id\":\"1\",\"img_name\":\"1_1_5y8ES_0.jpg\",\"image_link\":\"http:\\/\\/dev.krayrr.com\\/uploads\\/campaign_media\\/1\\/1_1_5y8ES_0.jpg\",\"status\":\"active\"},{\"camp_img_id\":\"2\",\"campaign_id\":\"1\",\"img_name\":\"1_1_5y8ES_1.jpg\",\"image_link\":\"http:\\/\\/dev.krayrr.com\\/uploads\\/campaign_media\\/1\\/1_1_5y8ES_1.jpg\",\"status\":\"active\"}]}}";
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject object =jsonObject.getJSONObject("campaigns");
                    JSONArray array = object.getJSONArray("campaign_data");
                    JSONObject detail =array.getJSONObject(0);

                    name.setText(detail.getString("campaign_name"));
                    strDate.setText(detail.getString("camp_start_date"));
                    prgendDate.setText(detail.getString("camp_end_date"));
                    prgstrDate.setText(detail.getString("camp_start_date"));
                    purpose.setText(detail.getString("campaign_purpose"));

                    Date date1 = simpleDateFormat.parse(detail.getString("camp_start_date"));
                    Date date2 = simpleDateFormat.parse(detail.getString("camp_end_date"));

                    printDifference(date1, date2);

                    ArrayList<String> dataList = new ArrayList<String>();

                    JSONArray array1 =object.getJSONArray("campaign_images");
                    for (int i = 0; i < array1.length(); i++) {
                        JSONObject json_data = array1.getJSONObject(i);
                        dataList.add(json_data.getString("image_link"));

                    }
                    imagearray=dataList.toArray(new String[dataList.size()]);
                    slider();

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
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

            params.put("campaign_id", getIntent().getStringExtra("Cid"));
            params.put("user_id", Global.uid);
            params.put("session_id", Global.Sessionid);
            //   params.put("token", notificationToken);

            return params;
        }};

        // Adding request to request queue
        strReq.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    public void slider(){ viewPager = (ViewPager) findViewById(R.id.catviewPager);

        sliderDotspanel = (LinearLayout) findViewById(R.id.catSliderDots);

        if (imagearray.length<2){

            sliderDotspanel.setVisibility(View.GONE );
        }

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this,imagearray);

        viewPager.setAdapter(viewPagerAdapter);

        dotscount = viewPagerAdapter.getCount();
        dots = new ImageView[dotscount];

        for(int i = 0; i < dotscount; i++){

            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.nonactive_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            params.setMargins(8, 0, 8, 0);

            sliderDotspanel.addView(dots[i], params);

        }

//        dots[0].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_dot));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                for(int i = 0; i< dotscount; i++){
                    dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.nonactive_dot));
                }

                dots[position].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_dot));

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new MyTimerTask(), 2000, 4000);}
    public class MyTimerTask extends TimerTask {

        @Override
        public void run() {

            Campaigndetail.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if(viewPager.getCurrentItem() == 0){
                        viewPager.setCurrentItem(1);
                    } else if(viewPager.getCurrentItem() == 1){
                        viewPager.setCurrentItem(2);
                    } else if(viewPager.getCurrentItem() == 2){
                        viewPager.setCurrentItem(3);
                    } else if(viewPager.getCurrentItem() == 3){
                        viewPager.setCurrentItem(4);
                    }else if(viewPager.getCurrentItem() == 4){
                        viewPager.setCurrentItem(5);
                    } else if(viewPager.getCurrentItem() == 5){
                        viewPager.setCurrentItem(0);
                    }


                }
            });

        }
    }

    public void printDifference(Date startDate, Date endDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        System.out.println("startDate : " + startDate);
        System.out.println("endDate : "+ endDate);
        System.out.println("different : " + different);

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long month = 45/30;

        Calendar startCalendar = new GregorianCalendar();
        startCalendar.setTime(startDate);
        Calendar endCalendar = new GregorianCalendar();
        endCalendar.setTime(endDate);

        int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        int diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        System.out.print(diffMonth);

        System.out.printf(
                "%d days, %d hours, %d minutes, %d seconds%n",
                elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds);
    }

}
