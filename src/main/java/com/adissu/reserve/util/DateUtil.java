package com.adissu.reserve.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class DateUtil {

    public static Date getDateWithoutTimeFromToday(int daysToAdd) {
        LocalDate localDate = null;

        if( daysToAdd < 0 ) {
            localDate = LocalDate.now().minusDays(daysToAdd*-1);
        } else {
            localDate = LocalDate.now().plusDays(daysToAdd);
        }

        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static int getCurrentMonth() {
        return LocalDate.now().getMonthValue();
    }

}
