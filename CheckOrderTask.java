import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.TimerTask;

public class CheckOrderTask extends TimerTask {

  private Main main;

  public CheckOrderTask(Main main){
    this.main = main;
  }

  @Override
  public void run(){

    if (!main.enabled){
      return;
    }

    System.out.println(".");

    // Here, we can run a check for online orders.
    try {
      URL url = new URL("https://fustarbuffet.com/online-order-api/fetch/jbj9zhRM6CpucC46ezQU6nJy");
      URLConnection uc = url.openConnection();
      InputStream is = uc.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      String line = null;
      String data = "";
      while ((line = reader.readLine()) != null){
        data += line;
      }
      reader.close();
      is.close();
      // Send over to processing.
      ProcessOrder.process(data);
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

}