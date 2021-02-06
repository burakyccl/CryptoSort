package com.example.cryptosort.model;

import com.google.gson.annotations.SerializedName;

public class CryptoModel {
    @SerializedName("id")
    public String id;
    @SerializedName("currency")
    public String currency;
    @SerializedName("price")
    public String price;
    @SerializedName("name")
    public String name;
    @SerializedName("logo_url")
    public String logo_url;

    public String favStatus = "0";

    public String getCurrency() { return currency; }

    public String getFavStatus() {
        return favStatus;
    }

    public void setFavStatus(String favStatus) {
        this.favStatus = favStatus;
    }


    public String getId() {
        return id;
    }

    public String getPrice() {
        return price;
    }
}
