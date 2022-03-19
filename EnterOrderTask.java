import java.util.ArrayList;
import java.util.Base64;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.awt.Robot;
import java.awt.Point;
import java.awt.MouseInfo;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.Rectangle;
import java.util.HashMap;
import java.awt.image.BufferedImage;

import org.json.JSONArray;
import org.json.JSONObject;


public class EnterOrderTask extends TimerTask {

  private boolean mouseTestActive;
  private Robot robot;
  private JSONObject currentlyProcessingOrder = null;
  private Main main;
  
  @Override
  public void run(){
    if (mouseTestActive){
      // Check if the mouse has moved. If not, we are probably safe to enter the order in.
      mouseTestActive = false;
      Point p = MouseInfo.getPointerInfo().getLocation();
      if ((int)p.getX() == 250 && (int)p.getY() == 250){
        // Safe to enter orders... maybe.
        // Enter only the first one, try again later.
        if (currentlyProcessingOrder == null){
          currentlyProcessingOrder = Main.cachedOrders.get(0);
          enterOrder(false, currentlyProcessingOrder);
        }
        // Elsewise, wait until later since we are current processing an order.
      }
    }
    else{
      // Restart the mouse test.
      mouseTestActive = true;
      robot.mouseMove(250, 250);
    }
  }

  /**
   * Enters an online order into the POS.
   * @param testmode Whether to submit or leave it.
   * @param order JSONObject
   */
  public void enterOrder(boolean testmode, JSONObject order){
    try {
      // Start

      // TO-DO: Detect and navigate to the takeout screen.
      Rectangle rect = new Rectangle(4, 49, 246, 201);

      // Press "new order"
      enter("takeoutNewOrder");
      TimeUnit.SECONDS.sleep(2);
      enter("customerMenu");
      TimeUnit.SECONDS.sleep(1);
      enter("phoneNumberEntryMenu");
      TimeUnit.SECONDS.sleep(1);
      
      // Enter the phone number
      JSONObject body = order.getJSONObject("body");
      type(body.getString("phone"));
      // Press enter
      enter("phoneNumberSubmit");
      TimeUnit.SECONDS.sleep(3);
      enter("phoneNumberSubmit2");

      enter("firstName1");
      type(body.getString("customerName"));
      enter("firstNameSubmit");

      // Now we can enter items.
      TimeUnit.SECONDS.sleep(1);

      JSONArray items = body.getJSONArray("items");
      for (int i = 0; i < items.length(); i++){
        JSONObject item = items.getJSONObject(i);
        // Enter item into computer.
        enter(item.getString("name"));
        TimeUnit.MILLISECONDS.sleep(300);
        // Check if item has small/large selection.
        // Decode menuItemReference
        JSONObject meta = new JSONObject(new String(Base64.getDecoder().decode(item.getString("menuItemReference"))));
        if (meta.getJSONArray("price").length() > 1){
          // Check size now.
          if (item.getString("size").equals("Small")){
            enter("small");
          }
          else{
            enter("large");
          }
        }
        if (i == 0){
          enter("customChoice");
          type("need ready for " + body.getString("customerTime"));
          enter("customChoiceSubmit");
        }
        if (Integer.parseInt(item.getString("quantity")) > 1){
          enter("quantity");
          type(item.getString("quantity"));
          enter("quantitySubmit");
        }
        if (item.getString("comments").length() > 0){
          enter("customChoice");
          type(item.getString("comments"));
          enter("customChoiceSubmit");
        }
      }

      // Now we can submit the order.
      if (!testmode){
        enter("done");
      }
      
      Main.cachedOrders.remove(0);
      currentlyProcessingOrder = null;
      main.bot.updateActivity(" orders: " + ++main.ordersProcessed);
    }
    catch(Exception e){
      e.printStackTrace();
      // Remove the item from currently processing.
      // but, do not remove it from cache.
      currentlyProcessingOrder = null;
    }
  }

  protected void type(String in){
    HashMap<String, Integer> map = new HashMap<>();
    map.put("a", KeyEvent.VK_A);
    map.put("b", KeyEvent.VK_B);
    map.put("c", KeyEvent.VK_C);
    map.put("d", KeyEvent.VK_D);
    map.put("e", KeyEvent.VK_E);
    map.put("f", KeyEvent.VK_F);
    map.put("g", KeyEvent.VK_G);
    map.put("h", KeyEvent.VK_H);
    map.put("i", KeyEvent.VK_I);
    map.put("j", KeyEvent.VK_J);
    map.put("k", KeyEvent.VK_K);
    map.put("l", KeyEvent.VK_L);
    map.put("m", KeyEvent.VK_M);
    map.put("n", KeyEvent.VK_N);
    map.put("o", KeyEvent.VK_O);
    map.put("p", KeyEvent.VK_P);
    map.put("q", KeyEvent.VK_Q);
    map.put("r", KeyEvent.VK_R);
    map.put("s", KeyEvent.VK_S);
    map.put("t", KeyEvent.VK_T);
    map.put("u", KeyEvent.VK_U);
    map.put("v", KeyEvent.VK_V);
    map.put("w", KeyEvent.VK_W);
    map.put("x", KeyEvent.VK_X);
    map.put("y", KeyEvent.VK_Y);
    map.put("z", KeyEvent.VK_Z);
    map.put("0", KeyEvent.VK_0);
    map.put("1", KeyEvent.VK_1);
    map.put("2", KeyEvent.VK_2);
    map.put("3", KeyEvent.VK_3);
    map.put("4", KeyEvent.VK_4);
    map.put("5", KeyEvent.VK_5);
    map.put("6", KeyEvent.VK_6);
    map.put("7", KeyEvent.VK_7);
    map.put("8", KeyEvent.VK_8);
    map.put("9", KeyEvent.VK_9);
    map.put(",", KeyEvent.VK_COMMA);
    map.put("!", KeyEvent.VK_EXCLAMATION_MARK);
    map.put(".", KeyEvent.VK_PERIOD);
    map.put(" ", KeyEvent.VK_SPACE);
    map.put("&", KeyEvent.VK_AMPERSAND);
    for (int i = 0; i < in.length(); i++){
      String c = (in.charAt(i) + "").toLowerCase();
      if (map.containsKey(c)){
        int key = map.get(c);
        robot.keyPress(key);
        robot.keyRelease(key);
      }
    }
  }

  protected void enter(String sequence){
    ArrayList<ArrayList<Integer>> seq = Main.getCoordinates(sequence);
    for (int i = 0; i < seq.size(); i++){
      ArrayList<Integer> s = seq.get(i);
      robot.mouseMove(s.get(0), s.get(1));
      robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
      robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }
  }

  public EnterOrderTask(Main main){
    mouseTestActive = true;
    this.main = main;
    // Init the mouse test. Scan on next cycle.
    try {
      robot = new Robot();
      // Move the mouse to the testing zone.
      robot.mouseMove(250, 250);
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  /**
   * Adds an order to the waiting stash. The system will enter them as needed.
   * @param order
   */
  protected static void addOrderToCache(JSONObject order){
    Main.cachedOrders.add(order);
  }
  
}
