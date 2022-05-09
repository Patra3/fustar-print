import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;

public class Logger {
  

  public static void log(String message){
    try {
      URL url = new URL("https://fustarbuffet.com/pw35/zh385920394hf/");
      HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      con.setRequestProperty("Content-Type", "application/json; utf-8");
      con.setRequestProperty("Accept", "application/json");
      con.setDoOutput(true);
      JSONArray m = new JSONArray();
      m.put(message);
      try(OutputStream os = con.getOutputStream()) {
        byte[] input = m.toString().getBytes("utf-8");
        os.write(input, 0, input.length);			
        os.close();
      }
      con.disconnect();
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  
}
