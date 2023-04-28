package db.pitt.chatbotbackendsupport.controller;

import db.pitt.chatbotbackendsupport.entity.Response;
import db.pitt.chatbotbackendsupport.service.ExtractTime;
import db.pitt.chatbotbackendsupport.service.SUTime;
import db.pitt.chatbotbackendsupport.service.YesOrNoModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/pitt/db/support")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class Controller {
    private final ExtractTime extractTimeService;

    private final YesOrNoModel yesOrNoModel;

    private final SUTime suTimeService;

    public Controller(ExtractTime extractTimeService, YesOrNoModel yesOrNoModel, SUTime suTime) {
        this.extractTimeService = extractTimeService;
        this.yesOrNoModel = yesOrNoModel;
        this.suTimeService = suTime;
    }

    @GetMapping("/re/{input}")
    public Response RE(@PathVariable("input") String input) {
        log.info("Regular expression input: " +input);
        Response response = extractTimeService.RE(input);
        if(response.code == 0){
            log.info("RE Success");
        } else if (response.code == 1) {
            log.info("RE Ambiguity");
        }else{
            log.info("RE Fail");
        }
        return response;
    }

    @GetMapping("/sut/{input}")
    public String SUT(@PathVariable("input") String input){
        return suTimeService.SUTimeExtract(input);
    }


    /**
     *  And departure 1 <（earlier) departure 2.
     */
    @GetMapping("ambiguity/{arrivalHour}/{arrivalMin}/{departureHour1}/{departureMin1}/{departureHour2}/{departureMin2}")
    public Response DepartureAmbiguity(          @PathVariable("arrivalHour") int arrivalHour,
                                                 @PathVariable("arrivalMin") int arrivalMin,
                                                 @PathVariable("departureHour1") int departureHour1,
                                                 @PathVariable("departureMin1") int departureMin1,
                                                 @PathVariable("departureHour2")int departureHour2,
                                                 @PathVariable("departureMin2") int departureMin2){
        log.info("Resolve Departure Ambiguity "+"Arrival Time "+arrivalHour+":" +arrivalMin+
                " Departure Time(Ambiguity) "+departureHour1+":"+departureMin1+" , "+departureHour2+":"+departureMin2);
        Response response = extractTimeService.DepartureAmbiguity(departureHour1, departureMin1,departureHour2, departureMin2, arrivalHour, arrivalMin);
        if(response.code == 7){
            log.info("DA Ambiguity Resolved ");
        }else {
            log.info("DA Ambiguity doesn't Resolved ");
        }
        return response;
    }

    @GetMapping("/yesmodel/{input}")
    public String YesOrNo(@PathVariable("input") String input) {
        log.info("YesOrNoModel input: "+input);
        String intention = yesOrNoModel.RequestYesOrNo(input);
        log.info("Detect intention: "+intention);
        return intention;
    }

    @GetMapping("/matchexpectation/{input}")
    public void SaveExpectation(@PathVariable("input") String input){
        log.info("User expectation: "+ input);
    }




}
