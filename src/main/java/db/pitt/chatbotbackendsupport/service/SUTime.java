package db.pitt.chatbotbackendsupport.service;

import db.pitt.chatbotbackendsupport.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.time.*;
import edu.stanford.nlp.util.CoreMap;

@Slf4j
@Service
public class SUTime {

    public String SUTimeExtract(String input) {
        Properties props = new Properties();
        AnnotationPipeline pipeline = new AnnotationPipeline();
        pipeline.addAnnotator(new TokenizerAnnotator(false));
        pipeline.addAnnotator(new WordsToSentencesAnnotator(false));
        pipeline.addAnnotator(new POSTaggerAnnotator(false));
        pipeline.addAnnotator(new TimeAnnotator("sutime", props));

        StringBuilder sb = new StringBuilder();


        Annotation annotation = new Annotation(input);
        // annotation.set(CoreAnnotations.DocDateAnnotation.class, "2013-07-14");
        annotation.set(CoreAnnotations.DocDateAnnotation.class, Util.getNowYYMMDDHHMM());
        pipeline.annotate(annotation);
        System.out.println(annotation.get(CoreAnnotations.TextAnnotation.class));
        List<CoreMap> timexAnnsAll = annotation.get(TimeAnnotations.TimexAnnotations.class);

        for (CoreMap cm : timexAnnsAll) {
            List<CoreLabel> tokens = cm.get(CoreAnnotations.TokensAnnotation.class);
            System.out.println(cm+ " --> "+ cm.get(TimeExpression.Annotation.class).getTemporal());
            if(sb.length() !=0){
                sb.append(" , ");
            }
            sb.append(cm+ " --> "+ cm.get(TimeExpression.Annotation.class).getTemporal());
        }
        if(timexAnnsAll.isEmpty()){
            return "No Time is extracted!";
        }
        return sb.toString();

    }
}
