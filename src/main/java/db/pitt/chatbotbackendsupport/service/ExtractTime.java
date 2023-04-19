package db.pitt.chatbotbackendsupport.service;

import db.pitt.chatbotbackendsupport.entity.Response;
import db.pitt.chatbotbackendsupport.entity.Time;
import db.pitt.chatbotbackendsupport.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;


@Slf4j
@Service
public class ExtractTime {
    /**
     * Resolve Ambiguity
     * Given the fact that arrival time must be later than the departure time.
     * Choose the closest departure time  from possible time.
     * For example , we extract two possible Departure  time : 10 or 22  and Arrival time is  23.
     * Both 10 or 22 are appropriate, Return 22 since 22 is closet time.
     *
     * Input : Two candidate departure 1 and departure 2. And departure 1 <（earlier) departure 2.
     */
    public Response DepartureAmbiguity(int departureHour1,
                                       int departureMin1,
                                       int departureHour2,
                                       int departureMin2,
                                       int arrivalHour,
                                       int arrivalMin){
        Response response = new Response();
        response.code = 7;
        Time responseTime = new Time(3);
        responseTime.hour = -1;
        responseTime.minute = -1;
        // Departure Time 2
        if(departureHour2 < arrivalHour){
            responseTime.hour = departureHour2;
            responseTime.minute = departureMin2;
        } else if (departureHour2 == arrivalHour) {
            if(departureMin2 < arrivalMin){
                responseTime.hour = departureHour2;
                responseTime.minute = departureMin2;
            }
        }

        // Departure Time 1
        if(responseTime.hour == -1){
            if(departureHour1 < arrivalHour){
                responseTime.hour = departureHour1;
                responseTime.minute = departureMin1;
            } else if (departureHour1 == arrivalHour) {
                if(departureMin1 < arrivalMin){
                    responseTime.hour = departureHour1;
                    responseTime.minute = departureMin1;
                }
            }
        }
        if(responseTime.hour == -1){
            response.code = 8;
        }
        response.time = responseTime;
        return response;
    }


    /**
     *  Time Units:
     *  1. HHMM Pattern
     *  two thirty-four (234)
     *  1800
     *  1:30
     *
     *  2. HH Pattern
     *  3 o'clock
     *  6 am
     *
     *  3. Keyword:
     *  noon
     *  now
     *
     *  4. Possible Time or Time Duration
     *  three
     *
     *  5. Possible Time Duration
     *  Quarter
     *  Half
     *
     *  6. Time Duration
     *  15 minutes
     *  30 minutes
     *  3 hours
     *  half hour/ half of an hour
     *
     *  Relative time
     *  quarter after noon
     *  quarter after three
     *  10 minutes prior to the arrival
     *  five past three
     *  30 minutes from now
     *  I need to be there in 30 minutes
     */

    /**
     * First Round:
     *  Extract
     *      Possible Time
     *      Time Duration
     *      AMPM indication
     *
     * Second Round:
     *  Get fixed Time by
     *  keyword
     *      ： -> hh : mm
     *      am pm -> number am/pm
     *
     * Third Round:
     *  Based on keyword such as prior, .... Calculate relative time
     *
     * Fourth Round:
     *  Identify AM PM or Ambiguity
     */
    public static Response RE(String input) {
        String[] words = Util.splitByNumber(input);
        Map<Integer,Time> indexTime= new TreeMap<>();

        // First Round
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            // Case 1. duration:  quarter, minutes ,hours ,half
            // Case 2. single number 10/ 3 / thirty-four ....
            // Case 3. clock
            // Case 4. pm am morning .....
            // Case 5. specific time: noon midday...
            if(Case1(indexTime,words,word,i)){
                continue;
            }
            if(Case2(indexTime,words,word,i)){
                continue;
            }
            if(Case3(indexTime,words,word,i)){
                continue;
            }
            if(Case4(indexTime,words,word,i)){
                continue;
            }
            if(Case5(indexTime,words,word,i)){
                continue;
            }
        }

        // Second Round
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            Case6(indexTime,words,word,i);
        }

        /**
         * Extract Time Unit Test:
         */
