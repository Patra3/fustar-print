import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Timer;
import java.awt.Point;
import java.awt.MouseInfo;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.json.JSONObject;

public class Main {

  private String token = "ODQyMTQ4MjIwMDM3NzU5MDI3.YJxFpg.MZ6cwdzhW-6ezPQPszyqco6YMaE"; // DO NOT SHARE!

  protected DiscordApi bot = new DiscordApiBuilder().setToken(token).login().join();

  public Timer timer = new Timer();

  protected static ArrayList<JSONObject> cachedOrders = new ArrayList<>();

  protected int ordersProcessed = 0;

  public Main(){
    // First we init the Discord bot status.
    bot.updateActivity(" orders: " + ordersProcessed);
    // Next, we schedule a repeating task to check the online ordering server.
    timer.scheduleAtFixedRate(new CheckOrderTask(), 3000, 10000);
    timer.scheduleAtFixedRate(new EnterOrderTask(this), 3000, 25000);

    bot.addMessageCreateListener(event -> {
      if (!event.getMessageAuthor().isBotUser()){
        if (event.getMessageContent().equals("!testrun")){
          // Obtain the file.
          try {
            InputStream stream = event.getMessageAttachments().get(0).downloadAsInputStream();
            String data = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            ProcessOrder.process(data);
          }
          catch(IOException e){
            e.printStackTrace();
          }
        }
        else if (event.getMessageContent().equals("!help")){
          event.getChannel().sendMessage("**Commands:**\n!testrun <attachment> - Runs an array of orders through the system.\n!cached - Get # of cached orders.\n!addTo <sequence> - Program inputs for each required item.");
        }
        else if (event.getMessageContent().equals("!cached")){
          event.getChannel().sendMessage(ordersProcessed + " orders have been processed.");
        }
        else if (event.getMessageContent().contains("!program")){
          // Parse message.
          String part = event.getMessageContent().substring(9);
          // Create folder if not exist.
          File db = new File("db/");
          if (!db.exists())
            db.mkdirs();
          // Check if sequence exists.
          File sequence = new File("db/" + part + ".txt");
          String data = "";
          try {
            if (sequence.exists())
              data = new String(Files.readAllBytes(Paths.get(sequence.getAbsolutePath())));
            Point p = MouseInfo.getPointerInfo().getLocation();
            data += "" + ((int)p.getX()) + "," + ((int)p.getY()) + ";";
            // Write to the file the new data.
            sequence.delete();
            FileWriter write = new FileWriter(sequence);
            write.write(data);
            write.close();
            event.getChannel().sendMessage("Wrote `(" + (int)p.getX() + ',' + (int)p.getY() + ")` to sequence `" + part + "`.");
          }
          catch(IOException e){
            e.printStackTrace();
          }
        }
      }
    });
  }

  protected static ArrayList<ArrayList<Integer>> getCoordinates(String sequence){
    File s = new File("db/" + sequence + ".txt");
    String data = "";
    try {
      if (s.exists())
        data = new String(Files.readAllBytes(Paths.get(s.getAbsolutePath())));
      if (data.length() > 0){
        ArrayList<ArrayList<Integer>> ret = new ArrayList<>(); // outer layer, row
        // Parse the data.
        String[] parts = data.split(";");
        for (String part : parts){
          if (part.length() > 0){
            String[] xy = part.split(",");
            ArrayList<Integer> inner = new ArrayList<>();
            inner.add(Integer.parseInt(xy[0]));
            inner.add(Integer.parseInt(xy[1]));
            ret.add(inner);
          }
        }
        return ret;
      }
      else{
        return null;
      }
    }
    catch(IOException e){
      e.printStackTrace();
      return null;
    }
  }

  public static void main(String[] args){
    new Main();
  }
}