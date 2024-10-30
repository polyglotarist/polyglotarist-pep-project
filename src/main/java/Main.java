
import DAO.AccountDAO;
import DAO.MessageDAO;
import Model.Account;
import Model.Message;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        MessageDAO messageDAO = new MessageDAO();
        AccountDAO accountDAO = new AccountDAO();
        
        //Test if username exists:
        String username = "aUsername";
        String password = "aPassword";
        Account newAccount = new Account(username, password);
        Boolean accountExists = accountDAO.accountExists(username);
        System.out.println(newAccount.toString() + " "+ accountExists);
        // Test adding a new message
        System.out.println("Adding a new message...");
        Message newMessage = new Message(1, "Hello, world!", System.currentTimeMillis() / 1000L); // Replace 1 with a valid user ID
        Message addedMessage = messageDAO.addMessage(newMessage);
        System.out.println("Added Message: " + addedMessage);

        // Test getting all messages
        System.out.println("\nRetrieving all messages...");
        List<Message> allMessages = messageDAO.getAllMessages();
        for (Message message : allMessages) {
            System.out.println(message);
        }

        // Test getting a message by ID
        System.out.println("\nRetrieving message by ID...");
        if (addedMessage != null) {
            Message retrievedMessage = messageDAO.getMessageById(addedMessage.getMessage_id());
            System.out.println("Retrieved Message: " + retrievedMessage);
        }

        // Test updating a message
        System.out.println("\nUpdating the added message...");
        if (addedMessage != null) {
            addedMessage.setMessage_text("Hello, updated world!");
            Message aMessage = messageDAO.updateMessage(addedMessage);
            boolean isUpdated = (aMessage != null? true : false);
            System.out.println("Message updated: " + isUpdated);
            // Verify the update
            Message updatedMessage = messageDAO.getMessageById(addedMessage.getMessage_id());
            System.out.println("Updated Message: " + updatedMessage);
        }

        // Test getting messages by user
        System.out.println("\nRetrieving messages by user...");
        List<Message> userMessages = messageDAO.getMessagesByUser(1); // Replace 1 with a valid user ID
        for (Message message : userMessages) {
            System.out.println(message);
        }

        // Test deleting a message
        System.out.println("\nDeleting the added message...");
        if (addedMessage != null) {
            boolean isDeleted = messageDAO.deleteMessage(addedMessage.getMessage_id());
            System.out.println("Message deleted: " + isDeleted);
        }

        // Verify deletion
        System.out.println("\nRetrieving all messages after deletion...");
        allMessages = messageDAO.getAllMessages();
        for (Message message : allMessages) {
            System.out.println(message);
        }
    }
}
