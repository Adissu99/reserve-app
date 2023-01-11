package com.adissu.reserve.util;

public class ProductUtil {

    public static int formatDurationFromAdminInput(String duration) {
        return (Integer.parseInt(duration) + 1) * 15;
    }
}
