import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

public class Main {

  private String token = "ODQyMTQ4MjIwMDM3NzU5MDI3.YJxFpg.MZ6cwdzhW-6ezPQPszyqco6YMaE"; // DO NOT SHARE!

  protected DiscordApi bot = new DiscordApiBuilder().setToken(token).login().join();

  public Main(){
    
  }
  public static void main(String[] args){
    new Main();
  }
}