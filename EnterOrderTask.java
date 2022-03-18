import java.util.ArrayList;
import java.util.TimerTask;
import java.awt.Robot;
import java.awt.Point;
import java.awt.MouseInfo;
import java.awt.event.InputEvent;

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
      // TO-DO;
      // Remove the item from the list and the variable.
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

  }
  
}
