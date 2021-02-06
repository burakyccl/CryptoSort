package com.example.cryptosort.compare;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.cryptosort.model.CryptoModel;

import java.util.Comparator;

public class PriceComparator implements Comparator<CryptoModel> {
    @RequiresApi(api = Build.VERSION_CODES.N)

    @Override
    public int compare(CryptoModel price1, CryptoModel price2) {
        return price1.getPrice().compareTo(price2.getPrice());
    }
}
