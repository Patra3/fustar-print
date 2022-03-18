import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProcessOrder {
  
  /**
   * Processes online orders based on query after CheckOrderTask.
   * @param data
   */
  public static void process(String data){
    JSONArray orders = new JSONArray(data);
    // Create a log history locally and on Discord.
    for (int i = 0; i < orders.length(); i++){
      JSONObject order = orders.getJSONObject(i);
      File file = new File("orders/");
      // Check if backup folder exists.
      if (!file.exists())
        file.mkdirs();
      // Write as file into the folder.
      Path f = Paths.get("orders/" + order.getString("name") + ".txt");
      try {
        Files.write(f, order.toString().getBytes());
      }
      catch(IOException e){
        e.printStackTrace();
      }
    }
  }
}
