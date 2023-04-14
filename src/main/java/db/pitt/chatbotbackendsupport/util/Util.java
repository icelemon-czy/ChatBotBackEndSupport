package db.pitt.chatbotbackendsupport.util;

import db.pitt.chatbotbackendsupport.constant.Constant;
import db.pitt.chatbotbackendsupport.entity.Time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Util {
    /**
     * Number can be in two format
     * 1. [Hyphenated] word
     * twenty-five  (25)
     * O-five       (5)
     * five         (5)
     *
     * 2. Number
     * 25
     * 5
     * The function try to extract number if the input following the format we assume
     * Return null if the function cannot recognize the number
     */
    public static Integer extractInt(String word){
        if(word.contains("-")){
            String[] letters= word.split("-");
            if(letters.length>2){
                return null;
            }else {
                // Extract the first number zero or o or twenty.... fifty
                Integer firstNumber = Constant.linkWordToInt.get(letters[0].toLowerCase());
                // Extract second number
                Integer secondNumber = Constant.wordToInt.get(letters[1].toLowerCase());
                if(firstNumber != null && secondNumber != null && secondNumber<=9){
                    return firstNumber + secondNumber;
                }
                return null;
            }
        }else{
            return extractIntAtomic(word);
        }
    }
    public static Integer extractIntAtomic(String letter){
        // check whether is letter format (two) or number format (2)
        if(letter.length()>0 && letter.charAt(0)>='0' && letter.charAt(0) <='9'){
            try{
                return Integer.parseInt(letter);
            }catch (NumberFormatException e){
                return null;
            }
        }else {
            return Constant.wordToInt.get(letter);
        }
    }

    /**
     * Split number with character.
     * For example 3pm -> 3 pm
     * Besides, we will do some cleaning job
     * 1. Convert the sentence into lowercase
     * 2. Separate PM AM   threePM -> three pm ?????
     */
    public static String[] splitByNumber(String sentence){
        sentence = sentence.toLowerCase();
        String[] dirty = sentence.split(" ");
        List<String> cleanList = new ArrayList<>();
        for(String dirtyword : dirty){
            StringBuilder sb = new StringBuilder();
            Boolean lastCisNumber = null;
            for(char c: dirtyword.toCharArray()){
                if(isNum(c)){
                    if(lastCisNumber == null){
                        lastCisNumber = true;
                        sb.append(c);
                        continue;
                    }
                    if(lastCisNumber){
                        sb.append(c);
                    }else{
                        lastCisNumber = true;
                        cleanList.add(sb.toString());
                        sb = new StringBuilder();
                        sb.append(c);
                    }
                }else{
                    if(lastCisNumber == null){
                        lastCisNumber = false;
                        sb.append(c);
                        continue;
                    }
                    if(!lastCisNumber){
                        sb.append(c);
                    }else{
                        lastCisNumber = false;
                        cleanList.add(sb.toString());
                        sb = new StringBuilder();
                        sb.append(c);
                    }
                }
            }
            if(sb.length() >0){
                cleanList.add(sb.toString());
            }
        }
        return cleanList.toArray(new String[cleanList.size()]);
    }

    public static boolean isNum (char c){
        if(c>='0' && c<='9'){
            return true;
        }else{
            return false;
        }
    }


    /**
     * The 12-hour clock is a time convention in which the 24 hours of the day are divided into two periods: a.m. and p.m.
     * Each period consists of 12 hours numbered: 12 (acting as 0), 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 and 11.
     * @param hour
     * This method will check whether hour is valid in 12 hour clock system
     */
    public static boolean isValid12HourTimeSystem(int hour){
        if(hour > 0 && hour <13){
            return true;
        }
        return false;
    }

    public static boolean isValidHour(int hour){
        if(hour<0 || hour>23){
            return false;
        }
        return true;
    }

    public static boolean isValidMinute(int minute){
        if(minute<0 || minute>59){
            return false;
        }
        return true;
    }


    /**
     * Two cases
     * 1. No Ambiguity Time
     *      Calculate the new Time
     *          If new time is in tomorrow, then throw it away
     *          Otherwise, return new time
     * 2. Ambiguity Time
     *      Calculate two possible new Time
     *          If one of the new time is in tomorrow, then throw it away and choose the other one
     *           Otherwise, return two possible time
     */
    public static Time addTime(Time time, Time duration){
        Time newTime = new Time(3);
        long offset = duration.durationMin * 60 * 1000 + duration.durationHour * 60 * 60 * 1000;
        // Dates to be parsed
        String time1 = 1 + " " + time.hour + ":" + time.minute;

        // Creating a SimpleDateFormat object
        // to parse time in the format dd HH:MM
        /**
         * Set Default Day to Day 1
         * Why? we do want the dest or arrival day to be next day.
         */
        SimpleDateFormat simpleDateFormat
                = new SimpleDateFormat("dd HH:mm");

        Date date1;

        // Parsing the Time Period
        try {
            date1 = simpleDateFormat.parse(time1);
        } catch (ParseException e) {
            System.out.println("Date is not correct");
            return null;
        }

        long newTimeMS = offset + date1.getTime();

        String[] newDate = simpleDateFormat.format(new Date(newTimeMS)).split(" ");
        int newDay = Integer.parseInt(newDate[0]);

        if(time.flag == 3) {
            if (newDay != 1) {
                return null;
            }
            String[] hhMM = newDate[1].split(":");
            int newHour = Integer.parseInt(hhMM[0]);
            int newMin = Integer.parseInt(hhMM[1]);
            newTime.hour = newHour;
            newTime.minute = newMin;
        }
        else{
            /**
             * Remember hour:minute comes after hour2:minute2
             */
            newTime.flag = 2;
            if (newDay != 1) {
                newTime.flag = 3;
            }else{
                String[] hhMM = newDate[1].split(":");
                int newHour = Integer.parseInt(hhMM[0]);
                int newMin = Integer.parseInt(hhMM[1]);
                newTime.hour = newHour;
                newTime.minute = newMin;
            }
            /**
             * Calculate the second possible time
             */
            // Dates to be parsed
            String time2 = 1 + " " + time.hour2 + ":" + time.minute2;

            // Creating a SimpleDateFormat object
            // to parse time in the format dd HH:MM

            Date date2;

            // Parsing the Time Period
            try {
                date2 = simpleDateFormat.parse(time2);
            } catch (ParseException e) {
                // System.out.println(time.hour2 +" ----- " +time.minute2);
                System.out.println("Second Date is not correct");
                return null;
            }

            long newTime2MS = offset + date2.getTime();

            String[] newDate2 = simpleDateFormat.format(new Date(newTime2MS)).split(" ");
            int newDay2 = Integer.parseInt(newDate2[0]);

            if (newDay2 != 1) {
                return null;
            }

            String[] hhMM = newDate2[1].split(":");

            int newHour = Integer.parseInt(hhMM[0]);
            int newMin = Integer.parseInt(hhMM[1]);
            newTime.hour2 = newHour;
            newTime.minute2 = newMin;

            if(newTime.flag == 3){
                newTime.hour = newTime.hour2;
                newTime.minute = newTime.minute2;
            }
        }
        return newTime;
    }

    /**
     * If the calculated time is not the same day as fixed time,
     * Throw it away.
     */
    public static Time subTime(Time time, Time duration){
        Time newTime = new Time(3);
        long offset = duration.durationMin * 60 * 1000 + duration.durationHour * 60 * 60 * 1000;
        // Dates to be parsed
        String time1 = 2 + " " + time.hour + ":" + time.minute;

        // Creating a SimpleDateFormat object
        // to parse time in the format dd HH:MM
        /**
         * Set Default Day to Day 2
         * Why? we do want the dest or arrival day to be next day.
         */
        SimpleDateFormat simpleDateFormat
                = new SimpleDateFormat("dd HH:mm");

        Date date1;

        // Parsing the Time Period
        try {
            date1 = simpleDateFormat.parse(time1);
        } catch (ParseException e) {
            System.out.println("Date is not correct");
            return null;
        }

        long newTimeMS = date1.getTime() - offset;

        String[] newDate = simpleDateFormat.format(new Date(newTimeMS)).split(" ");
        int newDay = Integer.parseInt(newDate[0]);

        if(time.flag == 3) {
            if (newDay != 2) {
                return null;
            }
            String[] hhMM = newDate[1].split(":");
            int newHour = Integer.parseInt(hhMM[0]);
            int newMin = Integer.parseInt(hhMM[1]);
            newTime.hour = newHour;
            newTime.minute = newMin;
        }
        else{
            /**
             * Remember hour:minute comes after hour2:minute2
             */
            newTime.flag = 2;
            if (newDay != 2) {
                return null;
            }else{
                String[] hhMM = newDate[1].split(":");
                int newHour = Integer.parseInt(hhMM[0]);
                int newMin = Integer.parseInt(hhMM[1]);
                newTime.hour = newHour;
                newTime.minute = newMin;
            }
            /**
             * Calculate the second possible time
             */
            // Dates to be parsed
            String time2 = 2 + " " + time.hour2 + ":" + time.minute2;

            // Creating a SimpleDateFormat object
            // to parse time in the format dd HH:MM

            Date date2;

            // Parsing the Time Period
            try {
                date2 = simpleDateFormat.parse(time2);
            } catch (ParseException e) {
                // System.out.println(time.hour2 +" ----- " +time.minute2);
                System.out.println("Second Date is not correct");
                return null;
            }

            long newTime2MS = date2.getTime() - offset;

            String[] newDate2 = simpleDateFormat.format(new Date(newTime2MS)).split(" ");
            int newDay2 = Integer.parseInt(newDate2[0]);

            if (newDay2 != 2) {
                newTime.flag = 3;
                return newTime;
            }

            String[] hhMM = newDate2[1].split(":");

            int newHour = Integer.parseInt(hhMM[0]);
            int newMin = Integer.parseInt(hhMM[1]);
            newTime.hour2 = newHour;
            newTime.minute2 = newMin;
        }
        return newTime;
    }


    /**
     * Get Current Time
     */
    public static Time getNow(){
        Time now = new Time(3);
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        String[] currentTime = formatter.format(date).split(":");
        now.hour = Integer.parseInt(currentTime[0]);
        now.minute = Integer.parseInt(currentTime[1]);
        return now;
    }

    /**
     * Based on AM or PM Flag or Intention
     * transform hour from 12 clock system to 24 clock system
     * For example 1pm -> 13
     */
    public static int transform12To24(int hour,int ampmFlag){
        if(ampmFlag == 1){
            // am
            if(hour == 12){
                hour = 0;
            }
        }else{
            // pm
            if(hour != 12){
                hour += 12;
            }
        }
        return hour;
    }

    public static Time resolveAmbiguity(Time time, int ampmFlag){
        Time t = new Time(3);
        if(ampmFlag == 1){
            // am
            t.hour = time.hour2;
            t.minute = time.minute2;
        }else{
            // pm
            t.hour = time.hour;
            t.minute = time.minute;
        }
        return t;
    }

    public static boolean compareAMPMFlag(int hour,int ampmFlag){
        if(ampmFlag == 1){
            // am
            if(hour < 12){
                return true;
            }
            return false;
        }else{
            // pm
            if(hour >= 12){
                return true;
            }
            return false;
        }
    }

}
