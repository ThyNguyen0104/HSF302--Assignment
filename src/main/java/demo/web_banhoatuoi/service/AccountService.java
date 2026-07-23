package demo.web_banhoatuoi.service;

import demo.web_banhoatuoi.entity.Account;
import java.util.List;
import java.util.Optional;

public interface AccountService {

    List<Account> getAllAccounts();

    Optional<Account> getAccountById(int id);

    Optional<Account> getAccountByUserName(String userName);

    boolean existsByUserName(String userName);

    Account saveAccount(Account account);

    void deleteAccount(int id);
}
