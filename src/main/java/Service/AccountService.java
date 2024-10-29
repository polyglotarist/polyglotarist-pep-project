package Service;

import java.util.List;

import DAO.AccountDAO;
import Model.Account;

public class AccountService {

    private final AccountDAO accountDAO;

    public AccountService() {
        accountDAO = new AccountDAO();
    }

    public AccountService(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    public Account addAccount(Account account) {
        // Inserts a new account into the database via the DAO and returns the added account
        return accountDAO.insertAccount(account);
    }

    public List<String> getAllUsernames() {
        // Retrieves all usernames from the DAO
        return accountDAO.getAllUsernames();
    }

    public boolean validateUser(String username, String password) {
        // Validates a user's credentials by checking the DAO
        Boolean result = false;
        Account account = accountDAO.getAccountByUsername(username);
        if(account != null && account.getPassword().equals(password)){
            result = true;
        }
        return result;
    }

    public Account getAccountByUsername(String username) {
        return accountDAO.getAccountByUsername(username);
    }
    public Boolean accountExists(String username) {
        if(accountDAO.accountExists(username)){
            return true;
        }
        return false;
    }

    public Boolean accountExists(int id) {
        if(accountDAO.accountExists(id)){
            return true;
        }
        return false;
    }

    public Account getAccountById(int id) {
        return accountDAO.getAccountById(id);
    }

  
    
}
