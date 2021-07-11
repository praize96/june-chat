package ru.geekbrains.june.chat.server;

import java.sql.*;

public class DatabaseAuthenticationProvider implements AuthenticationProvider {
    private static Statement statement;
    private static Connection connection;

    private class UserInfo {
        private String login;
        private String password;
        private String username;

        public UserInfo(String login, String password, String username) {
            this.login = login;
            this.password = password;
            this.username = username;
        }
    }

    @Override
    public void start() {
        try {
            connect();
            createTable();
            insertUser("Bob@mail.ru", "1234", "Bob");
            insertUser("Johnb@mail.ru", "1234", "John");
            insertUser("Evgeniy@mail.ru", "1234", "Evgeniy");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void stop() {
        try {
            dropTable();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            disconnect();
        }
    }

    public static void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:chatusers.db");
        statement = connection.createStatement();
    }

    public static void createTable() throws SQLException {
        String sql = "create table if not exists usersdb (\n" +
                "id integer primary key autoincrement not null,\n" +
                "login text not null,\n" +
                "password text not null,\n" +
                "username text not null\n" +
                ");";
        System.out.println(sql);
        statement.executeUpdate(sql);
    }

    public static void insertUser(String login, String password, String username) throws SQLException {
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("insert into usersdb (login, password, username) values (?, ?, ?)")) {
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, username);
            preparedStatement.addBatch();
            preparedStatement.executeBatch();
        }
    }

    public String readTable(String login, String password) {
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("select * from usersdb where lower (login) = lower (?) and password = ?")) {
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        }catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void dropTable() throws SQLException {
        statement.execute("drop table usersdb;");
    }

    public static void disconnect() {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        String nickname = readTable(login, password);
        return nickname;
    }
}
