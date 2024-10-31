package Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        app.patch("/messages/{message_id}", this::updateMessageHandler); 
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
    private void getAllMessagesHandler(Context ctx) {
        List<Message> messages = messageService.getAllMessages();
        ctx.json(messages);
        ctx.status(200);
        System.out.println("Fetched all messages.");
    }

// 5: Our API should be able to retrieve a message by its ID.
    private void getMessageByIdHandler(Context ctx) {
        int messageId = Integer.parseInt(ctx.pathParam("id"));
        Message retrievedMessage = messageService.getMessageById(messageId); 
        ctx.status(200);
        if(retrievedMessage != null){
            ctx.json(retrievedMessage);
        }
    }

// 6: Our API should be able to delete a message identified by a message ID.
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
    private void updateMessageHandler(Context ctx) throws JsonProcessingException {

    //step1: retrieve what's passed in the ctx object, namely the id, and the newText
    int retrievedId = Integer.parseInt(ctx.pathParam("message_id"));
    //step2: validate that the id corresponds to an existing message
    boolean isValidId = false;
    if(messageService.getMessageById(retrievedId) != null){
        isValidId = true;
    }
     //step0: store old message_text in a variable to compare with new text later:
    //step3: validate that the newText passed in is neither blank nor exceeding 255 characters after retrieving it from the passed ctx object
    String retrievedNewText = ctx.bodyAsClass(Message.class).getMessage_text();
    boolean isValidText = false;
    if(retrievedNewText.length() > 0 && retrievedNewText.length() <= 255){
        isValidText = true;
    }
    //step4: set the Message object's message_text to the new text
    Message oldMessage = messageService.getMessageById(retrievedId);
    if(retrievedNewText != null && isValidId && isValidText){
        oldMessage.setMessage_text(retrievedNewText);
    }
    // after we updated the old message we are labeling it appropriately for clarity
    Message updatedMessage = oldMessage; 
    //step5: pass the object to the service layer for update
    Message messageReturnedFromUpdate = null;
    if(updatedMessage != null){
        messageReturnedFromUpdate = messageService.updateMessage(updatedMessage);
    }
    //step6: ensure that the service layer calls the dao layer with the passed object
        //done
    //step7: check that the sql querry updates the existing object's message_text in the database
        //done
    //step8: if the update is successful, display status code 200 and a json representation of the updated Message object
    if(isValidId && isValidText){
        ctx.status(200);
        ctx.json(messageReturnedFromUpdate);
    //step9: display status code 400 if text not valid or id not valid
    }else {
        ctx.status(400);
    }
}

    // 8: Our API should be able to retrieve all messages written by a particular user.
    private void getMessagesByUserHandler(Context ctx) {
        int accountId = Integer.parseInt(ctx.pathParam("account_id"));
        List<Message> messages = messageService.getMessagesByUser(accountId);
        ctx.json(messages);
        ctx.status(200);

    }
}
