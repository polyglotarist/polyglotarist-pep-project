package Service;

import java.util.List;

import DAO.MessageDAO;
import Model.Message;

public class MessageService {
    private final MessageDAO messageDAO;

    public MessageService() {
        this.messageDAO = new MessageDAO();
    }

    public MessageService(MessageDAO messageDAO) {
        this.messageDAO = messageDAO; 
    }

    public List<Message> getAllMessages() {
        // Retrieves all messages from the DAO
        return messageDAO.getAllMessages(); 
    }

    public boolean deleteMessage(int messageId) {
        // Deletes a message by its ID and returns true if deletion was successful
        return messageDAO.deleteMessage(messageId);
    }

    public boolean updateMessage(Message updatedMessage) {
        // Updates an existing message and returns true if the update was successful
        return messageDAO.updateMessage(updatedMessage);
    }

    public List<Message> getMessagesByUser(int userId) {
        // Retrieves all messages posted by a specific user
        return messageDAO.getMessagesByUser(userId);
    }

    public Message getMessageById(int messageId) {
        // Retrieves a message by its ID
        return messageDAO.getMessageById(messageId);
    }

    public Message addMessage(Message message) {
        // Adds a new message to the database and returns the added message
        return messageDAO.addMessage(message);
    }
}
