
package utb.fai;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class EmailSender {

    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;

    public EmailSender(String host, int port) throws IOException {

        socket = new Socket(host, port);

        reader = new BufferedReader(
            new InputStreamReader(
                socket.getInputStream(),
                StandardCharsets.UTF_8
            )
        );

        writer = new BufferedWriter(
            new OutputStreamWriter(
                socket.getOutputStream(),
                StandardCharsets.UTF_8
            )
        );

        readResponse(220);
    }

    private void readResponse(int expectedCode) throws IOException {

        String line;
        String code = null;

        do {
            line = reader.readLine();

            if (line == null) {
                throw new IOException("Server closed connection");
            }

            System.out.println(line);

            if (line.length() < 4) {
                throw new IOException("Invalid SMTP response: " + line);
            }

            String currentCode = line.substring(0, 3);

            if (code == null) {
                code = currentCode;
            } else if (!code.equals(currentCode)) {
                throw new IOException("Inconsistent SMTP response: " + line);
            }

        } while (line.charAt(3) == '-');

        if (!code.equals(String.valueOf(expectedCode))) {
            throw new IOException("SMTP error: " + line);
        }
    }

    private void sendCommand(String command, int expectedCode)
            throws IOException {

        writer.write(command + "\r\n");
        writer.flush();

        readResponse(expectedCode);
    }

    public void send(String from, String to, String subject, String text)
            throws IOException {

        if (from == null || to == null ||
            subject == null || text == null) {
            throw new IllegalArgumentException(
                "Email parameters cannot be null"
            );
        }

        // SMTP connection
        sendCommand("EHLO localhost", 250);

        // Sender
        sendCommand("MAIL FROM:<" + from + ">", 250);

        // Recipient
        sendCommand("RCPT TO:<" + to + ">", 250);

        // Start email data
        sendCommand("DATA", 354);

        // Email headers
        writer.write("From: " + from + "\r\n");
        writer.write("To: " + to + "\r\n");
        writer.write("Subject: " + subject + "\r\n");
        writer.write("MIME-Version: 1.0\r\n");
        writer.write("Content-Type: text/plain; charset=UTF-8\r\n");
        writer.write("\r\n");

        // Normalize line endings
        String normalizedText = text
                .replace("\r\n", "\n")
                .replace("\r", "\n");

        // Email body
        String[] lines = normalizedText.split("\n", -1);

        for (String line : lines) {

            // SMTP dot-stuffing
            if (line.startsWith(".")) {
                writer.write(".");
            }

            writer.write(line + "\r\n");
        }

        // End email
        writer.write(".\r\n");
        writer.flush();

        readResponse(250);
    }

    public void close() {

        try {
            if (!socket.isClosed()) {

                try {
                    sendCommand("QUIT", 221);
                } finally {
                    socket.close();
                }
            }

        } catch (IOException e) {
            System.err.println(
                "Error closing connection: " + e.getMessage()
            );
        }
    }
}
