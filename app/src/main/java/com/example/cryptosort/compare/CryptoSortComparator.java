package com.example.cryptosort.compare;

import com.example.cryptosort.model.CryptoModel;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class CryptoSortComparator implements Comparator<CryptoModel> {
    private List<Comparator<CryptoModel>> listComparators;

    @SafeVarargs
    public CryptoSortComparator(Comparator<CryptoModel>... comparators) {
        this.listComparators = Arrays.asList(comparators);
    }

    @Override
    public int compare(CryptoModel crpMoney1, CryptoModel crpMoney2) {
        for (Comparator<CryptoModel> comparator : listComparators) {
            int result = comparator.compare(crpMoney1, crpMoney2);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }
}
