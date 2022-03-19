import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import java.awt.Point;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

public class Main {

  protected boolean enabled = true;

  private String token = "ODQyMTQ4MjIwMDM3NzU5MDI3.YJxFpg.MZ6cwdzhW-6ezPQPszyqco6YMaE"; // DO NOT SHARE!

  protected DiscordApi bot = new DiscordApiBuilder().setToken(token).login().join();

  public Timer timer = new Timer();

  protected static ArrayList<JSONObject> cachedOrders = new ArrayList<>();

  protected int ordersProcessed = 0;

  protected EnterOrderTask enterOrderTask = new EnterOrderTask(this);

  public void testOrder(JSONObject order){
    enterOrderTask.enterOrder(true, order);
  }

  public Main(){
    // First we init the Discord bot status.
    bot.updateActivity(" orders: " + ordersProcessed);
    // Next, we schedule a repeating task to check the online ordering server.
    timer.scheduleAtFixedRate(new CheckOrderTask(this), 3000, 10000);
    timer.scheduleAtFixedRate(enterOrderTask, 3000, 25000);

    bot.addMessageCreateListener(event -> {
      if (!event.getMessageAuthor().isBotUser()){
        if (event.getMessageContent().equals("!testrun")){
          // Obtain the file.
          try {
            InputStream stream = event.getMessageAttachments().get(0).downloadAsInputStream();
            String data = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            ProcessOrder.testProcess(this, data);
          }
          catch(IOException e){
            e.printStackTrace();
          }
        }
        else if (event.getMessageContent().equals("!toggle")){
          /**
           * Toggles the online ordering system on or off as needed.
           */
          this.enabled = !this.enabled;
          event.getChannel().sendMessage("Online ordering system turned **" + (this.enabled ? "on" : "off") + "**.");
        }
        else if (event.getMessageContent().equals("!help")){
          event.getChannel().sendMessage("**Commands:**\n!testrun <attachment> - Runs an array of orders through the system.\n!cached - Get # of cached orders.\n!addTo <sequence> - Program inputs for each required item.");
        }
        else if (event.getMessageContent().equals("!status")){
          /**
           * Gets the status (how many orders done, in the cache).
           */
          event.getChannel().sendMessage("**" + ordersProcessed + "** orders have been processed.");
          event.getChannel().sendMessage("**" + cachedOrders.size() + "** orders are in the cache.");
        }
        else if (event.getMessageContent().equals("!test")){
          /**
           * Test enter an order sent as an attachment in Discord.
           */
          try {
            InputStream stream = event.getMessageAttachments().get(0).downloadAsInputStream();
            String data = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            ProcessOrder.testProcess(this, data);
          }
          catch(IOException e){
            e.printStackTrace();
          }
        }
        else if (event.getMessageContent().contains("!snap")){
          /**
           * Snaps the top left corner of a page, for identifying which menu we are on and 
           * how to navigate back to the takeout menu.
           */
          String part = event.getMessageContent().substring(6);
          // Create folder if not exist.
          File db = new File("db/");
          if (!db.exists())
            db.mkdirs();
          File image = new File("db/" + part + ".png");
          // Take picture
          Rectangle rect = new Rectangle(4, 49, 50, 30);
          try{
            Robot r = new Robot();
            BufferedImage i = r.createScreenCapture(rect);
            ImageIO.write(i, "png", image);
            event.getChannel().sendMessage("Image saved as " + part + ".png.");
          }
          catch(Exception e){
            e.printStackTrace();
          }
        }
        else if (event.getMessageContent().equals("!autoprogram")){
          try {
            event.getChannel().sendMessage("Auto-programming starting in 5 seconds.");
            TimeUnit.SECONDS.sleep(5);  
            InputStream stream = event.getMessageAttachments().get(0).downloadAsInputStream();
            String data = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            JSONObject menu = new JSONObject(data);
            JSONArray items = menu.getJSONArray("items");
            for (int i = 0; i < items.length(); i++){
              JSONObject item = items.getJSONObject(i);
              String fullItemName = item.getString("code") + ". " + item.getString("name");
              // Delete existing file if it is there.
              File sequence = new File("db/" + fullItemName + ".txt");
              if (sequence.exists())
                sequence.delete();
              String d = "";
              // Register click for category.
              event.getChannel().sendMessage("Registering **" + fullItemName + "**:");
              event.getChannel().sendMessage("Please move mouse to the category button. Locking in 3 seconds.");
              TimeUnit.SECONDS.sleep(3);
              Point p = MouseInfo.getPointerInfo().getLocation();
              // Register click for item.
              d += "" + ((int)p.getX()) + "," + ((int)p.getY()) + ";";
              event.getChannel().sendMessage("Please move mouse to the item button. Locking in 3 seconds.");
              TimeUnit.SECONDS.sleep(3);
              p = MouseInfo.getPointerInfo().getLocation();
              d += "" + ((int)p.getX()) + "," + ((int)p.getY()) + ";";
              FileWriter write = new FileWriter(sequence);
              write.write(d);
              write.close();
              event.getChannel().sendMessage("Wrote `[ " + d + " ]` to sequence `" + fullItemName + ".txt`.");
            }
          }
          catch(Exception e){
            e.printStackTrace();
          }
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