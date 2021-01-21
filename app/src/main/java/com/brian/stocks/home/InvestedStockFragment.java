package com.brian.stocks.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.brian.stocks.R;
import com.brian.stocks.stock.StockDepositActivity;
import com.brian.stocks.stock.StocksActivity;
import com.brian.stocks.stock.stocktrade.StocksTradingActivity;
import com.brian.stocks.home.adapters.OpenPositionAdapter;
import com.brian.stocks.helper.SharedHelper;
import com.brian.stocks.helper.URLHelper;
import com.brian.stocks.model.PositionInfo;
import com.brian.stocks.stock.stockwithdraw.StockCoinWithdrawActivity;

import net.steamcrafted.loadtoast.LoadToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InvestedStockFragment extends Fragment {
    private LoadToast loadToast;
    private View rootView;
    private OpenPositionAdapter mAdapter;
    private List<PositionInfo> stocksList = new ArrayList<>();
    private RecyclerView stocksListView;
    private TextView mTotalBalance, mStockBalance, mTextStockProfit, mTextStockMargin, marketStatus;
    Button btnInvest, btnDeposit, btnWithdraw;
    private SwipeRefreshLayout refreshLayout;
    public InvestedStockFragment() {
        // Required empty public constructor
    }
    public static InvestedStockFragment newInstance() {
        InvestedStockFragment fragment = new InvestedStockFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadToast = new LoadToast(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_invested_stock, container, false);
        stocksListView = (RecyclerView) rootView.findViewById(R.id.list_stocks_view);
        mTotalBalance = rootView.findViewById(R.id.total_balance);
        mStockBalance = rootView.findViewById(R.id.stock_balance);
        mTextStockProfit = rootView.findViewById(R.id.stock_profit);
        mTextStockMargin = rootView.findViewById(R.id.margin_balance);
        marketStatus = rootView.findViewById(R.id.market_status);

        btnInvest = rootView.findViewById(R.id.btn_invest);
        btnDeposit = rootView.findViewById(R.id.btn_deposit);
        btnWithdraw = rootView.findViewById(R.id.btn_withdraw);

        btnInvest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), StocksActivity.class));
            }
        });

        btnDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), StockDepositActivity.class));
            }
        });

        btnWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), StockCoinWithdrawActivity.class));
            }
        });

        mAdapter = new OpenPositionAdapter(stocksList, true, getActivity());
        stocksListView.setLayoutManager(new LinearLayoutManager(getContext()));
        stocksListView.setAdapter(mAdapter);

        mAdapter.setListener(new OpenPositionAdapter.Listener() {
            @Override
            public void OnGoToTrade(int position) {
                GoToTrade(position);
            }
        });
        getAllStocks(true);

        refreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        refreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refresh();
                    }
                }
        );

        return rootView;
    }

    private void refresh() {
        refreshLayout.setRefreshing(true);
        getAllStocks(false);
    }

    private void getAllStocks(final boolean loading) {
        if(loading)
            loadToast.show();
        JSONObject jsonObject = new JSONObject();
        String url = URLHelper.GET_STOCK_ORDER_INVESTED;
        Log.d("invest", url);
        if(getContext() != null)
            AndroidNetworking.get(url)
                    .addHeaders("Content-Type", "application/json")
                    .addHeaders("accept", "application/json")
                    .addHeaders("Authorization", "Bearer " + SharedHelper.getKey(getContext(),"access_token"))
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if(loading)
                                loadToast.success();
                            refreshLayout.setRefreshing(false);
                            stocksList.clear();
                            try {
                                mTotalBalance.setText("$ " + response.getString("total_balance"));
                                mStockBalance.setText("$ " + response.getString("stock_balance"));
                                mTextStockProfit.setText("$ " + response.getString("stock_profit"));
                                mTextStockMargin.setText("$ " + response.getString("margin_balance"));
                                marketStatus.setText(response.getString("market_status"));
    
                                if(Double.parseDouble(response.getString("stock_profit"))>0)
                                    mTextStockProfit.setTextColor(getResources().getColor(R.color.green));
                                else mTextStockProfit.setTextColor(getResources().getColor(R.color.colorRedCrayon));
    
                                if(response.getInt("stock_auto_sell") == 1){
                                    showStockAutoSellAlarm();
                                    SharedHelper.putKey(getContext(), "stock_auto_sell", response.getString("stock_auto_sell"));
                                }
    
                                if(response.getInt("stock_auto_sell") == 2){
                                    showAddFundAlarm();
                                }
    
                                if(getContext() != null)
                                SharedHelper.putKey(getContext(), "stock_balance", response.getString("stock_balance"));
    
                                JSONArray stocks = null;
                            
                                stocks = response.getJSONArray("stocks");
                                if(stocks != null)
                                    for(int i = 0; i < stocks.length(); i ++) {
                                        try {
                                            Log.d("stocksinvestitem", stocks.get(i).toString());
                                            stocksList.add(new PositionInfo((JSONObject) stocks.get(i)));
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
                            if(loading)
                                loadToast.error();
                            refreshLayout.setRefreshing(false);
                            // handle error
                            Toast.makeText(getContext(), "Please try again. Network error.", Toast.LENGTH_SHORT).show();
                            Log.d("errorm", "" + error.getMessage());
                        }
                    });
    }

    private void showAddFundAlarm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        builder.setTitle(getContext().getResources().getString(R.string.app_name))
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("Your stock balance is low. Please add funds")
                .setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showStockAutoSellAlarm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        builder.setTitle(getContext().getResources().getString(R.string.app_name))
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("Account liquidation.")
                .setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void GoToTrade(int position) {
        PositionInfo stock = stocksList.get(position);
        Intent intent = new Intent(getActivity(), StocksTradingActivity.class);
        intent.putExtra("stock_name", stock.getSymbol());
        intent.putExtra("stock_price", stock.getCurrentPrice());
        intent.putExtra("stock_shares", stock.getQty());
        intent.putExtra("stock_avg_price", stock.getAvgPrice());
        intent.putExtra("stock_equity", stock.getEquity());
        intent.putExtra("stock_today_change", stock.getChangePrice());
        intent.putExtra("stock_today_change_perc", stock.getChangePricePercent());
        intent.putExtra("type", "invest");
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

//        getAllStocks();
    }
}