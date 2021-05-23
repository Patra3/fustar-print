import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
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
        else if (event.getMessageContent().equalsIgnoreCase("!ultimatetest")){
          menu.getJSONArray("items").forEach(item -> {
            try{
              TimeUnit.MILLISECONDS.sleep(500);
              JSONObject i = (JSONObject)item;
              if (i.has("choice")){
                for (int choiceIndex = 0; choiceIndex < i.getJSONArray("choice").length(); choiceIndex++){
                  clickItem(i.getString("code"), i.getInt("type"), i.getJSONArray("choice").getString(choiceIndex));
                }
              }
              else if (i.getJSONArray("price").length() > 1){
                clickItem(i.getString("code"), i.getInt("type"), "");
                clickSmall();
                clickItem(i.getString("code"), i.getInt("type"), "");
                clickLarge();
              }
              else{
                clickItem(i.getString("code"), i.getInt("type"), "");
              }
            }
            catch(InterruptedException e){
              e.printStackTrace();
            }
          });
          event.getChannel().sendMessage("Task completed.");
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
   * Type on POS keyboard.
   * @param text
   */
  public void keyboard(String text){
    // TO-DO
  }

  /**
   * Type on POS numpad.
   * @param numbers
   */
  public void numpad(String numbers){
    // TO-DO

  }

  /**
   * Click the "small" option.
   */
  public void clickSmall(){
    try {
      click("click_small");
    }
    catch(FileNotFoundException e){
      e.printStackTrace();
    }
  }

  /**
   * Click the "large" option.
   */
  public void clickLarge(){
    try {
      click("click_large");
    }
    catch(FileNotFoundException e){
      e.printStackTrace();
    }
  }

  /**
   * Executes a click on an item.
   * @param code i.e.  C4
   * @param type i.e.  Lunch Special = 0
   * @param choice i.e. Pork, chicken
   * @param size i.e. Small
   */
  public void clickItem(String code, int type, String choice){

    // Click on the category first.
    try{
      click("type_" + type);
    }
    catch(FileNotFoundException e){
      e.printStackTrace();
    }


    /*
    The plan is to:
    - Search for a precise item file name for hardcode,
    - if not found, then use grid fallback.
    */

    try {
      JSONObject ar = getMenuItem(code);
      String name = code + ". " + ar.getString("name");
      if (ar.has("choice")){
        name += "_" + choice;
      }
      click(name);
    }
    catch(FileNotFoundException e){
      // manual fixes for Appetizer
      //(locationWithinCategory(code, type) + 1)
      String target = "grid_";
      if (code.equals("6") && choice.equals("Fried")){
        target += 6;
      }
      else if (code.equals("6") && choice.equals("Steamed")){
        target += 7;
      }
      else if (code.equals("7")){
        target += 8;
      }
      else if (code.equals("8")){
        target += 9;
      }
      else if (code.equals("9")){
        target += 10;
      }
      else if (code.equals("10")){
        target += 11;
      }
      else if (code.equals("11")){
        target += 12;
      }
      else if (code.equals("12")){
        target += 13;
      }
      else if (code.equals("13")){
        target += 14;
      }
      else if (code.equals("14")){
        target += 15;
      }
      //manual fixes for chow mei fun.
      else if (code.equals("37") && choice.equals("Chicken")){
        target += 3;
      }
      else if (code.equals("37") && choice.equals("Roast Pork")){
        target += 4;
      }
      else if (code.equals("38")){
        target += 5;
      }
      else if (code.equals("39")){
        target += 6;
      }
      // Manual fix for chicken.
      else if (code.equals("40")){
        target = "40. Chicken with Black Bean Sauce";
      }
      // Manual fix for Shrimp.
      else if (code.equals("92") && choice.equals("Scallop")){
        target += 12;
      }
      else if (code.equals("92") && choice.equals("Shrimp")){
        target += 13;
      }
      else if (code.equals("93")){
        target += 14;
      }
      else if (code.equals("94")){
        target += 15;
      }
      else if (code.equals("95")){
        target += 16;
      }
      else if (code.equals("96")){
        target += 17;
      }
      else if (code.equals("97")){
        target += 18;
      }
      // Manual fix for Vegetable
      else if (code.equals("105") && choice.equals("Mushroom")){
        target += 8;
      }
      else if (code.equals("105") && choice.equals("Vegetable")){
        target += 9;
      }
      else if (code.equals("106")){
        target += 10;
      }
      // Manual fix for Chef specials
      else if (code.equals("S12") && choice.equals("Scallop")){
        target += 12;
      }
      else if (code.equals("S12") && choice.equals("Shrimp")){
        target += 13;
      }
      else if (code.equals("S13")){
        target += 14;
      }
      else if (code.equals("S14")){
        target += 15;
      }
      else if (code.equals("S15")){
        target += 16;
      }
      else if (code.equals("S16")){
        target += 17;
      }
      // Side order manually added hopefully :)
      else {
        // Test only with number parsing.
        try {
          Integer itemCode = Integer.parseInt(code);
          // Manual fix for chicken.
          if (itemCode > 40 && itemCode < 62 && itemCode != 51){
            target += locationWithinCategory(code, type);
          }
          else if (itemCode == 51){
            target += locationWithinCategory(code, type);
          }
          else{
            // For non-fix-needed items.
            target += locationWithinCategory(code, type) + 1;
          }
        }
        catch(Exception x){
          // Move on, not a number.
          target += locationWithinCategory(code, type) + 1;
          System.out.println("Item not number, moving on...");
        }
      }
      try {
        click(target);
        if (code.equals("51") || code.equals("68")){
          click("cancel_choice_dialog");
        }
      }
      catch(FileNotFoundException f){
        f.printStackTrace();
      }
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
  public void click(String access) throws FileNotFoundException{
    System.out.println(access);
    // [    [0,0] ,  [1,0]  , [2,0]    ]
    ArrayList<ArrayList<Integer>> t = new ArrayList<>();
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
