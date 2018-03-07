package com.hq.dely;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import me.relex.circleindicator.CircleIndicator;

/**
 * Created by Korir on 2/16/2018.
 */

public class Home_Frag extends Fragment implements View.OnClickListener, Toolbar.OnMenuItemClickListener{
    ViewPager vphome;
    List<getimages> imagelist;
    CircleIndicator indicator;
    ProgressBar vppbar;
    TextView tvvphone;

    RecyclerView bestrv;
    List<getBestDetails> bestlist;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        tvvphone = (TextView) rootView.findViewById(R.id.tvvphone);
        vphome = (ViewPager) rootView.findViewById(R.id.vphome);
        indicator = (CircleIndicator) rootView.findViewById(R.id.indicator);
        vppbar = (ProgressBar) rootView.findViewById(R.id.vppbar);
        imagelist = new ArrayList<>();
        Toolbar toolbar= (Toolbar) getActivity().findViewById(R.id.toolbar);

        toolbar.setOnMenuItemClickListener(this);

        loadImages();

        tvvphone.setOnClickListener(this);

        //bestsales
        bestlist= new ArrayList<>();
        bestrv = (RecyclerView) rootView.findViewById(R.id.rvbest);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        bestrv.setLayoutManager(layoutManager);
        bestrv.setHasFixedSize(true);
        loadBest();

        return rootView;
    }

    //menu clicked
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.usericon:
                Intent i = new Intent(getActivity(),Login.class);
                getActivity().startActivity(i);
                getActivity().finish();
                break;
            case R.id.logouticon:
                SharedPrefs.getmInstance(getActivity()).userlogout();
                getActivity().recreate();
                break;
            case R.id.settingsicon:
                Toast.makeText(getActivity(), "settings", Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }

    //vpTimer
    public class vpTimer extends TimerTask{
        @Override
        public void run() {
            vphome.post(new Runnable() {
                @Override
                public void run() {
                    vphome.setCurrentItem((vphome.getCurrentItem() + 1) % imagelist.size(), false);
                }
            });
        }
    }

    //implementing onclick
    @Override
    public void onClick(View v) {
        if(v == tvvphone){
            loadImages();
        }
    }

    //Home automatic viewpager
    //vpvolley
    private void loadImages() {
        final String CO_ROOT_URL = "http://192.168.56.1/korirphp/slider.php";
        vppbar.setVisibility(View.VISIBLE);
        StringRequest sRequest = new StringRequest(Request.Method.GET, CO_ROOT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        vppbar.setVisibility(View.INVISIBLE);
                        tvvphone.setVisibility(View.INVISIBLE);
                        try {
                            JSONArray json = new JSONArray(response);
                            for (int i = 0; i < json.length(); i++) {
                                JSONObject jsonObject = json.getJSONObject(i);

                                String imagename = jsonObject.getString("imagename");
                                String image = jsonObject.getString("images");

                                getimages get = new getimages(imagename,image);
                                imagelist.add(get);
                            }
                            VpAdapter vpAdapter = new VpAdapter(imagelist);
                            vphome.setAdapter(vpAdapter);
                            indicator.setViewPager(vphome);

                            Timer timer = new Timer();
                            timer.scheduleAtFixedRate(new vpTimer(),2000,5000);

                            vpAdapter.registerDataSetObserver(indicator.getDataSetObserver());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        vppbar.setVisibility(View.INVISIBLE);
                        tvvphone.setVisibility(View.VISIBLE);
                    }
                });
        Singleton.getmInstance(getActivity()).addToRequestQueue(sRequest);
    }

    //vpAdapter
    class VpAdapter extends PagerAdapter{

        private LayoutInflater layoutinflate;
        private List<getimages> imagelist;


        public VpAdapter(List<getimages> imagelist) {
            this.imagelist = imagelist;
        }

        @Override
        public int getCount() {
            return imagelist.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            final getimages get = imagelist.get(position);

            layoutinflate = (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
            View view = layoutinflate.inflate(R.layout.homevpimage,null);
            ImageView ivhomevp = (ImageView) view.findViewById(R.id.ivhomevp);
            ivhomevp.setScaleType(ImageView.ScaleType.FIT_XY);

            Glide.with(getActivity())
                    .load(get.getImage())
                    .into(ivhomevp);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(position != -1){
                        Toast.makeText(getActivity(),"clicked \t"+ get.getImagename(),Toast.LENGTH_LONG).show();
                }
            }});

            container.addView(view, 0);

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }
    }

    //vpGetter
    class getimages{
        String imagename,image;

        public getimages(String imagename,String image) {
            this.imagename = imagename;
            this.image = image;
        }

        public String getImagename() {
            return imagename;
        }

        public String getImage() {
            return image;
        }
    }

    //Home specials viewpager



    //Home bestsales
    //Volley
    public void loadBest() {
        final String CO_ROOT_URL = "http://192.168.56.1/korirphp/bestsales.php";
//        copbar.setVisibility(View.VISIBLE);

        StringRequest sRequest = new StringRequest(Request.Method.GET, CO_ROOT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        copbar.setVisibility(View.INVISIBLE);
                        try {
                            JSONArray json = new JSONArray(response);
                            for (int i = 0; i < json.length(); i++) {
                                JSONObject jsonObject = json.getJSONObject(i);

                                String bestpatner = jsonObject.getString("partner");
                                String bestimages = jsonObject.getString("best_sales");

                                getBestDetails getbest = new getBestDetails(bestpatner, bestimages);
                                bestlist.add(getbest);
                            }
                            rvAdapter adapter = new rvAdapter(bestlist,getActivity());
                            bestrv.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
        Singleton.getmInstance(getActivity()).addToRequestQueue(sRequest);
    }

    //Adapter
    class rvAdapter extends RecyclerView.Adapter<rvAdapter.rvHolder>{
        private List<getBestDetails> bestlist;
        Context ctx;

        public rvAdapter(List<getBestDetails> bestlist, Context ctx) {
            this.bestlist = bestlist;
            this.ctx = ctx;
        }


        @Override
        public rvHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View itemview = inflater.inflate(R.layout.bestsalecardview,parent,false);

            return new rvHolder(itemview);
        }

        @Override
        public void onBindViewHolder(rvAdapter.rvHolder holder, int position) {
            final getBestDetails getbest = bestlist.get(position);

            Glide.with(getActivity())
                    .load(getbest.getBestimages())
                    .into(holder.ivbest);
            holder.setclicker(new rvListener() {
                @Override
                public void onClick(View view, int position) {
                    Toast.makeText(getActivity(), "clicked"+"\t"+getbest.getPartnername() , Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return bestlist.size();
        }



        //Holder
        class rvHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            ImageView ivbest;
            rvListener click;

            public rvHolder(View itemView) {
                super(itemView);
                ivbest = (ImageView) itemView.findViewById(R.id.ivbest);
                ivbest.setScaleType(ImageView.ScaleType.FIT_XY);

                itemView.setOnClickListener(this);
            }

            public void setclicker(rvListener click){
                this.click =click;
            }

            @Override
            public void onClick(View v) {
                click.onClick(v,getAdapterPosition());
            }
        }
    }

    //getitemcless
    class getBestDetails {
        private String bestpatner,bestimages;

        public getBestDetails(String bestpatner, String bestimages) {
            this.bestpatner = bestpatner;
            this.bestimages= bestimages;
        }

        public String getPartnername() {
            return bestpatner;
        }

        public String getBestimages() {
            return bestimages;
        }
    }
}

