package db.pitt.chatbotbackendsupport.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
public class ReadFile {
    public static void main(String[] args) {
        String path ="src/test/java/db/pitt/chatbotbackendsupport/TestFile/Time Extraction- Ambiguity.txt";
        readTestCase(path);
    }

    public static List<String> readTestCase(String filePath){
        List<String> cases = new LinkedList<>();
        try {
            File file = new File(filePath);
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String data = scanner.nextLine();
                cases.add(data);
                System.out.println(data);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.err.println("File not found.");
            return cases;
        }
        return cases;
    }
}
