package db.pitt.chatbotbackendsupport.service;

import lombok.extern.slf4j.Slf4j;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static db.pitt.chatbotbackendsupport.Util.ReadFile.readTestCase;


@Slf4j
@SpringBootTest
public class ExtractTimeTest {

    @Autowired
    private YesOrNoModel yesOrNoModel;
    @Test
    public void RequestYesOrNo(){
        System.out.println(yesOrNoModel.RequestYesOrNo("I think so"));
        System.out.println(yesOrNoModel.RequestYesOrNo("I would like to"));
    }

    @Autowired
    private ExtractTime extractTime;

    String Ambiguity_Input_Path ="src/test/java/db/pitt/chatbotbackendsupport/TestFile/Time Extraction- Ambiguity.txt";
    List<String> Ambiguity_Input = readTestCase(Ambiguity_Input_Path);

    @Test
    public void AmbiguityTest(){
        for (String input: Ambiguity_Input) {
           // System.out.println(input);
            System.out.println(extractTime.RE(input));
         //   System.out.println("---------");
        }
    }

    String Success_Input_Path ="src/test/java/db/pitt/chatbotbackendsupport/TestFile/Time Extraction- Success.txt";
    List<String> Success_Input = readTestCase(Success_Input_Path);

    @Test
    public void SuccessTest(){
        for (String input: Success_Input) {
            //System.out.println(input);
            System.out.println(extractTime.RE(input));
            //System.out.println("---------");
        }
    }

    String Fail_Input_Path ="src/test/java/db/pitt/chatbotbackendsupport/TestFile/Time Extraction- Fail.txt";
    List<String> Fail_Input = readTestCase(Fail_Input_Path);
    @Test
    public void FailTest(){
        for (String input: Fail_Input ) {
          //  System.out.println(input);
            System.out.println(extractTime.RE(input));
           // System.out.println("---------");
        }
    }

    String[] Fail_inputs = new String[]{
            //  " 07\uD83D\uDE32 0",




            "310 pm",
            "400pm",
            " i will arrive by 4 this afternoon",
            "呃 for PM",
            "for PM",
            //   "12\uD83D\uDE1Bm",
            //   "14\uD83D\uDE320",
            "in 10 minutes",
            "now",
            "10 in the morning tomorrow",
            "15 minutes after 5",
            "10 minutes prior to the arrival",
            "two to two",
            "in two minutes",
            "10+2 pm",
            " 8*00 pm",
            "8&00 pm",
            "1!00 am",
            "30 minutes from now",
            "I need to be there in 30 minutes",
            "quarter after noon",
            " quarter after three",
            "twenty-five "
    };
}
