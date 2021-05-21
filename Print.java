import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.json.JSONArray;
import org.json.JSONObject;

public class Print{
  
  private JSONObject menu;
  private DiscordApi bot = new DiscordApiBuilder().setToken("ODQyMTQ4MjIwMDM3NzU5MDI3.YJxFpg.Z3rBemeRV3vsiUNridUqtwAtwXE").login().join();
  private boolean registering = false;
  private static MouseListener listener = new MouseListener();
  private JSONObject registeringItem;
  private String loc = System.getProperty("user.home") + "/Desktop/";

  Print() {
    try {
      // Register restaurant menu data.
      File payload = new File(loc + "payload.txt");
      Scanner scanner = new Scanner(payload);
      String data = "";
      while (scanner.hasNextLine()) {
        data += scanner.nextLine();
      }
      menu = new JSONObject(data);
      scanner.close();

      // If not exist, create position db folder.
      File folder = new File(loc + "positions/");
      if (!folder.exists())
        folder.mkdirs();

      // Register Discord bot listeners.
      bot.addMessageCreateListener(event -> {
        //{"code":"L2","price":[5.45],"name":"Chicken Chow Mein","type":0}
        if (registering && !event.getMessageAuthor().isBotUser()){
          // Index number;
          try {
            registeringItem = menu.getJSONArray("items").getJSONObject(Integer.parseInt(event.getMessageContent()));  
            event.getChannel().sendMessage("Registering clicks to navigate to item `" + registeringItem.getString("code") + ". " + registeringItem.getString("name") + "`. Message `!s` to stop.");
            registering = false;
            listener.listen();
          }
          catch(Exception e){
            e.printStackTrace();
            registering = false;
            event.getChannel().sendMessage("Invalid index. Please run `!r` again.");
          }
        }
        if (event.getMessageContent().equalsIgnoreCase("!r")) {
          // Get the last item not registered yet.
          event.getChannel().sendMessage("Please enter the `item[]` index you wish to register clicks for.");
          registering = true;
        }
        else if (event.getMessageContent().equalsIgnoreCase("!s")) {
          // Save clickCoords to a file.
          String pos = loc + "positions/" + registeringItem.getString("name") + ".txt";
          File saveTo = new File(pos);
          // Delete if exists.
          if (saveTo.exists())
            saveTo.delete();
          
          try {
            FileWriter writer = new FileWriter(pos);
            writer.write(listener.getRecorded());
            writer.close();
            event.getChannel().sendMessage("Registration complete.");
            listener.stopListen();
            listener.erase();
            registering = false;
            registeringItem = null;
          }
          catch(Exception e){
            e.printStackTrace();
          }
          
        }
      });

      System.out.println(getAllItemsInCategory(3));
    }
    catch(FileNotFoundException e){
      e.printStackTrace();
    }
  }

  /**
   * Executes a click on an item.
   * @param code i.e.  C4
   * @param type i.e.  Lunch Special = 0
   */
  public void clickItem(String code, int type){
    // TO-DO: Enter edge cases manually here.


    // 
  }

  /**
   * Get all items in a category (i.e. All items in Lunch Special, etc.)
   * as an ArrayList of JSONObjects.
   * @param type
   * @return ArrayList<JSONObject>
   */
  public ArrayList<JSONObject> getAllItemsInCategory(int type){
    ArrayList<JSONObject> categoryItems = new ArrayList<>();
    JSONArray allItems = menu.getJSONArray("items");
    allItems.forEach(i -> {
      JSONObject item = (JSONObject)i;
      if (item.getInt("type") == type){
        categoryItems.add(item);
      }
    });
    return categoryItems;
  }

  /**
   * Returns the grid location of the item given category "type".
   * This does not account for "misplaced" items, so do checks elsewhere for that.
   * @param type
   * @return Integer grid location (or -1 if not found)
   */
  public int locationWithinCategory(String code, int type){
    ArrayList<JSONObject> categoryItems = getAllItemsInCategory(type);
    for (int i = 0; i < categoryItems.size(); i++){
      if (categoryItems.get(i).getString("code").equals(code))
        return i;
    }
    return -1;
  }

  public static void main(String[] args){
    new Print();
    // Register global listener for jnativehook.
    Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
    logger.setLevel(Level.OFF);
    try {
      GlobalScreen.registerNativeHook();
      GlobalScreen.addNativeMouseListener(listener);
    }
    catch(NativeHookException e){
      e.printStackTrace();
    }
  }
}
