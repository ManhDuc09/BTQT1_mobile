package com.example.btqt1;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btqt1.Api.GoldApiService;
import com.example.btqt1.Api.RetrofitClient;
import com.example.btqt1.Model.ConversionRecord;
import com.example.btqt1.Model.GoldData;
import com.example.btqt1.Model.GoldHistoryResponse;
import com.example.btqt1.Model.GoldResponse;
import com.example.btqt1.Model.HistoryItem;
import com.example.btqt1.Model.PriceData;
import com.example.btqt1.Utils.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    Spinner spinnerUnit;

    String[] units = {"Gram", "Luong", "Ounce"};
    EditText etAmount;
    Button btnConvert;
    TextView tvResult;
    LineChart lineChart;
    Spinner spinnerGoldType;
    double goldPerGramVnd = 0;
    double goldPriceUSD = 0; // price per ounce
    TextView tvGoldInfo;
    String[] goldNames = {
            "SJC 9999",
            "DOJI Hanoi",
            "PNJ 24K",
            "World Gold (Ounce)"
    };


    String[] goldCodes = {
            "SJL1L10",
            "DOHNL",
            "PQHN24NTT",
            "XAUUSD"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lineChart = findViewById(R.id.lineChart);
        Chart chartUtil = new Chart();
        chartUtil.setupChart(lineChart);
        spinnerGoldType = findViewById(R.id.spinnerGoldType);




        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                goldNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoldType.setAdapter(adapter);
        etAmount = findViewById(R.id.etAmount);
        tvResult = findViewById(R.id.tvResult);
        lineChart = findViewById(R.id.lineChart);
        btnConvert = findViewById(R.id.btnConvert);
        tvGoldInfo = findViewById(R.id.tvGoldInfo);
        spinnerUnit = findViewById(R.id.spinnerUnit);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                units
        );
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);
        Button btnHistory = findViewById(R.id.btnHistory);
        btnHistory.setOnClickListener(v -> showHistoryPopup());

        btnConvert.setOnClickListener(v -> {
            int position = spinnerGoldType.getSelectedItemPosition();
            String selectedType = goldCodes[position];

            fetchGoldPrice(selectedType);
            fetchGoldHistory(selectedType);
        });


        spinnerGoldType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String type = goldCodes[position];
                fetchGoldHistory(type);

                fetchGoldPrice(type);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                calculate(); // ✅ no API call
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void fetchGoldPrice(String type) {

        GoldApiService service = RetrofitClient.getApiService();

        Log.d("SPINNER", "Selected type: " + type);
        Call<GoldResponse> call = service.getGoldPrice(type);

        Log.d("API", "REQUEST SENT");

        call.enqueue(new Callback<GoldResponse>() {
            @Override
            public void onResponse(Call<GoldResponse> call, Response<GoldResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    GoldResponse gold = response.body();
                    String changeSymbol = gold.change_buy > 0 ? "↑" :
                            gold.change_buy < 0 ? "↓" : "-";

                    String info = gold.name + "\n"
                            + "Buy: " + String.format("%,d", (long) gold.buy)
                            + " (" + changeSymbol + String.format("%,d", (long) gold.change_buy) + ")\n"
                            + "Sell: " + String.format("%,d", (long) gold.sell);

                    info += "\nUpdated: " + gold.time + " " + gold.date;
                    tvGoldInfo.setText(info);



                    if (type.equals("XAUUSD")) {
                        goldPriceUSD = gold.buy;
                    } else {
                        goldPerGramVnd = gold.buy / 37.5;
                    }

                    calculate();

                } else {
                    tvResult.setText("API error");
                }
            }

            @Override
            public void onFailure(Call<GoldResponse> call, Throwable t) {
                Log.e("API", "ERROR: " + t.getMessage());
                tvResult.setText("Failed: " + t.getMessage());
            }
        });
    }


    private double convertGoldToVND(double priceUSDPerOunce, double amountGram) {
        double ounceToGram = 31.1035;
        double usdToVnd = 24000;

        double pricePerGramUSD = priceUSDPerOunce / ounceToGram;

        return pricePerGramUSD * amountGram * usdToVnd;
    }

    private void calculate() {
        String input = etAmount.getText().toString();

        if (input.isEmpty()) return;

        double amount = Double.parseDouble(input);
        String unit = units[spinnerUnit.getSelectedItemPosition()];


        if (unit.equals("Luong")) {
            amount = amount * 37.5;
        } else if (unit.equals("Ounce")) {
            amount = amount * 31.1035;
        }
        String selectedType = goldCodes[spinnerGoldType.getSelectedItemPosition()];

        double result;

        if (selectedType.equals("XAUUSD")) {

            if (goldPriceUSD == 0) {
                tvResult.setText("Waiting for price...");
                return;
            }

            double usdToVnd = 24000;
            result = convertGoldToVND(goldPriceUSD, amount);

        } else {

            if (goldPerGramVnd == 0) return;

            result = amount * goldPerGramVnd;
        }

        tvResult.setText(String.format("%,d VND", (long) result));
        saveHistory(selectedType, amount, result);
    }

    private void fetchGoldHistory(String type) {

        GoldApiService service = RetrofitClient.getApiService();

        service.getGoldHistory(type, 7).enqueue(new Callback<GoldHistoryResponse>() {
            @Override
            public void onResponse(Call<GoldHistoryResponse> call, Response<GoldHistoryResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    List<HistoryItem> list = response.body().history;

                    if (list == null || list.isEmpty()) return;

                    drawChart(list, type);

                } else {
                    Log.e("CHART", "No data");
                }
            }

            @Override
            public void onFailure(Call<GoldHistoryResponse> call, Throwable t) {
                Log.e("CHART", "Error: " + t.getMessage());
            }
        });
    }
    private void saveHistory(String type, double amount, double result) {
        SharedPreferences prefs = getSharedPreferences("gold_history", MODE_PRIVATE);
        String json = prefs.getString("history", "[]"); // default empty array

        Gson gson = new Gson();
        List<ConversionRecord> list = new ArrayList<>();

        // Load existing history
        ConversionRecord[] oldRecords = gson.fromJson(json, ConversionRecord[].class);
        if (oldRecords != null) {
            for (ConversionRecord r : oldRecords) list.add(r);
        }

        // Add new record
        list.add(new ConversionRecord(type, amount, result, System.currentTimeMillis()));

        // Save back
        String newJson = gson.toJson(list);
        prefs.edit().putString("history", newJson).apply();
    }
    private void drawChart(List<HistoryItem> list, String type) {

        List<Entry> entries = new ArrayList<>();
        List<String> dates = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {

            HistoryItem item = list.get(i);

            PriceData priceData = item.prices.get(type);
            if (priceData == null) continue;

            float price;

            if (type.equals("XAUUSD")) {
                price = (float)(priceData.buy * 24000);
            } else {
                price = (float)(priceData.buy / 37.5);
            }

            entries.add(new Entry(i, price));
            dates.add(item.date);
        }

        LineDataSet dataSet = new LineDataSet(entries, "Gold Price");
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);

        lineChart.clear();
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(-45);

        lineChart.invalidate();
    }

    private void showHistoryPopup() {
        SharedPreferences prefs = getSharedPreferences("gold_history", MODE_PRIVATE);
        String json = prefs.getString("history", "[]");
        Gson gson = new Gson();
        ConversionRecord[] recordsArray = gson.fromJson(json, ConversionRecord[].class);

        if (recordsArray == null || recordsArray.length == 0) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("History")
                    .setMessage("No conversion history yet.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        List<ConversionRecord> records = java.util.Arrays.asList(recordsArray);

        View popupView = getLayoutInflater().inflate(R.layout.history_popup, null);
        RecyclerView rvHistory = popupView.findViewById(R.id.rvHistory);
        rvHistory.setAdapter(new HistoryAdapter(records));
        rvHistory.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(popupView)
                .setPositiveButton("Close", null)
                .show();
    }
}