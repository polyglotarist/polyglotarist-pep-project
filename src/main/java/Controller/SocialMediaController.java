package Controller;

import Service.AccountService;
import Service.MessageService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import Model.Account;
import Model.Message;

import java.sql.SQLException;
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
        app.post("/messages", this::addMessageHandler); 
        app.get("/messages", this::getAllMessagesHandler);
        app.get("/messages/{id}", this::getMessageByIdHandler); 
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
    //The creation of the message will be successful if and only if the message_text is not blank, is not over 255 characters,
    // and posted_by refers to a real, existing user

    private void addMessageHandler(Context ctx) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Message message = objectMapper.readValue(ctx.body(), Message.class);
        System.out.println("Received new message request.");


        if(message.getMessage_text().length() > 0 && message.getMessage_text().length() <= 255 
        && accountService.accountExists(message.getPosted_by())){
            ctx.status(200); // Created status
            Message newMessage = messageService.addMessage(message);
            
            ctx.json(newMessage);
            System.out.println("Message created successfully.");
        }else{
            ctx.status(400);
        }
        
    }

    // 4: Our API should be able to retrieve all messages.
    /**As a user, I should be able to submit a GET request on the endpoint GET localhost:8080/messages.
- The response body should contain a JSON representation of a list containing all messages retrieved from the database.
 It is expected for the list to simply be empty if there are no messages. The response status should always be 200, which is the default. */
    private void getAllMessagesHandler(Context ctx) {
        List<Message> messages = messageService.getAllMessages();
        ctx.json(messages);
        ctx.status(200);
        System.out.println("Fetched all messages.");
    }

// 5: Our API should be able to retrieve a message by its ID.
/*As a user, I should be able to submit a GET request on the endpoint GET localhost:8080/messages/{message_id}.
- The response body should contain a JSON representation of the message identified by the message_id.
 It is expected for the response body to simply be empty if there is no such message. 
 The response status should always be 200, which is the default. */
    private void getMessageByIdHandler(Context ctx) {
        int messageId = Integer.parseInt(ctx.pathParam("id"));
        Message retrievedMessage = messageService.getMessageById(messageId);
        Boolean result = false;
        if(retrievedMessage != null){
            result = true;
        }

        if (result) {
            ctx.status(200);
            ctx.json(retrievedMessage);
            System.out.println("Fetched message with ID: " + messageId);
        } else {
            ctx.status(400);
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
