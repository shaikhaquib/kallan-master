package com.krayrr.Fragement;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.krayrr.Activity.Campaigndetail;
import com.krayrr.Activity.LoginActivity;
import com.krayrr.Global;
import com.krayrr.Helper.API;
import com.krayrr.Helper.AppController;
import com.krayrr.Helper.SessionManager;
import com.krayrr.Model.CampaignModel;
import com.krayrr.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    RecyclerView recyclerView;
    SessionManager sessionManager;
    ArrayList<CampaignModel> models=new ArrayList<>();
    int[] images ={R.drawable.banner,R.drawable.download,R.drawable.images};
    String [] names = {"Kolkata Knight Riders","Gold Coast 2018","HP" };

    private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.rvCampaign);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        sessionManager=new SessionManager(getActivity());
        campaignService();

        return view;
    }

    private void campaignService() {
        // Tag used to cancel the request
        String tag_string_req = "req_dashboard";

        StringRequest strReq = new StringRequest(Request.Method.POST, API.CampaignUrl, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                //response = "{\"campaign_data\":[{\"campaign_id\":\"1\",\"client_id\":\"1\",\"campaign_name\":\"Campaign Demo\",\"camp_start_date\":\"2018-07-01\",\"camp_end_date\":\"2018-07-30\",\"campaign_purpose\":\"Test Campaign\",\"status\":\"active\"},{\"campaign_id\":\"2\",\"client_id\":\"1\",\"campaign_name\":\"Campaign Demo 2\",\"camp_start_date\":\"2018-08-01\",\"camp_end_date\":\"2018-08-30\",\"campaign_purpose\":\"Test Campaign 2\",\"status\":\"active\"}],\"campaign_images\":[{\"camp_img_id\":\"1\",\"campaign_id\":\"1\",\"img_name\":\"1_1_5y8ES_0.jpg\",\"image_link\":\"http:\\/\\/dev.krayrr.com\\/uploads\\/campaign_media\\/1\\/1_1_5y8ES_0.jpg\",\"status\":\"active\"},{\"camp_img_id\":\"2\",\"campaign_id\":\"1\",\"img_name\":\"1_1_5y8ES_1.jpg\",\"image_link\":\"http:\\/\\/dev.krayrr.com\\/uploads\\/campaign_media\\/1\\/1_1_5y8ES_1.jpg\",\"status\":\"active\"},{\"camp_img_id\":\"3\",\"campaign_id\":\"2\",\"img_name\":\"1_2_NdeRp_0.jpg\",\"image_link\":\"http:\\/\\/dev.krayrr.com\\/uploads\\/campaign_media\\/1\\/1_2_NdeRp_0.jpg\",\"status\":\"active\"},{\"camp_img_id\":\"4\",\"campaign_id\":\"2\",\"img_name\":\"1_2_NdeRp_1.jpg\",\"image_link\":\"http:\\/\\/dev.krayrr.com\\/uploads\\/campaign_media\\/1\\/1_2_NdeRp_1.jpg\",\"status\":\"active\"}]}";
                if (!response.equals("\"Session Not Login\"")){
                    try {
                       //JSONObject resp = new JSONObject(response);
                       // JSONObject object =resp.getJSONObject("campaigns");
                        JSONObject object=new JSONObject(response);
                        JSONArray jsonArray = object.getJSONArray("campaigns_details");
                        for (int i = 0 ; i < jsonArray.length() ; i++){
                            JSONObject jsonObject =jsonArray.getJSONObject(i);
                            CampaignModel model=new CampaignModel();

                            model.name = jsonObject.getString("campaign_name");
                            model.campid = jsonObject.getString("campaign_id");
                            model.clientid = jsonObject.getString("client_id");
                            model.campstartdate = jsonObject.getString("camp_start_date");
                            model.Campenddate = jsonObject.getString("camp_end_date");
                            model.purpose = jsonObject.getString("campaign_purpose");
                            model.status = jsonObject.getString("status");
                            model.image_link=jsonObject.getString("image_link");

                           /* JSONArray imgArray = object.getJSONArray("campaign_images");
                            for (int y = 0 ; y < imgArray.length() ; y++){
                                JSONObject jsonObject1 =imgArray.getJSONObject(y);
                                model.camp_img_id = jsonObject1.getString("campaign_id");
                                if ( model.camp_img_id.equals(model.campid)){
                                    model.image_link=jsonObject1.getString("image_link");
                                    Log.d("image_link",model.image_link);
                                }
                               }*/
                            models.add(model);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(getActivity());
                    }
                    builder.setTitle("Login Session Expired !")
                            .setMessage("You'r Login session has expired .Do you want to extend the session?")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                    startActivity(new Intent(getActivity(), LoginActivity.class));
                                    sessionManager.setLogin(false);
                                    getActivity().finish();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }

                recyclerView.setAdapter(new RecyclerView.Adapter() {
                    @NonNull
                    @Override
                    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view1 =LayoutInflater.from(getActivity()).inflate(R.layout.homeadapt,parent,false);
                        Holder holder = new Holder(view1);
                        return holder;
                    }

                    @Override
                    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

                        Holder holder1 = (Holder) holder ;
                        final CampaignModel current = models.get(position);

                        holder1.txtName.setText(current.name);
                        holder1.startDate.setText(current.campstartdate);
                        holder1.purpose.setText(current.purpose);

                        Glide.with(getActivity()).load(current.image_link).into(holder1.image);

                        // holder1.image.setImageResource(images[position]);
                        holder1.image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                             Intent intent=new Intent(getActivity(),Campaigndetail.class);
                             intent.putExtra("Cid",current.campid);
                             startActivity(intent);
                            }
                        });

                    }

                    @Override
                    public int getItemCount() {
                        return models.size();
                    }

                    class Holder extends RecyclerView.ViewHolder{
                        TextView txtName , startDate , purpose;
                        ImageView image;
                        public Holder(View itemView) {
                            super(itemView);
                            txtName = itemView.findViewById(R.id.campaignName);
                            startDate =itemView.findViewById(R.id.campStartdate);
                            purpose =itemView.findViewById(R.id.campPurpose);
                            image = itemView.findViewById(R.id.campaignimage);
                        }
                    }

                });

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
