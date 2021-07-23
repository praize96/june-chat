package ru.geekbrains.june.chat.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {  //обработчик клиента
    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);
    private Server server;
    private Socket socket;
    private String username;
    private DataInputStream in;
    private DataOutputStream out;
    private ExecutorService executorService;

    public String getUsername() {
        return username;
    }

    public ClientHandler(Server server, Socket socket) {//запускаем поток для общения с клиентом при новом подключении
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                logic();
            });
//            new Thread(() -> logic()).start();
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("Ошибка: " + e.getMessage());
        }
    }

    public void sendMessage(String message) {//метод для отправки сообения
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
    }

    private void logic() {
        try {
            while (!consumeAuthorizeMessage(in.readUTF()));
            while (consumeRegularMessage(in.readUTF()));
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
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
            LOGGER.error(e.getMessage());
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
        executorService.shutdown();
    }
}
