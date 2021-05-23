import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Robot;
import java.awt.event.InputEvent;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.json.JSONArray;
import org.json.JSONObject;

public class Print {
  
  private JSONObject menu;
  private DiscordApi bot = new DiscordApiBuilder().setToken("ODQyMTQ4MjIwMDM3NzU5MDI3.YJxFpg.Z3rBemeRV3vsiUNridUqtwAtwXE").login().join();
  private boolean registering = false;
  private static MouseListener listener = new MouseListener();
  private String registeringItem;
  private String loc = System.getProperty("user.home") + "/Desktop/";
  private JSONObject fx;

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
        if (registering && !event.getMessageAuthor().isBotUser()){
          // Index number;
          try {
            registeringItem = event.getMessageContent();
            event.getChannel().sendMessage("Registering clicks to navigate to item `" + registeringItem + "`. Message `!s` to stop.");
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
          event.getChannel().sendMessage("Please enter the item you wish to register clicks for.");
          registering = true;
        }
        else if (event.getMessageContent().equalsIgnoreCase("!s")) {
          // Save clickCoords to a file.
          String pos = loc + "positions/" + registeringItem + ".txt";
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
        else{
          String[] messageParts = event.getMessageContent().split(" ");
          if (messageParts[0].equalsIgnoreCase("!click")){
            // Item is mp[1]
            // We need to find the type first.
            JSONObject i = getMenuItem(messageParts[1]);
            clickItem(messageParts[1], i.getInt("type"), "");
            event.getChannel().sendMessage("Click sent!");
          }
        }
      });
    }
    catch(FileNotFoundException e){
      e.printStackTrace();
    }
  }

  public JSONObject getMenuItem(String code){
    menu.getJSONArray("items").forEach(item -> {
      JSONObject i = (JSONObject)item;
      if (i.getString("code").equals(code))
        fx = i;
    });
    return this.fx;
  }

  /**
   * Executes a click on an item.
   * @param code i.e.  C4
   * @param type i.e.  Lunch Special = 0
   * @param choice i.e. Pork, chicken
   */
  public void clickItem(String code, int type, String choice){

    // Click on the category first.
    click("type_" + type);

    // TO-DO: Enter edge cases manually here.


    /*
    List of Hard codes needed:
    - Lunch Special [done]
    - Special Combo [done]
    - Appetizers
    - Lo Mein
    - Chow Mei Fun
    - Chicken
    - Beef
    - Shrimp
    - Vegetables
    - Chef Special
    - Side Order
    */

    if (type == 0 || type == 12){
      // Lunch Special && Special Combo hard code
      JSONObject ar = getMenuItem(code);
      String name = code + ". " + ar.getString("name");
      if (ar.has("choice")){
        name += "_" + choice;
      }
      click(name);
    }
    else if (type == 1){
      // Appetizer hard code
    }

    else{

      /*
      
      [1] [2] [3] [4]
      [5] [6] [7] [8]
      ... 
      
      */
      // Get the exact grid position.
      int position = locationWithinCategory(code, type);

      // Click on the item.
      click("grid_" + (position + 1));
    }
  }

  /**
   * Mouse click at point.
   */
  public void click(int x, int y){
    try{
      Robot bot = new Robot();
      bot.mouseMove(x, y);
      bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
      bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  /**
   * Deliver a click to an item.
   * @param access
   * @return
   */
  public void click(String access){
    System.out.println(access);
    // [    [0,0] ,  [1,0]  , [2,0]    ]
    ArrayList<ArrayList<Integer>> t = new ArrayList<>();
    try{
      File payload = new File(loc + "positions/" + access + ".txt");
      Scanner scanner = new Scanner(payload);
      String data = "";
      while (scanner.hasNextLine()) {
        data += scanner.nextLine();
      }
      scanner.close();
      String[] datum = data.split(";");
      for (String entry : datum){
        // entry would be 370,88 for example
        String[] xy = entry.split(",");
        ArrayList<Integer> coordinates = new ArrayList<>();
        for (String num : xy)
          coordinates.add(Integer.parseInt(num));
        t.add(coordinates);
      }
    }
    catch(FileNotFoundException e){
      e.printStackTrace();
    }
    for (ArrayList<Integer> point : t){
      int x = point.get(0), y = point.get(1);
      click(x, y);
    }
  }

  /**
   * Determines how many times we need to shift the cursor downwards from the first row.
   * @param position
   * @return int
   */
  public int numberOfShiftsDown(int position){
    return position/4;
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
