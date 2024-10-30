package Controller;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import Model.Account;
import Model.Message;
import Service.AccountService;
import Service.MessageService;
import io.javalin.Javalin;
import io.javalin.http.Context;

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
        app.delete("/messages/{id}", this::deleteMessageHandler); 
        app.patch("/messages/{id}", this::updateMessageHandler); 
        app.get("/accounts/{account_id}/messages", this::getMessagesByUserHandler); 

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
        ctx.status(200);
        if(retrievedMessage != null){
            ctx.json(retrievedMessage);
        }
    }

    // 6: Our API should be able to delete a message identified by a message ID.
    /*As a User, I should be able to submit a DELETE request on the endpoint DELETE localhost:8080/messages/{message_id}.

- The deletion of an existing message should remove an existing message from the database. 
If the message existed, the response body should contain the now-deleted message. 
The response status should be 200, which is the default.
- If the message did not exist, the response status should be 200, but the response body should be empty.
 This is because the DELETE verb is intended to be idempotent, ie, multiple calls to the DELETE endpoint should respond 
 with the same type of response. */
    private void deleteMessageHandler(Context ctx) {
        int messageId = Integer.parseInt(ctx.pathParam("id"));
        Message deletedMessage = messageService.getMessageById(messageId);
        Boolean isDeleted = messageService.deleteMessage(messageId);
        ctx.status(200);
        if(isDeleted){
            ctx.json(deletedMessage);
        }
    }

// 7: Our API should be able to update a message text identified by a message ID.
/*As a user, I should be able to submit a PATCH request on the endpoint PATCH localhost:8080/messages/{message_id}.
The request body should contain a new message_text values to replace the message identified by message_id.
The request body can not be guaranteed to contain any other information.
- The update of a message should be successful if and only if the message id already exists and the new message_text is not blank 
and is not over 255 characters.
If the update is successful, the response body should contain the full updated message (including message_id, posted_by, message_text,
and time_posted_epoch), and the response status should be 200, which is the default. 
The message existing on the database should have the updated message_text.
- If the update of the message is not successful for any reason, the response status should be 400. (Client error) */
    private void updateMessageHandler(Context ctx) throws JsonProcessingException {
        int messageId = Integer.parseInt(ctx.pathParam("id"));
        //check if the messageId exists:
        boolean isValidId = false;
        Message nonUpdatedMessage = messageService.getMessageById(messageId);
        if(nonUpdatedMessage != null){
            isValidId = true;
        }
        //check if new message text is valid: must be not be blank and must be less than 256
        boolean isValidText = false;
        String newText = ctx.bodyAsClass(Message.class).getMessage_text();
        if(newText.length() > 0 && newText.length() < 256){
            isValidText = true;
        }
        //perform the update if id is valid and text is valid:
        Message theNewMessageObject = nonUpdatedMessage;
        if(isValidId && isValidText){
            nonUpdatedMessage.setMessage_text(newText);
            theNewMessageObject = nonUpdatedMessage;
            theNewMessageObject = messageService.updateMessage(theNewMessageObject);
        }
        //if update is successful, return status 200 and json of updated message object:
        if(theNewMessageObject != null && theNewMessageObject.getMessage_text() != nonUpdatedMessage.getMessage_text()){
            ctx.status(200);
            ctx.json(theNewMessageObject);
        }else{
            ctx.status(400);
        }
    }

    // 8: Our API should be able to retrieve all messages written by a particular user.
    /*As a user, I should be able to submit a GET request on the endpoint GET localhost:8080/accounts/{account_id}/messages.
- The response body should contain a JSON representation of a list containing all messages posted by a particular user, 
which is retrieved from the database. It is expected for the list to simply be empty if there are no messages.
 The response status should always be 200, which is the default. */

    private void getMessagesByUserHandler(Context ctx) {
        int accountId = Integer.parseInt(ctx.pathParam("account_id"));
        List<Message> messages = messageService.getMessagesByUser(accountId);
        ctx.json(messages);
        ctx.status(200);

    }
}
