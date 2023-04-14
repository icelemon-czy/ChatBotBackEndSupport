package db.pitt.chatbotbackendsupport.constant;

import java.util.HashMap;

public class Constant {
    public static final HashMap<String,Integer> wordToInt = new HashMap<>();
    public static final HashMap<String,Integer> linkWordToInt = new HashMap<>();

    static{
        //    wordToInt.put("o",0);
        wordToInt.put("zero", 0);
        wordToInt.put("one", 1);
        wordToInt.put("two", 2);
        wordToInt.put("three", 3);
        wordToInt.put("four", 4);
        wordToInt.put("five", 5);
        wordToInt.put("six", 6);
        wordToInt.put("seven", 7);
        wordToInt.put("eight", 8);
        wordToInt.put("nine", 9);
        wordToInt.put("ten", 10);
        wordToInt.put("eleven", 11);
        wordToInt.put("twelve", 12);
        wordToInt.put("thirteen", 13);
        wordToInt.put("fourteen", 14);
        wordToInt.put("fifteen", 15);
        wordToInt.put("sixteen", 16);
        wordToInt.put("seventeen", 17);
        wordToInt.put("eighteen", 18);
        wordToInt.put("nineteen", 19);
        wordToInt.put("twenty", 20);
        wordToInt.put("thirty",30);
        wordToInt.put("forty",40);
        wordToInt.put("fifty",50);

        linkWordToInt.put("zero", 0);
        linkWordToInt.put("o",0);
        linkWordToInt.put("twenty", 20);
        linkWordToInt.put("thirty",30);
        linkWordToInt.put("forty",40);
        linkWordToInt.put("fifty",50);
    }
}
