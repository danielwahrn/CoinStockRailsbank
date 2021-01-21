package com.brian.stocks.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.brian.stocks.cash.InfoActivity;
import com.brian.stocks.home.adapters.BankTransactionAdapter;
import com.brian.stocks.cash.CollectCashActivity;
import com.brian.stocks.cash.SendTargetActivity;
import com.brian.stocks.helper.SharedHelper;
import com.brian.stocks.helper.URLHelper;
import com.brian.stocks.model.BankInfo;
import com.brian.stocks.model.BankTransaction;
import com.google.android.material.button.MaterialButton;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.brian.stocks.R;

import net.steamcrafted.loadtoast.LoadToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CashFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    View mView;
    MaterialButton btnSend, btnAdd, btnAddBank, btnConvert;
    ImageButton btnInfo;
    RecyclerView transactionsView;
    Spinner currencySpinner, addCurrencySpinner;
    TextView tvBalance;

    ArrayList<BankInfo> currencies = new ArrayList<>();
    ArrayList<BankTransaction> transactions = new ArrayList<>();

    ArrayList<String> CURRENCIES = new ArrayList<>();

    ArrayAdapter<String> currencyAdapter;
    BankTransactionAdapter transactionAdapter;

    AlertDialog alertDialog;

    private LoadToast loadToast;

    public CashFragment() {
        // Required empty public constructor
    }

    public static CashFragment newInstance() {
        CashFragment fragment = new CashFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_cash, container, false);
        loadToast = new LoadToast(getActivity());

        initComponents();

        String[] ADD_CURRENCIES = {"Add currency", "USD", "EUR", "GBP"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.item_add_currency_popup, ADD_CURRENCIES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        addCurrencySpinner.setAdapter(adapter);
        addCurrencySpinner.setOnItemSelectedListener(this);

        currencyAdapter = new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, CURRENCIES);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(currencyAdapter);
        currencySpinner.setOnItemSelectedListener(this);

        transactionAdapter = new BankTransactionAdapter(transactions);
        transactionsView.setLayoutManager(new LinearLayoutManager(getContext()));
        transactionsView.setAdapter(transactionAdapter);

        initListeners();

        getBankData();

        showAlert();

        return mView;

    }

    private void showAlert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setIcon(R.mipmap.ic_launcher_round)
                .setTitle("Alert")
                .setMessage("Coming soon")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void getBankData() {
        loadToast.show();
        JSONObject jsonObject = new JSONObject();
        if(getContext() != null)
            AndroidNetworking.get(URLHelper.GET_BANK_DETAIL)
                    .addHeaders("Content-Type", "application/json")
                    .addHeaders("accept", "application/json")
                    .addHeaders("Authorization", "Bearer " + SharedHelper.getKey(getContext(),"access_token"))
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("info Edit ProfileGet", "" + response);
                            loadToast.success();

                            currencies.clear();
                            transactions.clear();
                            CURRENCIES.clear();

                            JSONArray currencies_array = response.optJSONArray("currencies");
                            for(int i = 0; i < currencies_array.length(); i ++) {
                                BankInfo bankInfo = new BankInfo();
                                bankInfo.setData(currencies_array.optJSONObject(i));
                                currencies.add(bankInfo);
                                CURRENCIES.add(bankInfo.getCurrency());
                            }

                            currencyAdapter.notifyDataSetChanged();

                            JSONArray transactions_array = response.optJSONArray("transactions");

                            for(int i = 0; i < transactions_array.length(); i ++) {
                                BankTransaction transaction = new BankTransaction();
                                transaction.setData(transactions_array.optJSONObject(i));
                                transactions.add(transaction);
                            }

                            transactionAdapter.notifyDataSetChanged();

                            if(currencies.size() > 0) {
                                currencySpinner.setSelection(0);
                                tvBalance.setText(currencies.get(0).getCurrencySymbol() + " "+currencies.get(0).getBalance());
                            }
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

    private void initComponents() {
        btnAdd = mView.findViewById(R.id.add_money);
        btnSend = mView.findViewById(R.id.send_money);
        btnConvert = mView.findViewById(R.id.convert_money);
        btnInfo = mView.findViewById(R.id.btn_info);
        currencySpinner = mView.findViewById(R.id.currency_spinner);
        addCurrencySpinner = mView.findViewById(R.id.add_currency_spinner);
        tvBalance = mView.findViewById(R.id.balance);

        transactionsView = mView.findViewById(R.id.money_transaction);
    }

    private void initListeners() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currencySpinner.getCount() == 0){
                    Toast.makeText(getContext(), "Please add currency", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(getActivity(), SendTargetActivity.class);
                intent.putExtra("currency", currencySpinner.getSelectedItem().toString());
                intent.putExtra("currency_id", currencies.get(currencySpinner.getSelectedItemPosition()).getCurrencyID());
                startActivity(intent);
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currencySpinner.getCount() == 0){
                    Toast.makeText(getContext(), "Please add currency", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(getActivity(), CollectCashActivity.class);
                intent.putExtra("currency", currencySpinner.getSelectedItem().toString());
                intent.putExtra("currency_id", currencies.get(currencySpinner.getSelectedItemPosition()).getCurrencyID());
                startActivity(intent);
            }
        });

        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), InfoActivity.class));
            }
        });

//        fabAddCurrency.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                View view = getLayoutInflater().inflate(R.layout.add_currency_dialog, null);
//                new AlertDialog.Builder(getActivity())
//                        .setView(view)
//                        .show();
//            }
//        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
        if (parent.getId() == R.id.add_currency_spinner) {
            String currency="";
            if(position == 1) { //USD
                currency = "USD";
            }
            if(position == 2) { //EUR
                currency = "EUR";
            }
            if(position == 3) { //GBP
                currency = "GBP";
            }
            if(position != 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Add currency")
                        .setIcon(R.mipmap.ic_launcher_round)
                        .setMessage("Are you sure you want to add " + currency +"?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendAddCurrency(position);
                    }
                });
                builder.show();
                addCurrencySpinner.setSelection(0);
            }
        }else{
            tvBalance.setText(currencies.get(position).getCurrencySymbol() + " "+currencies.get(position).getBalance());
        }
    }

    private void sendAddCurrency(int currency) {
        loadToast.show();
        JSONObject object = new JSONObject();
        try {
            object.put("currency", currency);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("add currency params", object.toString());
        if(getContext() != null)
            AndroidNetworking.post(URLHelper.REQUEST_ADD_BANK)
                    .addHeaders("Content-Type", "application/json")
                    .addHeaders("accept", "application/json")
                    .addHeaders("Authorization", "Bearer " + SharedHelper.getKey(getContext(),"access_token"))
                    .addJSONObjectBody(object)
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("response", "" + response);
                            loadToast.success();

                            if(response.optBoolean("success")){
                                CURRENCIES.clear();
                                currencies.clear();

                                JSONArray currencies_array = response.optJSONArray("currencies");
                                for(int i = 0; i < currencies_array.length(); i ++) {
                                    BankInfo bankInfo = new BankInfo();
                                    bankInfo.setData(currencies_array.optJSONObject(i));
                                    currencies.add(bankInfo);
                                    CURRENCIES.add(bankInfo.getCurrency());
                                }

                                currencyAdapter.notifyDataSetChanged();
//                                if(currencies.size() == 1) {
//                                    currencySpinner.setSelection(0);
//                                    tvBalance.setText(currencies.get(0).getCurrencySymbol() + " " + currencies.get(0).getBalance());
//                                }
                            }

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

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
