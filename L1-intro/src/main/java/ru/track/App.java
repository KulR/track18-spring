package ru.track;

import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.apache.http.HttpResponse;

/**
 * TASK:
 * POST request to  https://guarded-mesa-31536.herokuapp.com/track
 * fields: name,github,email
 *
 * LIB: http://unirest.io/java.html
 *
 *
 */
public class App {

    public static final String URL = "http://guarded-mesa-31536.herokuapp.com/track";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_GITHUB = "github";
    public static final String FIELD_EMAIL = "email";

    public static void main(String[] args) throws Exception {
       // 1) Use Unirest.post()
        // 2) Get response .asJson()
        // 3) Get json body and JsonObject
        // 4) Get field "success" from JsonObject
        com.mashape.unirest.http.HttpResponse<JsonNode> jsonNodeHttpResponse = Unirest.post(URL)
                .field(FIELD_NAME, "Roman")
                .field(FIELD_EMAIL, "kulikov.rn@phystech.edu")
                .field(FIELD_GITHUB, "github.com/KulR")
                .asJson();

        boolean success = false;

        success = (Boolean)jsonNodeHttpResponse.getBody().getObject().get("success");
        System.out.println(success);
    }

}
