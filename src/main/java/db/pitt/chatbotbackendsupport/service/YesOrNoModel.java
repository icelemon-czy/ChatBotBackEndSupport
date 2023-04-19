package db.pitt.chatbotbackendsupport.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

@Slf4j
@Service
public class YesOrNoModel {
    public String RequestYesOrNo(String input) {
        URL url = null;
        try {
            url = new URL("https://capriointentionclassification.cognitiveservices.azure.com/language/:analyze-conversations?api-version=2022-10-01-preview");
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("POST");

            httpConn.setRequestProperty("Ocp-Apim-Subscription-Key", "1e8c691965694b16b2b109f7f1973fa2");
            httpConn.setRequestProperty("Apim-Request-Id", "4ffcac1c-b2fc-48ba-bd6d-b69d9942995a");
            httpConn.setRequestProperty("Content-Type", "application/json");

            httpConn.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
            writer.write("{\"kind\":\"Conversation\",\"analysisInput\":{\"conversationItem\":{\"id\":\"1\",\"text\":\" "+input+" \",\"participantId\":\"1\"}},\"parameters\":{\"projectName\":\"IntentionClassification\",\"verbose\":true,\"deploymentName\":\"YesNoModel\",\"stringIndexType\":\"TextElement_V8\"}}");
            writer.flush();
            writer.close();
            httpConn.getOutputStream().close();

            InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                    ? httpConn.getInputStream()
                    : httpConn.getErrorStream();
            Scanner s = new Scanner(responseStream).useDelimiter("\\A");
            String response = s.hasNext() ? s.next() : "";
            JSONObject intent = new JSONObject(response).getJSONObject("result").getJSONObject("prediction");
            return  (String) intent.get("topIntent");
        } catch (Exception e) {
            return "";
        }
    }
}
