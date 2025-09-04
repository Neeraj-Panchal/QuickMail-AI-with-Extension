package com.email.email_writer_np.app;

import com.email.email_writer_np.DTO.EmailRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class EmailGeneraterService {

    //It is used to make HTTP requests to external APIs or services in a more efficient way compared to RestTemplate.
    private final WebClient
            webClient;

    //@Value annotation is used to inject the values from the application.properties file
    //gemini.api.url | gemini.api.key ARE PLACE HOLDERS THAT TELL SPRINGBOOT TO FETCH THE VALUE FROM APPLICATION.PROPERTIES FILE
    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public EmailGeneraterService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    //----GENERATE EMAIL REPLY----

    public String generateEmailReply(EmailRequest emailRequest) {

        //---BUILD THE PROMPT---------------[1].
        String prompt = buildPrompt(emailRequest);

        //AFTER GETTING THE PROMPT FROM THE buildPrompt METHOD THEN -- CRAFT A REQUEST

        //---CRAFT A REQUEST------------------------[3]
        //TAKING Map<String, Object>
        //HERE key is String (contents,parts,text) and Object is used here as the value and this gives the flexibility to store the different type of data in the single line of Map
        Map<String, Object> requestBody = Map.of(
                //new Object[] ==> [] is used because the Body of the JSON in the request is defined as the array of values
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );


        //QUES : WHY WE CRAFTING THE REQUEST IT'S BECAUSE AFTER GETTING THE PROMPT WE HAVE TO MAKE IT IN THE FORMAT WHICH IS EXCEPTABLE BY THE GEMINI API AND TO GET THE DESIRED OUTPUT PROPERLY

        //DO REQUEST AND GET RESPONSE
        String response = webClient.post()                  //WebClient is a reactive HTTP client used in Spring Boot to make API calls asynchronously.
                .uri(geminiApiUrl + geminiApiKey)       //values injected from application.properties | This keeps the API key secure and hidden.
                .header("Content-Type", "application/json")     //this tells about the request that the header is in JSON format
                .bodyValue(requestBody)     //Request body passed to the gemini api
                .retrieve()                 //This sends the request to the API. It retrieves the response body asynchronously.
                .bodyToMono(String.class)   //it converts response to String and (Mono )means single iy represent the single value
                .block();                   //block() converts the asynchronous request into a synchronous one.


//        🔹 Short Story Form (Easy Memory Trick 🧠)

//        post() → “bhai ek POST request banani hai”
//        uri(...) → “kahan bhejni hai”
//        header(...) → “request ka format kya hai”
//        bodyValue(...) → “ye lo mera JSON data”
//        retrieve() → “chala request, response lao”
//        bodyToMono(String.class) → “response ko String banado”    ||  Mono → single result    |     Flux → multiple results (stream)
//        block() → “wait karo jab tak pura jawab aa na jaye”



        //extracting this because the response is in the tree like format so it will have to extracted and give to the user

//        ---------------[5]-------------------
        return extractResponseContent(response);
    }

//    -------[4]----//EXTRACT RESPONSE AND RETURN--------------

    private String extractResponseContent(String response) {
        try {
            //ObjectMapper convert JSON String to java object or vice versa
            //readTree(response) converts the JSON string into a JsonNode tree structure which help to extract the root node.
            //JsonNode allows easy navigation of nested JSON objects.

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);
            return rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();      //CONVERTED THE EXTRACTED JSON INTO THE PLAIN TEXT
            //here we got the text written in the response to parse it to the user/client
        } catch (Exception e) {
            return "Error Processing Request : " + e.getMessage();
        }
}

    //[2]-------------
    //BUILDING PROMPT FOR THE "GEMINI"
    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt = new StringBuilder();

        //GIVING A DEFAULT PROMPT WHICH IS ADDED TO EVERY PROMPT WITH ORIGINAL
        prompt.append("Generate a professional email reply for the following email content, Please don't generate a subject line ");

        //IF TONE EXISTS THEN GIVE THE TONE TO THE PROMPT
        if(emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()){
            prompt.append("Use a ").append(emailRequest.getTone()).append("tone. ");
        }

        //ADDING DEFAULT PROMPT TO ORIGINAL PROMPT
        prompt.append("\nOriginal email: \n").append(emailRequest.getEmailContent());

        return prompt.toString();
    }
}
