package db.pitt.chatbotbackendsupport.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static db.pitt.chatbotbackendsupport.Util.ReadFile.readTestCase;

@Slf4j
@SpringBootTest
public class SUTimeTest {
    @Autowired
    private SUTime suTime;

    String Success_Input_Path ="src/test/java/db/pitt/chatbotbackendsupport/TestFile/Time Extraction- Success.txt";
    List<String> Success_Input = readTestCase(Success_Input_Path);

    @Test
    public void NoAmbiguityCaseTest(){
//        for (String input: Success_Input) {
//            System.out.println(input);
//            System.out.println(suTime.SUTimeExtract(input));
//            System.out.println("---------");
//        }
        System.out.println(suTime.SUTimeExtract("I will arrive at 12 pm"));
    }
}
