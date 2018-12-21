package CS310.SQLPackage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class dbconn {



        private static Connection connection;

        public static Connection retrieveConnection() throws ClassNotFoundException, SQLException {
            if (connection != null) // Singleton Design Pattern
                return connection;

            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost/chat_database", "root", "12345678");

            return connection;
        }
    }



