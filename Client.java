import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
  public static void main(String[] args) {
    Socket clientSocket;
    BufferedReader in;
    PrintWriter out;
    Scanner keyboard = new Scanner(System.in);
    try {
      clientSocket = new Socket("127.0.0.1", 5000);
      out = new PrintWriter(clientSocket.getOutputStream());
      in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      Thread sender = new Thread(new Runnable() {
        String msg;
        @Override
        public void run() {
          while (true) {
            msg = keyboard.nextLine();
            out.println(msg);
            out.flush();
          }
        }
      });
      sender.start();
      Thread receiver = new Thread(new Runnable() {
        String msg;
        @Override
        public void run() {
          try {
            msg = in.readLine();
            while (msg != null) {
              System.out.println("Server : " + msg);
              msg = in.readLine();
            }

            out.close();
            clientSocket.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      });
      receiver.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
