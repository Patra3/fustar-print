import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

public class MouseListener implements NativeMouseInputListener {

  private boolean listening = false;
  private String record = "";

  public void listen(){
    listening = true;
  }

  public void stopListen(){
    listening = false;
  }

  public void erase(){
    record = "";
  }

  public String getRecorded(){
    return record;
  }

  public void nativeMouseClicked(NativeMouseEvent e){
    if (listening)
      record += "" + e.getX() + "," + e.getY() + ";";
  }
  public void nativeMouseReleased(NativeMouseEvent e){}
  public void nativeMouseMoved(NativeMouseEvent e){}
  public void nativeMouseDragged(NativeMouseEvent e){}
  public void nativeMousePressed(NativeMouseEvent e){}
}
