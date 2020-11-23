package com.brian.stocks.stock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.brian.stocks.R;
import com.brian.stocks.adapters.BottomCoinAdapter;
import com.brian.stocks.stock.adapter.StockTransferAdapter;
import com.brian.stocks.helper.SharedHelper;
import com.brian.stocks.helper.URLHelper;
import com.brian.stocks.model.CoinInfo;
import com.brian.stocks.model.TransferInfo;
import com.squareup.picasso.Picasso;

import net.steamcrafted.loadtoast.LoadToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Coin2StockFragment extends Fragment {
    private static String CoinSymbol, CoinBalance="0.0", CoinUsdc="0.0", CoinRate, CoinIcon;
    private LoadToast loadToast;
    TextView mCoinBalance, mCoinUsdc, mCoinSymbol, mStockBalance;
    ImageView mCoinIcon;
    EditText mEditAmount;
    Button mBtnTransfer;
    CheckBox mChkMargin;
    RecyclerView mTransferListView;
    StockTransferAdapter mTransferAdapter;
    private List<TransferInfo> stocksList = new ArrayList<>();
    View mView;
    BottomCoinAdapter mAdapter;
    private RecyclerView recyclerView;
    private BottomSheetDialog dialog;

    private String CoinId;
    String stockBalance, coinBalance, coinUSD;

    private List<CoinInfo> coinList = new ArrayList<>();
    

    public Coin2StockFragment() {
    }
    public static Coin2StockFragment newInstance(String symbol, String balance, String usdc, String rate, String icon) {
        Coin2StockFragment fragment = new Coin2StockFragment();
        CoinSymbol = symbol;
        CoinBalance = balance;
        CoinUsdc = usdc;
        CoinRate = rate;
        CoinIcon = icon;
        return fragment;
    }

    public static Coin2StockFragment newInstance(String stockBalance, String coinBalance, String coinUSD, List<TransferInfo> coinStocksList){
        Coin2StockFragment fragment = new Coin2StockFragment();
        fragment.stockBalance = stockBalance;
        fragment.coinBalance = coinBalance;
        fragment.coinUSD = coinUSD;
        fragment.stocksList = coinStocksList;
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
        mView = inflater.inflate(R.layout.fragment_coin2_stock, container, false);

        initComponents();

        initListeners();

//        mCoinBalance.setText(CoinBalance);
//        mCoinSymbol.setText(CoinSymbol);
//        mCoinUsdc.setText("( $ "+CoinUsdc+" )");

//        Picasso.with(getContext())
//                .load(CoinIcon)
//                .placeholder(R.drawable.coin_bitcoin)
//                .error(R.drawable.coin_bitcoin)
//                .into(mCoinIcon);

        mCoinBalance.setText(coinBalance);
        mCoinUsdc.setText("($ " + coinUSD+")");
        mStockBalance.setText("$ "+stockBalance);

        mTransferAdapter = new StockTransferAdapter(stocksList, true);
        mTransferListView.setLayoutManager(new LinearLayoutManager(getContext()));
        mTransferListView.setAdapter(mTransferAdapter);

       return mView;

    }

    private void initListeners() {

//        mCoinIcon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                getCoinAssets();
//            }
//        });

        mBtnTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mEditAmount.getText().toString().equals("")) {
                    mEditAmount.setError("!");
                    return;
                }
                if(Double.parseDouble(mEditAmount.getText().toString()) > Double.parseDouble(coinBalance)) {
                    Toast.makeText(getContext(), "Insufficient balance", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(mChkMargin.isChecked())
                    showMarginConfirmAlertDialog();
                else
                    showTransferConfirmAlertDialog();
            }
        });

        mAdapter.setListener(new BottomCoinAdapter.Listener() {
            @Override
            public void onSelectCoin(int position) {
                CoinInfo coin = coinList.get(position);
                CoinId = coin.getCoinId();
                CoinSymbol = coin.getCoinSymbol();
                CoinBalance = coin.getCoinBalance();
                CoinUsdc = coin.getCoinUsdc();
                mCoinBalance.setText(CoinBalance + " " + CoinSymbol);
                mCoinSymbol.setText(CoinSymbol);
                mCoinUsdc.setText("( $ "+CoinUsdc+" )");
                Picasso.with(getActivity())
                        .load(coin.getCoinIcon())
                        .placeholder(R.drawable.coin_bitcoin)
                        .error(R.drawable.coin_bitcoin)
                        .into(mCoinIcon);
                dialog.dismiss();
            }
        });
    }

    private void initComponents() {
        mCoinBalance = mView.findViewById(R.id.coin_balance);
        mCoinSymbol = mView.findViewById(R.id.coin_symbol);
        mCoinUsdc = mView.findViewById(R.id.coin_amount);
        mStockBalance = mView.findViewById(R.id.stock_balance);
        mEditAmount = mView.findViewById(R.id.edit_transfer_amount);
        mBtnTransfer = mView.findViewById(R.id.btn_transfer_funds);
        mCoinIcon = mView.findViewById(R.id.coin_icon);
        mTransferListView = mView.findViewById(R.id.list_transfer_view);
        mChkMargin = mView.findViewById(R.id.chk_margin);

        View dialogView = getLayoutInflater().inflate(R.layout.coins_bottom_sheet, null);
        dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(dialogView);

        recyclerView = dialogView.findViewById(R.id.bottom_coins_list);
        mAdapter  = new BottomCoinAdapter(coinList, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);
    }

    private void showTransferConfirmAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        builder.setTitle(getContext().getResources().getString(R.string.app_name))
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("Are you sure transfer $ " + mEditAmount.getText()+"?")
                .setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onTransferFunds();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showMarginConfirmAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        builder.setTitle(getContext().getResources().getString(R.string.app_name))
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("Do you want to transfer $ " + mEditAmount.getText()+" into your margin account?")
                .setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showTransferConfirmAlertDialog();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void onTransferFunds() {
        loadToast.show();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("amount", mEditAmount.getText());
            jsonObject.put("type", 0);
            jsonObject.put("coin", "USDC");
            jsonObject.put("rate", 1);
            jsonObject.put("check_margin", mChkMargin.isChecked());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(getContext() != null)
            AndroidNetworking.post(URLHelper.REQUEST_DEPOSIT_STOCK)
                    .addHeaders("Content-Type", "application/json")
                    .addHeaders("accept", "application/json")
                    .addHeaders("Authorization", "Bearer " + SharedHelper.getKey(getContext(),"access_token"))
                    .addJSONObjectBody(jsonObject)
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("response", "" + response);
                            loadToast.success();

                            stocksList.clear();
                            if(response.optBoolean("success")) {
                                JSONArray stocks = null;
                                try {
                                    stocks = response.getJSONArray("stock_transfer");
                                    for (int i = 0; i < stocks.length(); i++) {
                                        try {
                                            Log.d("transferitem", stocks.get(i).toString());
                                            stocksList.add(new TransferInfo((JSONObject) stocks.get(i)));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    mTransferAdapter.notifyDataSetChanged();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


//                            CoinUsdc = BigDecimalDouble.newInstance().sub(CoinUsdc, mEditAmount.getText().toString());

                                try {
                                    mStockBalance.setText("$ " + response.getString("stock_balance"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    mCoinBalance.setText("$ " + response.getString("usdc_balance"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    mCoinUsdc.setText("$ " + response.getString("usdc_est_usd"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            }

                            if(getContext() != null)
                            Toast.makeText(getContext(), response.optString("message"), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(ANError error) {
                            loadToast.error();
                            // handle error
                            Toast.makeText(getContext(), "Please try again. Network error.", Toast.LENGTH_SHORT).show();
                            Log.d("errorm", "" + error.getMessage());
                        }
                    });
    }

    private void getCoinAssets() {
        loadToast.show();

        if(getContext() != null)
            AndroidNetworking.get(URLHelper.GET_SEND_COIN_ASSETS)
                    .addHeaders("Content-Type", "application/json")
                    .addHeaders("accept", "application/json")
                    .addHeaders("Authorization", "Bearer " + SharedHelper.getKey(getContext(),"access_token"))
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("coin assets response", "" + response);
                            loadToast.success();

                            coinList.clear();

                            JSONArray coins = response.optJSONArray("assets");

                            for(int i = 0; i < coins.length(); i ++) {
                                try {
                                    Log.d("coinitem", coins.get(i).toString());
                                    coinList.add(new CoinInfo((JSONObject) coins.get(i)));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            mAdapter.notifyDataSetChanged();
                            dialog.show();
                        }

                        @Override
                        public void onError(ANError error) {
                            loadToast.error();
                            // handle error
                            Toast.makeText(getContext(), "Please try again. Network error.", Toast.LENGTH_SHORT).show();
                            Log.d("errorm", "" + error.getMessage());
                        }
                    });
    }
}
