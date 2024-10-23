package Controller;

import Service.AccountService;
import Service.MessageService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import Model.Account;
import Model.Message;

public class SocialMediaController {
    AccountService accountService;
    MessageService messageService;

    public SocialMediaController(){
        this.accountService = new AccountService();
        this.messageService = new MessageService();
    }

    public Javalin startAPI(){
        Javalin app = Javalin.create();

        app.post("/rgister", this::postRegisterHandler);

        return app;
    }

    private void postRegisterHandler(Context ctx) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Account account = objectMapper.readValue(ctx.body(), Account.class);
        if(!(account.getPassword().length() < 4 || account.getUsername().length() == 0) || accountService.getAllUsernames().contains(account.getUsername())){
            Account addedAccount = accountService.addAccount(account);
            ctx.json(objectMapper.writeValueAsString(addedAccount));
            ctx.status(200);
        }else{
            ctx.status(400);
        }
        
    }
}
