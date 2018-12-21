/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CSE310.ChatApplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import static javax.management.remote.JMXConnectorFactory.connect;

/**
 *
 * @author user
 */
public class connection {
    public Connection connect;
    public Connection getconnection() throws ClassNotFoundException, SQLException{
        try{
            String myurl="jdbc:mysql://localhost/chat_application_cse310";
            Class.forName("com.mysql.jdbc.Driver");
            try{
                connect= DriverManager.getConnection(myurl,"root","12345678");
            }
            catch(Exception e){
                System.out.println("mysql error");
            }
        }
        catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        
        
        return connect;
    }
}
