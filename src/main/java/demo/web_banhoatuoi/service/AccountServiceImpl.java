package demo.web_banhoatuoi.service;

import demo.web_banhoatuoi.entity.Account;
import demo.web_banhoatuoi.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public Optional<Account> getAccountById(int id) {
        return accountRepository.findById(id);
    }

    @Override
    public Optional<Account> getAccountByUserName(String userName) {
        return accountRepository.findByUserName(userName);
    }

    @Override
    public boolean existsByUserName(String userName) {
        return accountRepository.existsByUserName(userName);
    }

    @Override
    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    @Override
    public void deleteAccount(int id) {
        accountRepository.deleteById(id);
    }


}
