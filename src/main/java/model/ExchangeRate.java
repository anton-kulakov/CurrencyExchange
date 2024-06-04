package model;

public class ExchangeRate {
    int id;
    int baseCurrencyID;
    int targetCurrencyID;
    double rate;

    public ExchangeRate(int id, int baseCurrencyID, int targetCurrencyID, double rate) {
        this.id = id;
        this.baseCurrencyID = baseCurrencyID;
        this.targetCurrencyID = targetCurrencyID;
        this.rate = rate;
    }

    public int getId() {
        return id;
    }

    public int getBaseCurrencyID() {
        return baseCurrencyID;
    }

    public int getTargetCurrencyID() {
        return targetCurrencyID;
    }

    public double getRate() {
        return rate;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBaseCurrencyID(int baseCurrencyID) {
        this.baseCurrencyID = baseCurrencyID;
    }

    public void setTargetCurrencyID(int targetCurrencyID) {
        this.targetCurrencyID = targetCurrencyID;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }
}
