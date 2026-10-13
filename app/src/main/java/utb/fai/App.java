
package utb.fai;

import java.io.IOException;

public class App {

    public static void main(String[] args) {

        // Check number of arguments
        if (args.length != 6) {
            System.err.println(
                "Usage: java -jar app.jar <host> <port> <from> <to> <subject> <text>"
            );
            return;
        }

        String host = args[0];
        int port;

        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number");
            return;
        }

        String from = args[2];
        String to = args[3];
        String subject = args[4];
        String text = args[5];

        EmailSender sender = null;

        try {
            sender = new EmailSender(host, port);

            System.out.println("Connected to server");

            sender.send(from, to, subject, text);

            System.out.println("Email sent successfully");

        } catch (IOException e) {
            System.err.println("Error sending email: " + e.getMessage());

        } finally {
            if (sender != null) {
                sender.close();
            }
        }
    }
}
