package com.example.inventory5;

public class ProductTableInfo {

    private String index, transaction_date, transaction_qty, value_per_unit, remainder_qty, remainder_value;

    public ProductTableInfo(String index,
                            String transaction_date,
                            String transaction_qty,
                            String value_per_unit,
                            String remainder_qty,
                            String remainder_value) {
        this.index = index;
        this.transaction_date = transaction_date;
        this.transaction_qty = transaction_qty;
        this.value_per_unit = value_per_unit;
        this.remainder_qty = remainder_qty;
        this.remainder_value = remainder_value;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getTransaction_date() {
        return transaction_date;
    }

    public void setTransaction_date(String transaction_date) {
        this.transaction_date = transaction_date;
    }

    public String getTransaction_qty() {
        return transaction_qty;
    }

    public void setTransaction_qty(String transaction_qty) {
        this.transaction_qty = transaction_qty;
    }

    public String getValue_per_unit() {
        return value_per_unit;
    }

    public void setValue_per_unit(String value_per_unit) {
        this.value_per_unit = value_per_unit;
    }

    public String getRemainder_qty() {
        return remainder_qty;
    }

    public void setRemainder_qty(String remainder_qty) {
        this.remainder_qty = remainder_qty;
    }

    public String getRemainder_value() {
        return remainder_value;
    }

    public void setRemainder_value(String remainder_value) {
        this.remainder_value = remainder_value;
    }
}
