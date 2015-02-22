import com.mashape.unirest.http.JsonNode;
import java.io.File;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;



public class Main {
    public static void main(String[] args) {
    try {
            HttpResponse<JsonNode> request = Unirest
                    .post("https://camfind.p.mashape.com/image_requests")
                    .header("X-Mashape-Authorization",
                            "A0MYOpCsdfasdgadfadafgdj7vsdfe")
                    .field("image_request[image]",
                            new File("light-pole.jpg")).asJson();

            String body =  request.getBody().toString(); 
            System.out.println(body);
        } catch (UnirestException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}