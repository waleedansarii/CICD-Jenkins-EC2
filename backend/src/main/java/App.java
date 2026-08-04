import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.InetSocketAddress;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class App {

    public static void main(String[] args) throws Exception {

        Class.forName("com.mysql.cj.jdbc.Driver");

        Connection connection = DriverManager.getConnection(
                "jdbc:mysql://database:3306/mydb",
                "root",
                "root"
        );

        String createTable =
                "CREATE TABLE IF NOT EXISTS employees (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(100)," +
                "email VARCHAR(100)," +
                "designation VARCHAR(100))";

        connection.createStatement().execute(createTable);

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8080),
                0
        );

        server.createContext("/", new HttpHandler() {

            @Override
            public void handle(HttpExchange exchange) {

                try {

                    if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {

                        BufferedReader reader =
                                new BufferedReader(
                                        new InputStreamReader(
                                                exchange.getRequestBody()
                                        )
                                );

                        StringBuilder body = new StringBuilder();

                        String line;

                        while ((line = reader.readLine()) != null) {

                            body.append(line);
                        }

                        String requestBody = body.toString();

                        String name =
                                requestBody
                                        .split("\"name\":\"")[1]
                                        .split("\"")[0];

                        String email =
                                requestBody
                                        .split("\"email\":\"")[1]
                                        .split("\"")[0];

                        String designation =
                                requestBody
                                        .split("\"designation\":\"")[1]
                                        .split("\"")[0];

                        PreparedStatement statement =
                                connection.prepareStatement(
                                        "INSERT INTO employees(name, email, designation) VALUES (?, ?, ?)"
                                );

                        statement.setString(1, name);
                        statement.setString(2, email);
                        statement.setString(3, designation);

                        statement.executeUpdate();

                        String response =
                                "Employee Saved Successfully";

                        exchange.sendResponseHeaders(
                                200,
                                response.length()
                        );

                        OutputStream os =
                                exchange.getResponseBody();

                        os.write(response.getBytes());

                        os.close();
                    }

                } catch (Exception e) {

                    try {

                        String response = e.getMessage();

                        exchange.sendResponseHeaders(
                                500,
                                response.length()
                        );

                        OutputStream os =
                                exchange.getResponseBody();

                        os.write(response.getBytes());

                        os.close();

                    } catch (Exception ex) {

                        ex.printStackTrace();
                    }
                }
            }
        });

        server.start();

        System.out.println(
                "Backend running on port 8080"
        );
    }
}