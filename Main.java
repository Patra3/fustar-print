import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Timer;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

public class Main {

  private String token = "ODQyMTQ4MjIwMDM3NzU5MDI3.YJxFpg.MZ6cwdzhW-6ezPQPszyqco6YMaE"; // DO NOT SHARE!

  protected DiscordApi bot = new DiscordApiBuilder().setToken(token).login().join();

  public Timer timer = new Timer();

  protected int ordersProcessed = 0;

  public Main(){
    // First we init the Discord bot status.
    bot.updateActivity(" orders: " + ordersProcessed);
    // Next, we schedule a repeating task to check the online ordering server.
    timer.scheduleAtFixedRate(new CheckOrderTask(), 3000, 10000);

    bot.addMessageCreateListener(event -> {
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
    });
  }

  public static void main(String[] args){
    new Main();
  }
}