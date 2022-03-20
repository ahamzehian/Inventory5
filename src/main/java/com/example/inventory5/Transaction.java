package com.example.inventory5;

public class Transaction {

    private int stockedQty, transactionQty;
    private double unitPrice;
    private String productName, date;

    public Transaction(String productName, int transactionQty) {

        this.productName = productName;
        this.transactionQty = transactionQty;

    }

    public int getStockedQty() {
        return stockedQty;
    }

    public void setStockedQty(int stockedQty) {
        this.stockedQty = stockedQty;
    }

    public int getRemainderQty() {
        return getStockedQty() + transactionQty;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
