package db.pitt.chatbotbackendsupport.service;

import lombok.extern.slf4j.Slf4j;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


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
    String[] Ambiguity_Input = new String[]{
            "2:35",
            "It's twenty-five to three",
            "It's twenty-five past three",
            "5 past 12",
            "It's two thirty-four",
            "It's two thirty",
            "It's eight O-five",
            "It's two o'clock",
            " 2 :00",
            " 2: 00",
            "2 : 00",
            "12",
    };

    @Test
    public void AmbiguityTest(){
        for (String input: Ambiguity_Input) {
            System.out.println(extractTime.RE(input));
        }
    }

    String[] Success_inputs = new String[]{
            "twelve am",
            "twelve pm",
            "12:35 am",
            "12:35 pm",
            "2:35 am",
            "2:35 pm",
            "two pm",
            "It's thirteen o'clock",
            "It's twenty-three o'clock",
            "midday",
            "noon",
            "midnight",
            "2pm",
            "13 43"
    };
    @Test
    public void SuccessTest(){
        for (String input: Success_inputs) {
            System.out.println(extractTime.RE(input));
        }
    }

    String[] Conflict_inputs = new String[]{
            "13 am",
            "14 pm",
            "0 am",
    };

    @Test
    public void ConflictTest(){
        for (String input: Conflict_inputs ) {
            System.out.println(extractTime.RE(input));
        }
    }

    String[] Fail_inputs = new String[]{
            "5 to 13",
            "5 past 13",
            "The weather is good"
    };

    @Test
    public void FailTest(){
        for (String input: Fail_inputs  ) {
            System.out.println(extractTime.RE(input));
        }
    }
}
