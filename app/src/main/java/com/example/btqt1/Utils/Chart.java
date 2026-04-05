package com.example.btqt1.Utils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
public class Chart {
    public void setupChart(LineChart lineChart) {
        ArrayList<Entry> entries = new ArrayList<>();

        entries.add(new Entry(0, 1900));
        entries.add(new Entry(1, 1950));
        entries.add(new Entry(2, 1930));

        LineDataSet dataSet = new LineDataSet(entries, "Gold Price");
        LineData lineData = new LineData(dataSet);

        lineChart.setData(lineData);
        lineChart.invalidate();
    }
}