//        System.out.println("Extract Time Unit Test ----");
//        for(Map.Entry<Integer,Time> entry : indexTime.entrySet()) {
//            Integer pos = entry.getKey();
//            Time t =  entry.getValue();
//            System.out.println("Pos" + pos+ " ,"+t);
//        }
//        System.out.println(" End. ");

        // Third Round
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            Case7(indexTime,words,word,i);
        }

        /**
         * Relative Time Test:
         */
//        System.out.println("Relative Time Unit Test ----");
//        for(Map.Entry<Integer,Time> entry : indexTime.entrySet()) {
//            Integer pos = entry.getKey();
//            Time t =  entry.getValue();
//            System.out.println("Pos" + pos+ " ,"+t);
//        }
//        System.out.println(" End. ");

        // Fourth Round
        Time answer =  Case8(indexTime);
        Response response = new Response();
        if(answer.flag ==3 ){
            response.code = 0;
            response.time = answer;
        }
        else if(answer.flag == 2){
            response.code = 1;
            response.time = answer;
        }
        else {
            response.code = 5;
        }
        return response;
    }

    /**
     * Case 1. Duration Pattern : quarter, minutes ,hours ,half
     */
    public static  boolean Case1(Map<Integer,Time> indexTime,String[] words,String word,int i){
        boolean match = false;
        Time time = new Time(4);
        if(word.contains("minute")|| word.contains("min")){
            // Check last position
            if(indexTime.containsKey(i-1) && indexTime.get(i-1).flag == 1){
                match = true;
                time.durationHour  = indexTime.get(i-1).number/60;
                time.durationMin = indexTime.get(i-1).number%60;
                indexTime.remove(i-1);
                indexTime.put(i,time);
            }
        }else if(word.contains("hour")){
            // Check last position
            if(indexTime.containsKey(i-1) && indexTime.get(i-1).flag == 1){
                if(indexTime.get(i-1).number<24) {
                    match = true;
                    time.durationHour = indexTime.get(i - 1).number;
                    time.durationMin = 0;
                    indexTime.remove(i - 1);
                    indexTime.put(i, time);
                }
            }
        }else if(word.contains("quarter")){
            // Check the last word one quarter / 2 quarters / 3 quarters
            if(indexTime.containsKey(i-1) && indexTime.get(i-1).flag == 1){
                if(indexTime.get(i-1).number<4 && indexTime.get(i-1).number>0) {
                    match = true;
                    time.durationHour = 0;
                    time.durationMin = indexTime.get(i-1).number*15;
                    indexTime.remove(i - 1);
                    indexTime.put(i, time);
                }
            }else{
                match = true;
                time.durationHour = 0;
                time.durationMin = 15;
                indexTime.remove(i - 1);
                indexTime.put(i, time);
            }

        } else if(word.contains("half")){
            match = true;
            time.durationHour = 0;
            time.durationMin = 30;
            indexTime.put(i, time);
        }
        return match;
    }

    /**
     * Case 2. Extract Single Number
     *
     *  x can be number/ half / quarter......
     *  x> 100 only time
     *  x 60- 99 minutes duration
     *  x 24- 59 part of time, minutes duration
     *  x 0 - 23 time, part of time ,minutes duration, hour duration
     *
     * Possible Scenario
     * Scenario one : Time
     *  1800
     *  0001
     *
     * Scenario two : Possible Time/ Part of Time
     *  1. two thirty-four (234)  - two cases
     *  3. 18:00 / 2:34
     *  4. 3 o'clock/am
     *  5. five past three
     *
     * Scenario three : Possible Time Duration
     *  6. two hour/ quarters
     */
    public static boolean Case2(Map<Integer,Time> indexTime,String[] words,String word,int i){
        Integer number = Util.extractInt(word);
        if(number != null){
            Time time = new Time(3);
            // Scenario one
            // Special case for 001 -> 059 or 0001 -> 0059
            if(Util.isNum(word.charAt(0))  &&(word.length() == 4 || word.length() == 3) && Util.isValidMinute(number)){
                time.hour = 0;
                time.minute = number;
                indexTime.put(i,time);
                return true;
            }

            if(number >= 100){
                int minute= number%100;
                int hour = number/100;
                if(Util.isValidHour(hour) && Util.isValidMinute(minute)){
                    time.hour = hour;
                    time.minute = minute;
                    if(Util.isValid12HourTimeSystem(hour)){
                        time.flag = 2; // ambiguity
                        time.minute2 = minute;
                        time.minute = minute;
                        if(hour == 12){
                            time.hour = 12;
                            time.hour2 = 0;
                        }else{
                            time.hour = hour + 12;
                            time.hour2 = hour;
                        }
                    }
                    indexTime.put(i,time);
                    return true;
                }
                return false;
            }

            //  Scenario two
            // Check the last position whether it's a number
            if(indexTime.containsKey(i-1) && indexTime.get(i-1).flag == 1){
                // HH MM Pattern
                if(Util.isValidHour( indexTime.get(i-1).number) && Util.isValidMinute( number ) ){
                    time.hour = indexTime.get(i-1).number;
                    time.minute = number;

                    if(Util.isValid12HourTimeSystem(indexTime.get(i-1).number)){
                        time.flag = 2; // ambiguity
                        time.minute2 = number;
                        time.minute = number;
                        if(indexTime.get(i-1).number == 12){
                            time.hour = 12;
                            time.hour2 = 0;
                        }else{
                            time.hour = indexTime.get(i-1).number + 12;
                            time.hour2 = indexTime.get(i-1).number;
                        }
                    }
                    indexTime.put(i,time);
                    indexTime.remove(i-1);
                    return true;
                }
            }

            time.flag = 1;
            time.number = number;
            indexTime.put(i,time);
            return true;
        }
        return false;
    }

    /**
     * Case 3. clock
     * 9 o'clock
     */
    public static boolean Case3(Map<Integer,Time> indexTime,String[] words,String word,int i){
        if(word.contains("clock")) {

            // Check the last position
            if (indexTime.containsKey(i - 1) && indexTime.get(i - 1).flag == 1) {
                int number = indexTime.get(i - 1).number;
                Time time = new Time(3);
                if (Util.isValidHour(number)) {
                    time.hour = number;
                    time.minute = 0;
                    if (Util.isValid12HourTimeSystem(number)) {
                        time.flag = 2;// ambiguity
                        time.minute2 = 0;
                        time.minute = 0;
                        if(number == 12){
                            time.hour = 12;
                            time.hour2 = 0;
                        }else{
                            time.hour = number + 12;
                            time.hour2 = number;
                        }
                    }
                    indexTime.put(i, time);
                    indexTime.remove(i - 1);
                    return true;
                }
            }
            // Consider the edge case 6 o clock / 6 o' clock
            if (i-2>=0 && (words[i-1].equals("o") || words[i-1].equals("o'") ) && indexTime.containsKey(i - 2) && indexTime.get(i - 2).flag == 1) {
                int number = indexTime.get(i - 2).number;
                Time time = new Time(3);
                if (Util.isValidHour(number)) {
                    time.hour = number;
                    time.minute = 0;
                    if (Util.isValid12HourTimeSystem(number)) {
                        time.flag = 2;// ambiguity
                        time.minute2 = 0;
                        time.minute = 0;
                        if(number == 12){
                            time.hour = 12;
                            time.hour2 = 0;
                        }else{
                            time.hour = number + 12;
                            time.hour2 = number;
                        }
                    }
                    indexTime.put(i, time);
                    indexTime.remove(i - 2);
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * Case 4. specific time:
     * noon  - 12:00
     * midday - 12:00
     * midnight - 0:00
     * now
     */
    public static boolean Case4(Map<Integer,Time> indexTime,String[] words,String word,int i){
        boolean match = false;
        Time time = new Time(3);
        if(word.equals("noon") || word.equals("midday") ){
            match = true;
            time.hour = 12;
            time.minute = 0;
            indexTime.put(i,time);
        }
        else if (word.equals("midnight")) {
            match = true;
            time.hour = 0;
            time.minute = 0;
            indexTime.put(i,time);
        } else if (word.equals("now")) {
            match = true;
            time = Util.getNow();
            indexTime.put(i,time);
        }
        return match;
    }

    /**
     * Case 5. indicate PM and AM
     * AM: am a.m morning dawn sunrise (ante meridiem)
     * PM: pm p.m evening afternoon dusk (post meridiem)
     */
    public static  boolean Case5(Map<Integer,Time> indexTime,String[] words,String word,int i){
        boolean match = false;
        Time time = new Time(5);
        if(word.equals("morning") ||word.equals("dawn")|| word.equals("sunrise") ){
            time.flag = 6;
            match = true;
            time.ampmFlag = 1;
            indexTime.put(i,time);
        } else if (word.equals("am") || word.equals("a.m")) {
            match = true;
            time.ampmFlag = 1;
            indexTime.put(i,time);
        } else if (word.equals("evening") ||word.equals("afternoon")|| word.equals("dusk") ) {
            time.flag = 6;
            match = true;
            time.ampmFlag = 2;
            indexTime.put(i,time);
        } else if (word.equals("pm") || word.equals("p.m")) {
            match = true;
            time.ampmFlag = 2;
            indexTime.put(i,time);
        } else if (word.equals("meridiem")) {
            // Check the last word
            if(i-1>=0){
                String lastWord = words[i-1];
                if(lastWord.equals("ante")){
                    match = true;
                    time.ampmFlag = 1;
                    indexTime.put(i,time);
                } else if (lastWord.equals("post")) {
                    match = true;
                    time.ampmFlag = 2;
                    indexTime.put(i,time);
                }
            }
        }
        return match;
    }

    /**
     * Case 6.
     * : -> 3:2
     * 6pm -> 6:00
     */
    public static boolean Case6(Map<Integer,Time> indexTime,String[] words,String word,int i){
        Time t = indexTime.get(i);
        if(word.contains(":")){
            Time time = new Time(3);
            String[] letters = word.split(":");
            if(letters.length>2){
                // hh:mm:ss
                // :mm:ss
                Integer minute = Util.extractInt(letters[1]);
                if(minute != null && Util.isValidMinute(minute)){
                    if(letters[0].length() == 0){
                        // :mm:ss Check the previous number
                        if(indexTime.containsKey(i-1) && indexTime.get(i-1).flag == 1){
                            int h = indexTime.get(i-1).number;
                            if(Util.isValidHour(h)){
                                time.hour = h;
                                time.minute = minute;

                                if(Util.isValid12HourTimeSystem(h)){
                                    time.flag = 2;// ambiguity
                                    time.minute2 = minute;
                                    time.minute = minute;
                                    if(h == 12){
                                        time.hour = 12;
                                        time.hour2 = 0;
                                    }else{
                                        time.hour = h+ 12;
                                        time.hour2 = h;
                                    }
                                }
                                indexTime.remove(i-1);
                                indexTime.put(i,time);
                                return true;
                            }
                        }
                    }else{
                        // hh:mm:ss
                        Integer hour = Util.extractInt(letters[0]);
                        if(hour != null && Util.isValidHour(hour)){
                            time.hour = hour;
                            time.minute = minute;
                            if(Util.isValid12HourTimeSystem(hour)){
                                time.flag = 2;// ambiguity
                                time.minute2 = minute;
                                time.minute = minute;
                                if(hour == 12){
                                    time.hour = 12;
                                    time.hour2 = 0;
                                }else{
                                    time.hour = hour+ 12;
                                    time.hour2 = hour;
                                }
                            }
                            indexTime.remove(i-1);
                            indexTime.put(i,time);
                            return true;
                        }
                    }
                }
            }
            else if(letters.length == 2){
                // Two Possible Cases 1. :mm  2. hh:mm
                Integer minute = Util.extractInt(letters[1]);
                // :mm
                if(letters[0].length() == 0 && minute != null && Util.isValidMinute(minute)){
                    // Check the previous number
                    if(indexTime.containsKey(i-1) && indexTime.get(i-1).flag == 1){
                        int h = indexTime.get(i-1).number;
                        if(Util.isValidHour(h)){
                            time.hour = h;
                            time.minute = minute;
                            if(Util.isValid12HourTimeSystem(h)){
                                time.flag = 2;// ambiguity
                                time.minute2 = minute;
                                time.minute = minute;
                                if(h == 12){
                                    time.hour = 12;
                                    time.hour2 = 0;
                                }else{
                                    time.hour = h+ 12;
                                    time.hour2 = h;
                                }
                            }
                            indexTime.remove(i-1);
                            indexTime.put(i,time);
                            return true;
                        }
                    }
                }

                Integer hour = Util.extractInt(letters[0]);
                // hh:mm
                if(hour != null && minute != null && Util.isValidHour(hour) && Util.isValidMinute(minute)) {
                    time.hour = hour;
                    time.minute = minute;
                    if (Util.isValid12HourTimeSystem(hour)) {
                        time.flag = 2;// ambiguity
                        time.minute2 = minute;
                        time.minute = minute;
                        if (hour == 12) {
                            time.hour = 12;
                            time.hour2 = 0;
                        } else {
                            time.hour = hour + 12;
                            time.hour2 = hour;
                        }
                    }
                    indexTime.put(i, time);
                    return true;
                }

            }
            else if (letters.length ==1 ) {
                // hh: mm
                Integer hour = Util.extractInt(letters[0]);
                // Check the next number
                if(hour !=null && Util.isValidHour(hour) ){
                    if(indexTime.containsKey(i+1) && indexTime.get(i+1).flag == 1){
                        int min =  indexTime.get(i+1).number;
                        if(Util.isValidMinute(min)){
                            time.hour = hour;
                            time.minute = min;
                            if(Util.isValid12HourTimeSystem(hour)){
                                time.flag = 2;// ambiguity
                                time.minute2 = min;
                                time.minute = min;
                                if (hour == 12) {
                                    time.hour = 12;
                                    time.hour2 = 0;
                                } else {
                                    time.hour = hour + 12;
                                    time.hour2 = hour;
                                }
                            }
                            indexTime.remove(i+1);
                            indexTime.put(i,time);
                            return true;
                        }
                    }
                }
            }
            else{
                // letters length == 0 hh : mm
                // Check the previous and next number
                if(indexTime.containsKey(i-1) && indexTime.get(i-1).flag == 1 && Util.isValidHour(indexTime.get(i-1).number)){
                    int hour = indexTime.get(i-1).number;
                    if(indexTime.containsKey(i+1) && indexTime.get(i+1).flag == 1 && Util.isValidMinute(indexTime.get(i+1).number)){
                        int minute = indexTime.get(i+1).number;
                        time.hour = hour;
                        time.minute = minute;
                        if(Util.isValid12HourTimeSystem(hour)){
                            time.flag = 2;// ambiguity
                            time.minute2 = minute;
                            time.minute = minute;
                            if (hour == 12) {
                                time.hour = 12;
                                time.hour2 = 0;
                            } else {
                                time.hour = hour + 12;
                                time.hour2 = hour;
                            }
                        }
                        indexTime.remove(i-1);
                        indexTime.remove(i+1);
                        indexTime.put(i,time);
                        return true;
                    }
                }
            }
        }
        else if (t!= null && t.flag == 5) {
            for (int j = i - 1; j >= 0; j--) {
                if (indexTime.get(j) != null) {
                    // Number or Ambiguity Time
                    Time candidate = indexTime.get(j);
                    if (candidate.flag == 3) {
                        // TODO : Conflict
                        break;
                    }
                    if (candidate.flag == 2) {
                        // Remove the previous Time unit and add new one
                        indexTime.remove(j);
                        indexTime.remove(i);

                        // Resolve ambiguity
                        Time newTime = Util.resolveAmbiguity(candidate, t.ampmFlag);
                        indexTime.put(i, newTime);
                    } else if (candidate.flag == 1) {
                        // Number x pm or am
                        if (Util.isValid12HourTimeSystem(candidate.number)) {
                            // Remove the previous Time unit and add new one
                            indexTime.remove(j);
                            indexTime.remove(i);
                            Time newTime = new Time(3);
                            newTime.hour = Util.transform12To24(candidate.number, t.ampmFlag);
                            newTime.minute = 0;
                            indexTime.put(i, newTime);
                        }
                    }
                    break;
                }
            }
        }
        return false;
    }

    /**
     * Case 7. resolve relative time
     * "in"                 in duration(x minutes /hours)            example: in 30 minutes
     * "later"              duration later (now)                     example: Two hours later
     * "from, past, after"  duration from/past/after fixed time      example: 30 minutes from now , five past three , quarter after three
     * "prior (to) / to "   duration prior (to) / to fixed time      example: three to five
     */
    public static boolean Case7(Map<Integer,Time> indexTime,String[] words,String word,int i){
        if(word.equals("in")){
            // now + duration
            Time now = Util.getNow();
            for(int j = i+1;j<words.length;j++){
                // Find the duration TODO- possible time? in 30 in half?
                if(indexTime.containsKey(j) && indexTime.get(j).flag == 4){
                    Time relativeTime = Util.addTime(now,indexTime.get(j));
                    if(relativeTime != null){
                        indexTime.remove(j);
                        indexTime.put(i,relativeTime);
                        break;
                    }
                }
            }
        }
        else if (word.equals("later")) {
            // now + duration
            Time now = Util.getNow();
            for(int j = i-1;j>=0;j--){
                // Find the duration TODO- possible time?  30 later?
                if(indexTime.containsKey(j) && indexTime.get(j).flag == 4){
                    Time relativeTime = Util.addTime(now,indexTime.get(j));
                    if(relativeTime != null){
                        indexTime.remove(j);
                        indexTime.put(i,relativeTime);
                        break;
                    }
                }
            }
        }
        else if (word.equals("past") || word.equals("after") || word.equals("from") || word.equals("to") || word.equals("prior")) {
            int durationIndex = -1;
            // Find the duration or Possible Time
            for(int j = i-1;j>=0;j--){
                if(indexTime.containsKey(j) && (indexTime.get(j).flag == 4 || indexTime.get(j).flag == 1 )){
                    if(indexTime.get(j).flag == 4) {
                        durationIndex = j;
                        break;
                    }else{
                        if(!word.equals("from")) {
                            int number = indexTime.get(j).number;
                            // Possible minutes TODO 59-99
                            if (number >= 1 && number <= 59) {
                                durationIndex = j;
                                break;
                            }
                        }
                    }
                }
            }
            if(durationIndex == -1){
                return false;
            }

            int fixedIndex = -1;
            // Find fixed time
            for(int j = i;j<words.length;j++){
                if(indexTime.containsKey(j) && (indexTime.get(j).flag == 1 || indexTime.get(j).flag == 2 || indexTime.get(j).flag==3 )){
                    if(indexTime.get(j).flag == 1){
                        int number = indexTime.get(j).number;
                        // Possible hour
                        if(number>=0 && number <= 23){
                            fixedIndex = j;
                            break;
                        }
                    }else{
                        fixedIndex = j;
                        break;
                    }
                }
            }
            if(fixedIndex == -1){
                return false;
            }

            // Calculate time
            // Duration Part
            Time duration = indexTime.get(durationIndex);
            if(duration.flag == 1){
                duration.flag = 4;
                duration.durationHour = 0;
                duration.durationMin = duration.number;
            }
            indexTime.remove(durationIndex);

            // Fixed Time Part
            Time time = indexTime.get(fixedIndex);
            if(time.flag == 1){
                if(Util.isValid12HourTimeSystem(time.number)){
                    time.flag = 2;
                    if(time.number == 12){
                        time.hour = 12;
                        time.hour2 = 0;
                    }else{
                        time.hour = time.number +12;
                        time.hour2 = time.number;
                    }
                    time.minute = 0;
                    time.minute2 = 0;
                }else{
                    time.flag = 3;
                    time.hour = time.number;
                    time.minute = 0;
                }
            }
            indexTime.remove(fixedIndex);
            Time relativeTime;
            if(word.equals("to")|| word.equals("prior")){
                relativeTime = Util.subTime(time,duration);
            }else {
                relativeTime = Util.addTime(time,duration);
            }
            // Avoid Prior To
            if(word.equals("prior") && i+1< words.length && words[i+1].equals("to")){
                i++;
            }
            if(relativeTime != null) {
                indexTime.put(i, relativeTime);
            }
        }
        return false;
    }

    /**
     * Case 8. resolve AM PM
     *  Possible Combination
     *  AMPM Intention +  (Ambiguity Time OR Possible Time)
     *  (Ambiguity Time OR Possible Time)+ AMPM Intention
     *
     *  Idea1. Extract the first Possibility (Try First)
     *  Idea2. Extract all possibilities
     *  Idea3. Extra functionality : Compare ampm flag with real time am pm ? Example : pm - 13:00
     */
    public static Time Case8(Map<Integer,Time> indexTime){
        Time time = new Time(-1);

        ArrayList<Integer> indices = new ArrayList<>();
        indices.addAll( ((TreeMap) indexTime).navigableKeySet());

        int ampmFlag = 0;
        boolean ambiguity = true;
        int candidateHour = -1;
        int candidateMin = -1;
        int candidateIndex = -1;
        Time ambiguityCandidate = new Time(2);
        int ambiguityCandidateIndex = -1;
        for(int i = 0 ; i< indices.size();i++){
            Time t = indexTime.get(indices.get(i));
            if(t.flag == -1 || t.flag == 4 || t.flag == 5){
                continue;
            }
            if(t.flag == 1){
                int number = t.number;
                if(number <24 ){
                    // Possible Candidate
                    if(Util.isValid12HourTimeSystem(number)){
                        ambiguity = true;
                        if(ampmFlag != 0){
                            time.flag = 3;
                            time.hour = Util.transform12To24(number,ampmFlag);
                            time.minute = 0;
                            return time;
                        }
                    }else {
                        ambiguity = false;
                        if(ampmFlag != 0){
                            time.flag = 3;
                            time.hour = Util.transform12To24(number,ampmFlag);
                            time.minute = 0;
                            return time;
                        }
                    }
                    // It's a candidate
                    candidateIndex = i;
                    candidateHour = number;
                    candidateMin = 0;
                }
            }
            else if (t.flag == 3 ) {
                // TODO Might Compare with ampm flag intention Later (Conflict Case) !! E.X. 13pm in the morning
                time.flag = 3;
                time.hour = t.hour;
                time.minute = t.minute;
                return time;
            }
            else if (t.flag == 2) {
                ambiguity = true;
//                ambiguityCandidateHour = t.hour;
//                ambiguityCandidateMin= t.minute;
                ambiguityCandidate = t;
                ambiguityCandidateIndex = i;
                //  System.out.println("----- Test "+t.hour +":"+t.minute);
            }
            /**
             else if (t.flag == 5) {
             // Choose the closest Candidate
             if(candidateIndex == -1){
             if(ambiguityCandidateIndex== -1){
             // No Candidate
             continue;
             }else{
             // Choose Ambiguity Candidate
             //                        time.flag = 3;
             //                        time.hour = Util.transform12To24(ambiguityCandidateHour,t.ampmFlag);
             //                        time.minute = ambiguityCandidateMin;
             time = Util.resolveAmbiguity(ambiguityCandidate,ampmFlag);
             return time;
             }
             }
             else{
             if(ambiguityCandidateIndex== -1){
             // Choose number Candidate
             if(ambiguity) {
             time.flag = 3;
             time.hour = Util.transform12To24(candidateHour, t.ampmFlag);
             time.minute = candidateMin;
             return time;
             }else{
             if(Util.compareAMPMFlag(candidateHour,t.ampmFlag)){
             time.flag = 3;
             time.hour = candidateHour;
             time.minute = candidateMin;
             return time;
             }else{
             // Clean the bad candidate Example 13 am, here 13 will be cleaned
             candidateHour = -1;
             candidateMin = -1;
             candidateIndex = -1;
             }
             }
             }else{
             // Choose closet Candidate
             if(ambiguityCandidateIndex > candidateIndex){
             //                            time.flag = 3;
             //                            time.hour = Util.transform12To24(ambiguityCandidateHour,t.ampmFlag);
             //                            time.minute = ambiguityCandidateMin;
             time = Util.resolveAmbiguity(ambiguityCandidate,ampmFlag);
             return time;
             }else {
             // Choose number Candidate
             if(ambiguity) {
             time.flag = 3;
             time.hour = Util.transform12To24(candidateHour, t.ampmFlag);
             time.minute = candidateMin;
             return time;
             }else{
             if(Util.compareAMPMFlag(candidateHour,t.ampmFlag)){
             time.flag = 3;
             time.hour = candidateHour;
             time.minute = candidateMin;
             return time;
             }else{
             // Clean the bad candidate Example 13 am, here 13 will be cleaned
             candidateHour = -1;
             candidateMin = -1;
             candidateIndex = -1;
             }
             }
             }
             }
             }
             }
             **/
            else if (t.flag == 6) {
                // Choose the closest Candidate
                if(candidateIndex == -1){
                    if(ambiguityCandidateIndex== -1){
                        // No Candidate
                        ampmFlag = t.ampmFlag;
                        continue;
                    }else{
                        // Choose Ambiguity Candidate
                        time = Util.resolveAmbiguity(ambiguityCandidate,ampmFlag);
                        return time;
                    }
                }
                else{
                    if(ambiguityCandidateIndex== -1){
                        // Choose number Candidate
                        if(ambiguity) {
                            time.flag = 3;
                            time.hour = Util.transform12To24(candidateHour, t.ampmFlag);
                            time.minute = candidateMin;
                            return time;
                        }else{
                            if(Util.compareAMPMFlag(candidateHour,t.ampmFlag)){
                                time.flag = 3;
                                time.hour = candidateHour;
                                time.minute = candidateMin;
                                return time;
                            }else{
                                // Clean the bad candidate Example 13 am, here 13 will be cleaned
                                candidateHour = -1;
                                candidateMin = -1;
                                candidateIndex = -1;
                                // Set Flag
                                ampmFlag = t.ampmFlag;
                            }
                        }
                    }else{
                        // Choose closet Candidate
                        if(ambiguityCandidateIndex > candidateIndex){
                            time = Util.resolveAmbiguity(ambiguityCandidate,ampmFlag);
                            return time;
                        }else {
                            // Choose number Candidate
                            if(ambiguity) {
                                time.flag = 3;
                                time.hour = Util.transform12To24(candidateHour, t.ampmFlag);
                                time.minute = candidateMin;
                                return time;
                            }else{
                                if(Util.compareAMPMFlag(candidateHour,t.ampmFlag)){
                                    time.flag = 3;
                                    time.hour = candidateHour;
                                    time.minute = candidateMin;
                                    return time;
                                }else{
                                    // Clean the bad candidate Example 13 am, here 13 will be cleaned
                                    candidateHour = -1;
                                    candidateMin = -1;
                                    candidateIndex = -1;
                                    // Set Flag
                                    ampmFlag = t.ampmFlag;
                                }
                            }
                        }
                    }
                }
            }
        }
        // Pick from candidate
        // Priority - > Pick ambiguity Candidate First
        if(ambiguityCandidateIndex!= -1){
            time = ambiguityCandidate;
        }
        else{
            if(candidateIndex != -1){
                if(ambiguity){
                    time.flag = 2;
                    if(candidateHour == 12){
                        time.hour2 = 0;
                        time.hour = 12;
                    }else{
                        time.hour2 = candidateHour;
                        time.hour = candidateHour+12;
                    }
                    time.minute = candidateMin;
                    time.minute2 = candidateMin;
                }else{
                    time.flag = 3;
                    time.hour = candidateHour;
                    time.minute = candidateMin;
                }
            }
        }
        return time;
    }

}
