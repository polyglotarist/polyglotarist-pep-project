package DAO;

import Model.Account;
import Util.ConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    // Insert a new account into the database (register)
    public Account insertAccount(Account account) {
        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "INSERT INTO Account (username, password) VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // Set the parameters for the PreparedStatement
            preparedStatement.setString(1, account.getUsername());
            preparedStatement.setString(2, account.getPassword());

            preparedStatement.executeUpdate();
            ResultSet pkeyResultSet = preparedStatement.getGeneratedKeys();
            if (pkeyResultSet.next()) {
                int generatedAccountId = pkeyResultSet.getInt(1); // Use getInt for the account ID
                return new Account(generatedAccountId, account.getUsername(), account.getPassword());
            }

        } catch (SQLException e) {
            System.out.println("Error inserting account: " + e.getMessage());
        } finally {
            // Ensure the connection is closed after operation
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
        return null; // Return null if insertion fails
    }

    // Retrieve all usernames from the database
    public List<String> getAllUsernames() {
        List<String> usernameList = new ArrayList<>();
        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "SELECT username FROM account"; // Fixed query syntax
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                usernameList.add(resultSet.getString("username"));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving usernames: " + e.getMessage());
        } finally {
            // Ensure the connection is closed after operation
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
        return usernameList; // Return the list of usernames
    }

    // Retrieve an account by username
    public Account getAccountByUsername(String username) {
        Account account = null;
        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "SELECT * FROM account WHERE username = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, username);

            // ResultSet resultSet = preparedStatement.executeQuery();
            ResultSet pkeyResultSet = preparedStatement.getGeneratedKeys();
            ResultSet rs = preparedStatement.executeQuery();
            if(pkeyResultSet.next()){
                int generated_account_id = (int) pkeyResultSet.getLong(1);
                if(rs.next()){
                    String theUsername = rs.getString("username");
                    String thePassword = rs.getString("password");
                    account =  new Account(generated_account_id, theUsername, thePassword);

                }
               
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving account: " + e.getMessage());
        } finally {
            // Ensure the connection is closed after operation
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
        return account; // Return the found account or null if not found
    }
    //check if account exists
    public Boolean accountExists(String username){
        Boolean result = false;
        Account account = null;
        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "SELECT exists(select username from account where username = ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()){
                Boolean usernameExists = resultSet.getBoolean(1);
                System.out.println("I am here, the output is: " + usernameExists);
                if(usernameExists){
                    result = true;
                }else{
                    result = false;
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving account: " + e.getMessage());
        } finally {
            // Ensure the connection is closed after operation
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
        return result;
    }

}
