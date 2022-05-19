
import java.net.URLEncoder;

import org.restlet.resource.ClientResource;

public class Logger {
  

  public static void log(String message){
    try {
      new ClientResource("https://fustarbuffet.com/pw35/zh385920394hf/" + URLEncoder.encode(message, "UTF-8")).get();
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  
}
