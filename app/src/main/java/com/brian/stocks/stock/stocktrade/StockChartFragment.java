package com.brian.stocks.stock.stocktrade;

import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.brian.stocks.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StockChartFragment extends Fragment {
    private static JSONArray chartData;
    private AnyChartView mStocksChartView;

    public StockChartFragment() {
        // Required empty public constructor
    }

    public static StockChartFragment newInstance(JSONArray data) {
        StockChartFragment fragment = new StockChartFragment();
        chartData = data;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_chart, container, false);
        mStocksChartView = view.findViewById(R.id.stock_history_chart);
        drawStocksChart(chartData);

        return view;
    }

    private void drawStocksChart(JSONArray aggregate) {

        Cartesian cartesian = AnyChart.line();

        cartesian.animation(true)
                .tooltip(false)
                .xAxis(false)
                .yAxis(false)
                .background(false);
        List<DataEntry> seriesData = new ArrayList<>();
        if(aggregate != null) {
            for (int i = 0; i < aggregate.length(); i++) {
                try {
                    JSONObject data = aggregate.getJSONObject(i);
                    String time = data.optString("t");
                    Double vw = data.optDouble("vw");
                    seriesData.add(new StockChartFragment.ChartDataEntry(time, vw));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            Set set = Set.instantiate();
            set.data(seriesData);
            Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");

            Line series1 = cartesian.line(series1Mapping);
            series1.color("#ee204d");
            series1.stroke("3 #ee204d");
            mStocksChartView.setChart(cartesian);
            mStocksChartView.setBackgroundColor(getResources().getColor(R.color.colorPurple));
            mStocksChartView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPurple)));
            mStocksChartView.setForegroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPurple)));
        }
    }

    private class ChartDataEntry extends ValueDataEntry {
        ChartDataEntry(String x, Number value) {
            super(x, value);
        }
    }
}
