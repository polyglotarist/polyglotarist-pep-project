package Controller;

import Service.AccountService;
import Service.MessageService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import Model.Account;
import Model.Message;

import java.util.List;

public class SocialMediaController {
    private final AccountService accountService;
    private final MessageService messageService;

    public SocialMediaController() {
        this.accountService = new AccountService();
        this.messageService = new MessageService();
    }

    public Javalin startAPI() {
        Javalin app = Javalin.create();

        app.post("/register", this::addAccountHandler);
        app.post("/login", this::getLoginHandler); 
        // app.post("/message", this::addMessageHandler); // Added message creation endpoint
        // app.get("/messages", this::getAllMessagesHandler);
        // app.get("/message/:{id}", this::getMessageByIdHandler); // Added message retrieval by ID
        // app.delete("/message/:{id}", this::deleteMessageHandler); // Added message deletion by ID
        // app.put("/message/:{id}", this::updateMessageHandler); // Added message update by ID
        // app.get("/users/:userId/messages", this::getMessagesByUserHandler); // Added messages by user

        System.out.println("API started.");
        return app;
    }

    // 1: Our API should be able to process new User registrations.
    private void addAccountHandler(Context ctx) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Account account = objectMapper.readValue(ctx.body(), Account.class);
        System.out.println("Received account registration request.");

        if (account.getPassword().length() >= 4 && account.getUsername().length() > 0
         && accountService.accountExists(account.getUsername()) == false) {
            
            Account addedAccount = accountService.addAccount(account);
            ctx.json(objectMapper.writeValueAsString(addedAccount));
            ctx.status(200); // Created status
            System.out.println("Account created successfully.");
        } else {
            ctx.status(400);
            // ctx.json(account);
            System.out.println("Invalid account data.");
        }
    }

    // 2: Our API should be able to process User logins.
    private void getLoginHandler(Context ctx) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Account loginRequest = objectMapper.readValue(ctx.body(), Account.class);

        System.out.println("Received login request.");

        boolean isValidUser = accountService.validateUser(loginRequest.getUsername(), loginRequest.getPassword());
        if (isValidUser) {
            // Account account = new Account(loginRequest.getAccount_id(), loginRequest.getUsername(), loginRequest.getPassword());
            ctx.status(200);
            ctx.json(accountService.getAccountByUsername(loginRequest.getUsername()));
            System.out.println("Login successful for user: " + loginRequest.getUsername());
        } else {
            ctx.status(401);
            System.out.println("Invalid login attempt.");
        }
    }

    // 3: Our API should be able to process the creation of new messages.
    private void addMessageHandler(Context ctx) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Message message = objectMapper.readValue(ctx.body(), Message.class);
        System.out.println("Received new message request.");

        try {
            Message addedMessage = messageService.addMessage(message);
            ctx.json(objectMapper.writeValueAsString(addedMessage));
            ctx.status(201); // Created status
            System.out.println("Message created successfully.");
        } catch (Exception e) {
            ctx.status(400);
            ctx.json("{\"error\":\"" + e.getMessage() + "\"}");
            System.out.println("Error creating message: " + e.getMessage());
        }
    }

    // 4: Our API should be able to retrieve all messages.
    private void getAllMessagesHandler(Context ctx) {
        List<Message> messages = messageService.getAllMessages();
        ctx.json(messages);
        ctx.status(200);
        System.out.println("Fetched all messages.");
    }

    // 5: Our API should be able to retrieve a message by its ID.
    private void getMessageByIdHandler(Context ctx) {
        int messageId = Integer.parseInt(ctx.pathParam("id"));
        Message message = messageService.getMessageById(messageId);
        if (message != null) {
            ctx.json(message);
            ctx.status(200);
            System.out.println("Fetched message with ID: " + messageId);
        } else {
            ctx.status(400);
            ctx.json("{\"error\":\"Message not found.\"}");
            System.out.println("Message not found with ID: " + messageId);
        }
    }

    // 6: Our API should be able to delete a message identified by a message ID.
    private void deleteMessageHandler(Context ctx) {
        int messageId = Integer.parseInt(ctx.pathParam("id"));
        boolean isDeleted = messageService.deleteMessage(messageId);
        if (isDeleted) {
            ctx.status(200); // No content
            System.out.println("Deleted message with ID: " + messageId);
        } else {
            ctx.status(400);
            ctx.json("{\"error\":\"Message not found.\"}");
            System.out.println("Message not found with ID: " + messageId);
        }
    }

    // 7: Our API should be able to update a message text identified by a message ID.
    private void updateMessageHandler(Context ctx) throws JsonProcessingException {
        int messageId = Integer.parseInt(ctx.pathParam("id"));
        ObjectMapper objectMapper = new ObjectMapper();
        Message updatedMessage = objectMapper.readValue(ctx.body(), Message.class);
        updatedMessage.setMessage_id(messageId); // Set the ID for updating

        boolean isUpdated = messageService.updateMessage(updatedMessage);
        if (isUpdated) {
            ctx.status(200);
            ctx.json("{\"message\":\"Message updated successfully.\"}");
            System.out.println("Updated message with ID: " + messageId);
        } else {
            ctx.status(400);
            ctx.json("{\"error\":\"Message not found.\"}");
            System.out.println("Message not found for updating with ID: " + messageId);
        }
    }

    // 8: Our API should be able to retrieve all messages written by a particular user.
    private void getMessagesByUserHandler(Context ctx) {
        int userId = Integer.parseInt(ctx.pathParam("userId"));
        List<Message> messages = messageService.getMessagesByUser(userId);
        ctx.json(messages);
        ctx.status(200);
        System.out.println("Fetched messages for user ID: " + userId);
    }
}
