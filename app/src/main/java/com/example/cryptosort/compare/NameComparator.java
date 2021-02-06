package com.example.cryptosort.compare;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.cryptosort.model.CryptoModel;

import java.util.Comparator;

public class NameComparator implements Comparator<CryptoModel> {
    @RequiresApi(api = Build.VERSION_CODES.N)

    @Override
    public int compare(CryptoModel name1, CryptoModel name2) {
        return name1.getCurrency().compareTo(name2.getCurrency());
    }
}
