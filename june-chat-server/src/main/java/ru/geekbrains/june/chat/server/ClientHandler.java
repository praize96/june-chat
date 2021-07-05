package ru.geekbrains.june.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {  //обработчик клиента
    private Server server;
    private Socket socket;
    private String username;
    private DataInputStream in;
    private DataOutputStream out;

    public String getUsername() {
        return username;
    }

    public ClientHandler(Server server, Socket socket) {//запускаем поток для общения с клиентом при новом подключении
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> logic()).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {//метод для отправки сообения
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logic() {
        try {
            while (!consumeAuthorizeMessage(in.readUTF()));
            while (consumeRegularMessage(in.readUTF()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Клиент " + username + " отключился");
            server.unsubscribe(this);
            closeConnection();
        }
    }

    private boolean consumeRegularMessage(String inputMessage) {  //обработка сообщений от клиента
        if (inputMessage.startsWith("/")) {
            if (inputMessage.equals("/exit")) {
                sendMessage("/exit");
                return false;
            }
            if (inputMessage.startsWith("/w ")) {
                String[] tokens = inputMessage.split("\\s+", 3);
                server.sendPersonalMessage(this, tokens[1], tokens[2]);
            }
            return true;
        }
        server.broadcastMessage(username + ": " + inputMessage);
        return true;
    }

    private boolean consumeAuthorizeMessage(String message) { //обработка авторизационных сообщений
        if (message.startsWith("/auth ")) { // /auth bob
            String[] tokens = message.split("\\s+");
            if (tokens.length != 3) {
                sendMessage("SERVER: Неверно сформирована команда на авторизацию");
                return false;
            }

            String login = tokens[1];
            String password = tokens[2];

            String selectedUsername = server.getAuthenticationProvider().getUsernameByLoginAndPassword(login, password);

            if (selectedUsername == null) {
                sendMessage("SERVER: Неверный логин или пароль");
                return false;
            }

            if (server.isUsernameUsed(selectedUsername)) {
                sendMessage("SERVER: Данное имя пользователя уже занято");
                return false;
            }
            username = selectedUsername;
            sendMessage("/authok " + username);
            server.subscribe(this);
            return true;
        } else {
            sendMessage("SERVER: Вам необходимо авторизоваться");
            return false;
        }
    }

    private void closeConnection() { //закрытие соединения с клиентом
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
