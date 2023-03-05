package db.pitt.chatbotbackendsupport.service;

import db.pitt.chatbotbackendsupport.entity.Response;
import db.pitt.chatbotbackendsupport.entity.Time;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;

import static db.pitt.chatbotbackendsupport.constant.Constant.linkWordToInt;
import static db.pitt.chatbotbackendsupport.constant.Constant.wordToInt;

@Slf4j
@Service
public class ExtractTime {
    /**
     * Two cases
     * 1. Ambiguity Resolved
     * Arrival at 1 pm, departure 10 o'clock
     * The algorithm will return 10:00 (10 am)
     * 2. Cannot Resolve Ambiguity
     * Arrival at 10 am, departure  11 o'clock
     * Either 11 am or pm will not work in this case.
     */
    public Response DepartureAmbiguity(int departureHour,
                                       int departureMin,
                                       int arrivalHour,
                                       int arrivalMin){
        if(departureHour == 12){
            departureHour = 0;
        }
        Response response = new Response();
        response.code = 7;
        response.time = new Time(departureHour,departureMin);
        // departure at 10 arrival at 11
        if(departureHour < arrivalHour){
            if(departureHour +12 < arrivalHour){
                response.time.hour = departureHour+ 12;
                return response;
            } else if (departureHour+12 == arrivalHour) {
                if(departureMin < arrivalMin){
                    response.time.hour = departureHour+ 12;
                    return response;
                }else {
                    return response;
                }
            }else {
                return response;
            }
        } else if (departureHour == arrivalHour) {
            if(departureMin < arrivalMin){
                return response;
            }else {
                response.code = 8;
                return response;
            }
        }else {
            // Example : departure at 11 arrival at 10
            response.code = 8;
            return response;
        }
    }
    /**
     * TODO :Ambiguity , 20-5 - only for Hyphenated word
     */
    public Response RE(String input) {
        String[] words = input.split(" ");

        HashMap<Integer,Integer> indexInteger= new HashMap<>(); // Records every Integer we extract and record it and its position
        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            /* If found key word To/Past,
               Check  MM （minute[s]) TO/PAST HH  Pattern
               Ambiguity exists in this pattern(am or pm)
            */
            if((i+1 < words.length) && (word.equalsIgnoreCase("to") || word.equalsIgnoreCase("past"))){

                // Extract the minute
                Integer min = null;
                if(indexInteger.containsKey(i-1)){
                    //One Possible Pattern:  m + past/to + h
                    min = indexInteger.get(i-1);
                }else{
                    //The other Possible Pattern: m minute + past/to + h
                    if(words[i-1].toLowerCase().contains("minute") && indexInteger.containsKey(i-2)){
                        min = indexInteger.get(i-2);
                    }
                }
                if(min == null || !isValidMinute(min)) continue;

                // Extract Hour
                Integer hour = extractInt(words[i+1]);
                indexInteger.put(i+1,hour);
                if(hour != null && is12HourSystem(hour)){
                    // Set Ambiguity Code
                    Response response = new Response();
                    response.code = 1;
                    if(word.equalsIgnoreCase("to")){
                        response.time = new Time(hour-1,60-min);
                    }else{
                        response.time = new Time(hour,min);
                    }
                    return response;
                }
            }

            /* Check HH:MM / HHMM Pattern */
            Response response = extractHHMM(word,words,i,indexInteger);
            if(response.code == 10){
                // Check the next word is PM/AM
                int XM  = 0; // 0 : not exist , 1:am , 2:pm
                if( i+1<words.length){
                    if(words[i+1].toLowerCase().contains("am")){
                        XM = 1;
                    }
                    if(words[i+1].toLowerCase().contains("pm")){
                        XM = 2;
                    }
                }

                // Conflict : If the hour is already grater than 12 or equal to 0
                if(response.time.hour>=13 || response.time.hour ==0){
                    response.code = 2;
                    return response;
                }

                response.code = 0;
                if(XM == 2){
                    if(response.time.hour != 12) {
                        response.time.hour += 12;
                    }
                    return response;
                }

                if(XM == 1){
                    if(response.time.hour == 12){
                        response.time.hour -= 12;
                    }
                    return response;
                }

                // TODO: Ambiguity happens
                response.code = 1;
                return response;
            }

            /* If we found the word is a number
            Check whether match
                HH MM [pm/am] Pattern
            or  HH pm/am Pattern
            */
            Integer possibleInt = extractInt(word);
            if(possibleInt != null){
                // Collect a number
                indexInteger.put(i,possibleInt);

                // Whether the previous word is also a number
                // Then match the pattern HH MM [pm or am]
                if(indexInteger.containsKey(i-1) && isValidMinute(possibleInt)){
                    int hour= indexInteger.get(i-1);
                    if(i+1 < words.length){
                        if( isPM(words[i+1])|| isAM(words[i+1])){
                            //Possible Conflict happens,such as 13am 13pm
                            if(!is12HourSystem(hour)){
                                response.code = 2;
                                response.time = new Time(hour,possibleInt);
                                return response;
                            }
                            response.code = 0;
                            if(isPM(words[i+1])){
                                if(hour == 12) {
                                    response.time = new Time(12, possibleInt);
                                }else{
                                    response.time = new Time(hour + 12, possibleInt);
                                }
                            }else {
                                if( hour == 12){
                                    response.time = new Time(0, possibleInt);
                                }else {
                                    response.time = new Time(hour, possibleInt);
                                }
                            }
                            return response;
                        }
                        else {
                            response.code = 0;
                            response.time = new Time(hour, possibleInt);
                            // Ambiguity happens
                            if(hour<12){
                                response.code = 1;
                            }
                            return response;
                        }
                    }
                    else{
                        response.code = 0;
                        response.time = new Time(hour, possibleInt);
                        // Ambiguity happens
                        if(hour<12){
                            response.code = 1;
                        }
                        return response;
                    }
                }
                else {
                    // Check the pattern HH pm/am
                    if (isValidHour(possibleInt) &&(i + 1 < words.length) && ( isPM(words[i + 1]) || isAM(words[i + 1]))) {
                        int hour = possibleInt;
                        // Possible Conflict
                        if(!is12HourSystem(hour)){
                            response.code = 2;
                            response.time = new Time(hour,0);
                            return response;
                        }
                        response.code = 0;
                        if (isPM(words[i + 1])) {
                            if(hour == 12){ // 12pm - 12:00
                                response.time = new Time(12,0);
                            }else{
                                response.time = new Time(hour+12,0);
                            }
                        } else {
                            if(hour == 12){ // 12am -0:00
                                response.time = new Time(0,0);
                            }else{
                                response.time = new Time(hour,0);
                            }
                        }
                        return response;
                    }
                }
                continue;
            }

            /* Resolve Bad Case : hhPM, such as 2PM */
            if(isAM(word) || isPM(word)){
                String hourString= null;
                boolean isAM = true;
                if(word.toLowerCase().contains("am")) {
                    hourString = word.toLowerCase().split("am")[0];
                }
                if(word.toLowerCase().contains("a.m")) {
                    hourString = word.toLowerCase().split("a.m")[0];
                }
                if(word.toLowerCase().contains("pm")) {
                    hourString = word.toLowerCase().split("pm")[0];
                    isAM = false;
                }
                if(word.toLowerCase().contains("p.m")) {
                    hourString = word.toLowerCase().split("p.m")[0];
                    isAM = false;
                }
                Integer hour = extractInt(hourString);
                if(hour != null && isValidHour(hour)){
                    response.code = 0;
                    if(isValid12HourTimeSystem(hour)){
                        if(isAM){
                            if(hour == 12){
                                response.time = new Time(0,0);
                            }else {
                                response.time = new Time(hour,0);
                            }
                        }else{
                            if(hour == 12){
                                response.time = new Time(12,0);
                            }else {
                                response.time = new Time(hour+12,0);
                            }
                        }
                        return response;
                    }else{
                        response.code = 2;
                        return response;
                    }
                }
            }

            /* If the word contains Key clock */
            if(word.toLowerCase().contains("clock")){
                // Check the last index, whether we match the pattern hour clock
                if(indexInteger.containsKey(i-1)){
                    int hour = indexInteger.get(i-1);
                    if(is12HourSystem(hour)){
                        //Ambiguity happens
                        response.time = new Time(hour,0);
                        response.code = 1;
                        return response;
                    }
                    if(isValidHour(hour)){
                        response.code = 0;
                        response.time = new Time(hour,0);
                        return response;
                    }else{
                        // Not valid hour, continue
                        continue;
                    }
                }
            }

            /* If the word Match Key Word midday noon midnight */
            if(word.equalsIgnoreCase("noon") || word.equalsIgnoreCase("midday")){
                response.code = 0;
                response.time = new Time(12,0);
                return response;
            }

            if(word.equalsIgnoreCase("midnight")){
                response.code = 0;
                response.time = new Time(0,0);
                return response;
            }
        }
        Response response = new Response();
        response.code = 5;
        if(!indexInteger.isEmpty()) {
            // TODO Ambiguity: Does  11 can count as 11 o'clock ?
            for(int number : indexInteger.values()){
                if(isValidHour(number)){
                    if(is12HourSystem(number)){
                        // Ambiguity am or pm
                        response.code = 1;
                        response.time = new Time(number,0);
                    }else{
                        // No Ambiguity
                        response.code = 0;
                        response.time = new Time(number,0);
                        return  response;
                    }
                }
            }
        }
        return response;
    }


    /**
     *  If the word match the expression/format hh:mm or hhmm
     *  we will extract the time
     *  Attention:
     *      hhmm must grater than 100 and less than 2459 ..
     *   && 0<= hh <= 23
     *   && 0<= mm <= 59
     *
     *   Resolve Bad Cases: hh :mm or hh: mm
     *   or hh : mm
     */
    public Response extractHHMM(String word,String[] words,int i,HashMap<Integer,Integer> indexInteger){
        Response response = new Response();
        response.code = 11;
        if( word.contains(":")){
            String[] letters = word.split(":");
            if(letters.length>2){
                return response;
            }else if(letters.length == 2){
                // Two Possible Cases 1. :mm  2. hh:mm
                Integer hour = extractInt(letters[0]);
                Integer minute = extractInt(letters[1]);
                // hh:mm
                if(hour != null && minute != null && isValidHour(hour) && isValidMinute(minute)) {
                    response.code = 10;
                    response.time = new Time(hour,minute);
                }
                // :mm
                if(hour == null && letters[0].isEmpty() && minute != null && isValidMinute(minute)){
                    if(indexInteger.containsKey(i-1) && isValidHour(indexInteger.get(i-1))){
                        response.code = 10;
                        response.time = new Time(indexInteger.get(i-1),minute);
                    }
                }
                return response;
            } else if (letters.length ==1 ) {
                // hh: mm
                Integer hour = extractInt(letters[0]);
                if(hour !=null && isValidHour(hour) && (i+1) < words.length){
                    Integer minute = extractInt(words[i+1]);
                    if(minute != null && isValidMinute(minute)){
                        response.code = 10;
                        response.time = new Time(hour,minute);
                    }
                }
                return response;
            }else{
                // letters length == 0
                Integer hour = indexInteger.get(i-1);
                if(hour !=null && isValidHour(hour) && (i+1) < words.length){
                    Integer minute = extractInt(words[i+1]);
                    if(minute != null && isValidMinute(minute)){
                        response.code = 10;
                        response.time = new Time(hour,minute);
                    }
                }
                return response;
            }
        }
        else{
            Integer time = extractInt(word);
            if(time!= null && time>= 100){
                int minute= time%100;
                int hour = time/100;
                if(isValidHour(hour) && isValidMinute(minute) ){
                    response.code = 10;
                    response.time = new Time(hour,minute);
                }
            }
            return response;
        }
    }

    /**
     * Number can be in two format
     * 1. [Hyphenated] word
     * twenty-five  (25)
     * O-five       (5)
     * five         (5)
     * o            (0)
     *
     * 2. Number
     * 25
     * 5
     * The function try to extract number if the input following the format we assume
     * Return null if the function cannot recognize the number
     */
    public Integer extractInt(String word){
        if(word.contains("-")){
            String[] letters= word.split("-");
            if(letters.length>2){
                return null;
            }else {
                // Extract the first number zero or o or twenty.... fifty
                Integer firstNumber = linkWordToInt.get(letters[0].toLowerCase());
                // Extract second number
                Integer secondNumber = wordToInt.get(letters[1].toLowerCase());
                if(firstNumber != null && secondNumber != null && secondNumber<=9){
                    return firstNumber + secondNumber;
                }
                return null;
            }
        }else{
            return extractIntAtomic(word);
        }
    }

    public  Integer extractIntAtomic(String letter){
        // check whether is letter format (two) or number format (2)
        if(letter.length()>0 && letter.charAt(0)>='0' && letter.charAt(0) <='9'){
            try{
                return Integer.parseInt(letter);
            }catch (NumberFormatException e){
                return null;
            }
        }else {
            return wordToInt.get(letter);
        }
    }

    /**
     * The 12-hour clock is a time convention in which the 24 hours of the day are divided into two periods: a.m. and p.m.
     * Each period consists of 12 hours numbered: 12 (acting as 0), 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 and 11.
     * @param hour
     * This method will check whether hour is valid in 12 hour clock system
     */
    private  boolean isValid12HourTimeSystem(int hour){
        if(hour > 0 && hour <13){
            return true;
        }
        return false;
    }

    private  boolean isValidMinute(int minute){
        if(minute<0 || minute>59){
            return false;
        }
        return true;

    }

    private  boolean isValidHour(int hour){
        if(hour<0 || hour>23){
            return false;
        }
        return true;
    }

    /**
     * If the given hour is 0 or grater than 12, then the time system is 24 hour system
     */
    private boolean is24HourSystem(int hour){
        if(hour ==0 || (hour>12 && hour<24)){
            return true;
        }
        return false;
    }

    private boolean is12HourSystem(int hour){
        if(hour>=1 && hour<=12){
            return true;
        }
        return false;
    }

    private boolean isAM(String word){
        if(word.toLowerCase().contains("am") || word.toLowerCase().contains("a.m")){
            return true;
        }
        return false;
    }

    private boolean isPM(String word){
        if(word.toLowerCase().contains("pm") || word.toLowerCase().contains("p.m")){
            return true;
        }
        return false;
    }
}
