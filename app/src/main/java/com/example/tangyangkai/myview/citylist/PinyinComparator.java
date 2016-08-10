package com.example.tangyangkai.myview.citylist;

import java.util.Comparator;

/**
 * Created by kevin on 16/8/10.
 */
public class PinyinComparator implements Comparator<City> {
    @Override
    public int compare(City cityFirst, City citySecond) {
        return cityFirst.getCityPinyin().compareTo(citySecond.getCityPinyin());
    }
}
