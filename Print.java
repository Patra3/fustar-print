import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.json.JSONObject;

public class Print {
  
  private JSONObject menu;
  private DiscordApi bot = new DiscordApiBuilder().setToken("ODQyMTQ4MjIwMDM3NzU5MDI3.YJxFpg.Z3rBemeRV3vsiUNridUqtwAtwXE").login().join();
  private boolean registering = false;

  Print() {
    try {
      // Register restaurant menu data.
      File payload = new File("payload.txt");
      Scanner scanner = new Scanner(payload);
      String data = "";
      while (scanner.hasNextLine()) {
        data += scanner.nextLine();
      }
      menu = new JSONObject(data);
      scanner.close();

      // If not exist, create position db folder.
      File folder = new File("positions");
      if (!folder.exists())
        folder.mkdirs();

      // Register Discord bot listeners.
      bot.addMessageCreateListener(event -> {
        if (registering){
          // Index number;
          try{

          }
          catch(Exception e){
            
          }
          JSONObject item = menu.getJSONArray("items").getJSONObject(Integer.parseInt(event.getMessageContent()));
          System.out.println(item);
        }
        if (event.getMessageContent().equalsIgnoreCase("!register")) {
          // Get the last item not registered yet.
          event.getChannel().sendMessage("Please enter the `item[]` index you wish to register clicks for.");
          registering = true;
        }
      });
    }
    catch(FileNotFoundException e){
      e.printStackTrace();
    }
  }

  public static void main(String[] args){
    new Print();
  }
}
