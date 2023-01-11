package com.adissu.reserve.util;

import com.adissu.reserve.constants.AdminConfigConstants;
import com.adissu.reserve.entity.AdminConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReserveUtil {

    /*private Set<Integer> availableDurationsInMinutes;
    private static final int DAY_STARTING_AT = 8;
    private static final int DAY_ENDING_AT = 18;*/
    private final List<AdminConfig> adminConfigList;

    private int getFirstHour() {
        return Integer.parseInt(adminConfigList.stream()
                .filter(adminConfig -> adminConfig.getName().equals(AdminConfigConstants.FIRST_HOUR))
                .map(AdminConfig::getValue)
                .findFirst()
                .get());
    }

    private int getLastHour() {
        return Integer.parseInt(adminConfigList.stream()
                .filter(adminConfig -> adminConfig.getName().equals(AdminConfigConstants.LAST_HOUR))
                .map(AdminConfig::getValue)
                .findFirst()
                .get());
    }

    @Deprecated
    private static int compareTimeStrings(String time1, String time2) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date date1;
        Date date2;
        try {
            date1 = sdf.parse(time1);
            date2 = sdf.parse(time2);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return date1.compareTo(date2);
    }

    public List<String> getFreeDay() {
        List<String> freeDay = new ArrayList<>();
        String time = "";
        final int DAY_STARTING_AT = getFirstHour();
        final int DAY_ENDING_AT = getLastHour();

        for( int i = DAY_STARTING_AT; i < DAY_ENDING_AT; i++ ) {
            if( i < 10 ) {
                time = "0" + i;
            } else {
                time = String.valueOf(i);
            }
            freeDay.add(time + ":00");
            freeDay.add(time + ":15");
            freeDay.add(time + ":30");
            freeDay.add(time + ":45");
        }

        freeDay.add(DAY_ENDING_AT + ":00");

        return freeDay;
    }

    private void getRemainingFreeDay(String hourFrom, int durationInMinutes, List<String> day, Set<Integer> availableDurationsInMinutes) {

        System.out.println("Remaining from day:");
        for( String h : day ) {
            System.out.print(h + " ; ");
        }

        int indexFrom = day.stream().filter(s -> s.contains(hourFrom)).findFirst().map(day::indexOf).orElse(-1);
        System.out.println("--------indexFrom BEGIN = " + indexFrom);
        System.out.println("--------hourFrom = " + hourFrom);

        // marchez intervale ca nefolosibile pentru fiecare rezervare (nr de intervale e calculat: (duration/15)-1). Marcajul va fi: "HH:mm" + "-durationInMinutes"
        for( int duration : availableDurationsInMinutes ) {
            int interval = duration/15-1;
            System.out.println("--------interval = " + interval);
            int indexTo = indexFrom - interval;
            if( indexFrom - interval < 0 ) {
                indexTo = 0;
            }

            for( int i = indexFrom; i >= indexTo; i-- ) {
//                if (!day.get(i).contains("-" + duration)) {
                if( !(day.get(i).contains("-" + duration + "-") || day.get(i).endsWith("-" + duration)) ) {
                    String markedDuration = day.get(i) + "-" + duration;
                    day.set(i, markedDuration);
                }
            }
        }

        // if reservation is made for the last working hour, then we will just remove it from the available hours and return the new list.
        if( hourFrom.equals(day.get(day.size()-1)) ) {
            day.remove(hourFrom);
            return;
        }

        int intervalsToDelete = durationInMinutes/15;
        if( indexFrom + intervalsToDelete > day.size() - 1 ) {
            intervalsToDelete = day.size() - indexFrom;
            System.out.println("nr of intervals new = " + (day.size() - indexFrom));
        }

        while( intervalsToDelete > 0 ) {
            System.out.println("intervalsToDelete = " + intervalsToDelete);
            System.out.println("day.get(indexFrom) = " + day.get(indexFrom));
            day.remove(indexFrom);
            intervalsToDelete--;
        }
    }

    private List<String> getFreeHoursForDay(HashMap<String, Integer> occupiedHoursWithDuration, Set<Integer> availableDurationsInMinutes) {
        List<String> currentDay = getFreeDay();
        occupiedHoursWithDuration.forEach((hourFrom, durationInMinutes) -> getRemainingFreeDay(hourFrom, durationInMinutes, currentDay, availableDurationsInMinutes));

        log.info("getFreeHoursForDay - looping through currentDay:");
        currentDay.forEach(hour -> log.info("Hour = {}", hour));

        return currentDay;
    }

    public List<String> getFree(HashMap<String, Integer> occupiedHoursWithDuration, int selectedProductDurationInMinutes, Set<Integer> availableDurationsInMinutes) {
        List<String> availableTime = getFreeHoursForDay(occupiedHoursWithDuration, availableDurationsInMinutes);
        availableTime.removeIf(freeHour -> freeHour.contains("-" + selectedProductDurationInMinutes + "-") || freeHour.endsWith("-" + selectedProductDurationInMinutes));

        return availableTime;
    }

}
