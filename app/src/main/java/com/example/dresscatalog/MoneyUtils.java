package com.example.dresscatalog;

import java.text.NumberFormat;
import java.util.Locale;

public class MoneyUtils {

    private MoneyUtils() {}

    // "38 000 сом"
    public static String formatSom(int value) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("ru", "RU"));
        return nf.format(value) + " сом";
    }
}
