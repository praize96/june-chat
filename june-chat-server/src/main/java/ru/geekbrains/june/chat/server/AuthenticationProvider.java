package ru.geekbrains.june.chat.server;

public interface AuthenticationProvider {
    String getUsernameByLoginAndPassword(String login, String password);
    void start();
    void stop();
}
