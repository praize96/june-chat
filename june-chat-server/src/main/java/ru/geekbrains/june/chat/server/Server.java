package ru.geekbrains.june.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private List<ClientHandler> clients;  //список клиентов
    private AuthenticationProvider authenticationProvider;

    public AuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    public Server() {
        try {
            this.authenticationProvider = new DatabaseAuthenticationProvider();  //переключение на бд
            this.clients = new ArrayList<>();
            ServerSocket serverSocket = new ServerSocket(8189); //адрес сервера
            System.out.println("Сервер запущен. Ожидаем подключение клиентов..");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Подключился новый клиент");
                new ClientHandler(this, socket);  //при подключении добавляет клиента и обрабатываем
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribe(ClientHandler c) {  //добавление в список клиентов
        broadcastMessage("В чат зашел пользователь " + c.getUsername());
        clients.add(c);
        broadcastClientList();
    }

    public synchronized void unsubscribe(ClientHandler c) { //исключение из списка клиентов
        clients.remove(c);
        broadcastMessage("Из чата вышел пользователь " + c.getUsername());
        broadcastClientList();
    }

    public synchronized void broadcastMessage(String message) { //метод отправки сообещения всем клиентам на сервере
        for (ClientHandler c : clients) {
            c.sendMessage(message);
        }
    }

    public synchronized void broadcastClientList() {  //список авторизованных клиентов
        StringBuilder builder = new StringBuilder(clients.size() * 10);
        builder.append("/clients_list ");
        for (ClientHandler c : clients) {
            builder.append(c.getUsername()).append(" ");
        }
        String clientsListStr = builder.toString();
        broadcastMessage(clientsListStr);
    }

    public synchronized boolean isUsernameUsed(String username) {  //метод проверки в списке существующих клиентов
        for (ClientHandler c : clients) {
            if (c.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void sendPersonalMessage(ClientHandler sender, String receiverUsername, String message) { //метод отправки личных сообщений
        if (sender.getUsername().equalsIgnoreCase(receiverUsername)) {
            sender.sendMessage("Нельзя отправлять личные сообщения самому себе");
            return;
        }
        for (ClientHandler c : clients) {
            if (c.getUsername().equalsIgnoreCase(receiverUsername)) {
                c.sendMessage("от " + sender.getUsername() + ": " + message);
                sender.sendMessage("пользователю " + receiverUsername + ": " + message);
                return;
            }
        }
        sender.sendMessage("Пользователь " + receiverUsername + " не в сети");
    }
}

