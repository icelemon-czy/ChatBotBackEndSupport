package db.pitt.chatbotbackendsupport.controller;

import db.pitt.chatbotbackendsupport.entity.Response;
import db.pitt.chatbotbackendsupport.service.ExtractTime;
import db.pitt.chatbotbackendsupport.service.YesOrNoModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/pitt/db/support")
public class Controller {
    private final ExtractTime extractTimeService;

    private final YesOrNoModel yesOrNoModel;

    public Controller(ExtractTime extractTimeService, YesOrNoModel yesOrNoModel) {
        this.extractTimeService = extractTimeService;
        this.yesOrNoModel = yesOrNoModel;
    }

    @GetMapping("/re/{input}")
    public Response RE(@PathVariable("input") String input) {
        return extractTimeService.RE(input);
    }


    @GetMapping("ambiguity/{arrivalHour}/{arrivalMin}/{departureHour}/{departureMin}")
    public Response DepartureAmbiguity(@PathVariable("departureHour") int departureHour,
                                       @PathVariable("departureMin") int departureMin,
                                       @PathVariable("arrivalHour") int arrivalHour,
                                       @PathVariable("arrivalMin") int arrivalMin){
        return extractTimeService.DepartureAmbiguity(departureHour, departureMin, arrivalHour, arrivalMin);
    }

    @GetMapping("/yesmodel/{input}")
    public String YesOrNo(@PathVariable("input") String input) {
        return yesOrNoModel.RequestYesOrNo(input);
    }




}
