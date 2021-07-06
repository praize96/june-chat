package ru.geekbrains.june.chat.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Controller {
    @FXML
    TextArea chatArea;

    @FXML
    TextField messageField, loginField;

    @FXML
    HBox authPanel, msgPanel;

    @FXML
    ListView<String> clientsListView;

    @FXML
    PasswordField passwordField;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Stage stage;
    private String nickname = null;

    public void setStage(Stage stage) {  //метод setter для установки стэйджа в классе контроллера
        this.stage = stage;
    }

    public void setAuthorized(boolean authorized) { //переключение панелей в зависимости от авторизщации
        msgPanel.setVisible(authorized);
        msgPanel.setManaged(authorized);
        setTitleForUser(authorized);
        authPanel.setVisible(!authorized);
        authPanel.setManaged(!authorized);
        clientsListView.setVisible(authorized);
        clientsListView.setManaged(authorized);
    }

    public void sendMessage() { // отправка сообщения на сервер
        try {
            out.writeUTF(messageField.getText());
            messageField.clear();
            messageField.requestFocus();
        } catch (IOException e) {
            showError("Невозможно отправить сообщение на сервер");
        }
    }

    public void sendCloseRequest() {  //завершение соединения с сервером
        try {
            if (out != null) {
                out.writeUTF("/exit");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setTitleForUser(boolean authorized) { //установка заголовка окна
        String title;
        if (authorized) {
             title = "name: " + nickname;
        } else {
             title = "June Chat Client";
        }
        Platform.runLater(() -> { // для внесения изменений в трэде, где работаем с javafx
            stage.setTitle(title);
        });
    }

    public void tryToAuth() { //авторизация клиента
        connect();
        try {
            out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            showError("Невозможно отправить запрос авторизации на сервер");
        }
    }

    public void connect() { //создание нового клиента и треда
        if (socket != null && !socket.isClosed()) {
            return;
        }
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> logic()).start();
        } catch (IOException e) {
            showError("Невозможно подключиться к серверу");
        }
    }

    private void logic() { //основная логика
        try {
            while (true) {
                String inputMessage = in.readUTF();
                if (inputMessage.equals("/exit")) {
                    closeConnection();
                }
                if (inputMessage.startsWith("/authok")) {
                    String myUsername = inputMessage.split("\\s+")[1];
                    nickname = myUsername;
                    setAuthorized(true);
                    break;
                }
                chatArea.appendText(inputMessage + "\n");
            }
            while (true) {
                String inputMessage = in.readUTF();
                if (inputMessage.startsWith("/")) {
                    if (inputMessage.equals("/exit")) {
                        break;
                    }
                    // /clients_list bob john
                    if (inputMessage.startsWith("/clients_list ")) {
                        Platform.runLater(() -> {  // для внесения изменений в трэде, где работаем с javafx
                            String[] tokens = inputMessage.split("\\s+");
                            clientsListView.getItems().clear();
                            for (int i = 1; i < tokens.length; i++) {
                                clientsListView.getItems().add(tokens[i]);
                            }
                        });
                    }
                    continue;
                }
                chatArea.appendText(inputMessage + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {  //закрытие соединения с сервером
        setAuthorized(false);
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

    public void showError(String message) {  //отображение ошибки
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }

    public void clientsListDoubleClick(MouseEvent mouseEvent) {  //событие для отправки личного сообщения по нажатию на имя клиента
        if (mouseEvent.getClickCount() == 2) {
            String selectedUser = clientsListView.getSelectionModel().getSelectedItem();
            messageField.setText("/w " + selectedUser + " ");
            messageField.requestFocus();
            messageField.selectEnd();
        }
    }
}
