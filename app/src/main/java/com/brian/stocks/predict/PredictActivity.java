package com.brian.stocks.predict;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.brian.stocks.R;
import com.brian.stocks.helper.SharedHelper;
import com.brian.stocks.helper.URLHelper;
import com.brian.stocks.home.HomeActivity;
import com.brian.stocks.predict.adapters.PredictPageAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import net.steamcrafted.loadtoast.LoadToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PredictActivity extends AppCompatActivity {
    private String usdcBalance="0";
    TabLayout tab;
    ViewPager pager;
    TextView predictNow;
    PredictPageAdapter mAdapter;
    LoadToast loadToast;
    private ArrayList all = new ArrayList(), incoming = new ArrayList(), my_post = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predict);

        if(getSupportActionBar() != null){
            // getSupportActionBar().setTitle("Predict");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        loadToast = new LoadToast(this);
        //loadToast.setBackgroundColor(R.color.colorBlack);

        tab = findViewById(R.id.tab);
        pager = findViewById(R.id.view_pager);
        predictNow = findViewById(R.id.btn_post_predict);

        mAdapter = new PredictPageAdapter(getSupportFragmentManager(), all, incoming, my_post);
//        mAdapter.add(new NewPredictsFragment(all));
//        mAdapter.add(new IncomingPredictsFragment(incoming));
//        mAdapter.add(new MyPredictsFragment(my_post));
        pager.setAdapter(mAdapter);
        tab.setupWithViewPager(pager);

        predictNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PredictActivity.this, PredictableListActivity.class);
                startActivity(intent);
            }
        });

        getData();
    }

    private void getData() {
        loadToast.show();
        AndroidNetworking.get(URLHelper.REQUEST_PREDICT)
                .addHeaders("Content-Type", "application/json")
                .addHeaders("accept", "application/json")
                .addHeaders("Authorization", "Bearer " + SharedHelper.getKey(getBaseContext(),"access_token"))
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("response", "" + response);
                        loadToast.success();
                            try {

                                JSONArray all_temp = response.getJSONArray("new_predict");
                                JSONArray incoming_temp = response.getJSONArray("incoming");
                                JSONArray my_post_temp = response.getJSONArray("my_post");

                                for (int i = 0; i < all_temp.length(); i ++) {
                                    try {
                                        all.add(all_temp.getJSONObject(i));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                for (int i = 0; i < incoming_temp.length(); i ++) {
                                    try {
                                        incoming.add(incoming_temp.getJSONObject(i));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                for (int i = 0; i < my_post_temp.length(); i ++) {
                                    try {
                                        my_post.add(my_post_temp.getJSONObject(i));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                mAdapter.notifyDataSetChanged();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                    }

                    @Override
                    public void onError(ANError error) {
                        loadToast.error();
                        // handle error
                        Toast.makeText(getBaseContext(), "Please try again. Network error.", Toast.LENGTH_SHORT).show();
                        Log.d("errorm", "" + error.getMessage());
                    }
                });
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        startActivity(new Intent(PredictActivity.this, HomeActivity.class));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
