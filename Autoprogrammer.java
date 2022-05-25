import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

import java.awt.Point;
import java.awt.MouseInfo;

import org.javacord.api.event.message.MessageCreateEvent;
import org.json.JSONArray;
import org.json.JSONObject;


public class Autoprogrammer implements Runnable {

  private MessageCreateEvent event;

  public Autoprogrammer(Main main, MessageCreateEvent event) {
    this.event = event;
  }

  @Override
  public void run(){
    try {
      Logger.log("Auto-programming starting in 5 seconds.");
      TimeUnit.SECONDS.sleep(5);  
      InputStream stream = event.getMessageAttachments().get(0).downloadAsInputStream();
      String data = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
      JSONObject menu = new JSONObject(data);
      JSONArray items = menu.getJSONArray("items");
      int currentType = -1;
      String typeLoc = "";
      for (int i = 0; i < items.length(); i++){
        JSONObject item = items.getJSONObject(i);
        String fullItemName = item.getString("code") + ". " + item.getString("name");
        // Delete existing file if it is there.

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(StandardCharsets.UTF_8.encode(fullItemName));
        String rt = String.format("%032x", new BigInteger(1, md5.digest()));
        
        File sequence = new File("db/" + rt + ".txt");
        if (sequence.exists())
          sequence.delete();
        String d = "";
        Point p = MouseInfo.getPointerInfo().getLocation();
        if (currentType != item.getInt("type")){
          currentType = item.getInt("type");
          Logger.log("Registering **" + item.getString("code") + "**:");
          // Register click for category.
          Logger.log("Please move mouse to the category button. Locking in 5 seconds.");
          TimeUnit.SECONDS.sleep(5);
          typeLoc = "" + ((int)p.getX()) + "," + ((int)p.getY()) + ";";
        }
        Logger.log("Registering **" + fullItemName + "**:");
        // Register click for item.
        Logger.log("Please move mouse to the item button. Locking in 5 seconds.");
        TimeUnit.SECONDS.sleep(5);
        p = MouseInfo.getPointerInfo().getLocation();
        d += typeLoc;
        d += "" + ((int)p.getX()) + "," + ((int)p.getY()) + ";";
        FileWriter write = new FileWriter(sequence);
        write.write(d);
        write.close();
        Logger.log("Wrote `[ " + d + " ]` to sequence `" + fullItemName + ".txt`.");
      }
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
  
}