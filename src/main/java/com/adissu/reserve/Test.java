package com.adissu.reserve;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;

public class Test {

    public static void main(String[] args) {
        /*RandomString randomString = new RandomString(30);
        System.out.println(randomString.nextString());

        List<String> interval = Arrays.asList("10:00-12:00", "12:00-12:15", "16:00-16:15", "16:30-18:30");
        List<String> freeTime = ReserveUtil.getFreeTimeIntervals(null);

        for( String s : freeTime ) {
            System.out.println("s = " + s);
        }*/

//        List<String> day = ReserveUtil.getFreeDay(8, 18);

        /*for (String d : day) {
            System.out.println("d = " + d);
        }

        int nrOfIntv = 120/15;

        // tratam diferit 18:00. pur si simplu il stergem din lista, indiferent de tipul de rezervare
        int indexFrom = day.indexOf("18:00");
        System.out.println("nrOfIntv = " + nrOfIntv);
        System.out.println("day.size()-1 = " + (day.size()-1));
        System.out.println("indexFrom = " + indexFrom);

        // formula interval daca suntem peste limita.
        System.out.println("time = " + (day.size()-1 - indexFrom+1)); // lastIndex - currIndex + currIntv

        System.out.println("nr of intervals old = " + nrOfIntv);
        if( indexFrom + nrOfIntv > day.size() - 1 ) {
            System.out.println("nr of intervals new = " + (day.size() - indexFrom - 1));
            System.out.println("to index: " + (day.size() - 1));
        }*/



        /*for (String ss : ReserveUtil.getRemainingFreeDay("17:30", 120, day, null)) {
            System.out.println("ss = " + ss);
        }*/

        HashMap<String, Integer> pair = new HashMap<>();
        pair.put("09:00", 60);
        pair.put("12:00", 60);
        pair.put("15:00", 120);
        pair.put("17:00", 15);
        pair.put("17:30", 15);
        pair.put("18:00", 120);


        /*ReserveUtil reserveUtil = new ReserveUtil();
        reserveUtil.setAvailableDurationsInMinutes(Set.of(15, 120, 60));

        List<String> free = reserveUtil.getFree(pair, 60);
        for (String freeTime : free) {
            System.out.println("freeTime = " + freeTime.substring(0, 5));
        }*/

        LocalDate localDate = LocalDate.now().plusDays(2);
        LocalDate localDate1 = LocalDate.now().plusDays(3);
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date date1 = Date.from(localDate1.atStartOfDay(ZoneId.systemDefault()).toInstant());

        System.out.println("localDate = " + localDate);
        System.out.println("date = " + date);
        System.out.println("date compare = " + (date.equals(date1)));
        System.out.println("date1 >= date2 ? " + (date1.after(date)));

    }
}
