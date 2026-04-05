package com.example.btqt1.Model;

public class ConversionRecord {
    public String type;
    public double amount;
    public double result;
    public long timestamp;

    public ConversionRecord(String type, double amount, double result, long timestamp) {
        this.type = type;
        this.amount = amount;
        this.result = result;
        this.timestamp = timestamp;
    }
}