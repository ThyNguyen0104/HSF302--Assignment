package demo.web_banhoatuoi.data_init;

import demo.web_banhoatuoi.entity.Account;
import demo.web_banhoatuoi.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

@Component
@Order(0) // Run first
public class Account_data implements CommandLineRunner {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public void run(String... args) throws Exception {
        if (accountRepository.count() == 0) {

            // === ADMIN ACCOUNTS ===
            Account admin1 = new Account();
            admin1.setUserName("admin1");
            admin1.setPassword("1234");
            admin1.setRole("ADMIN");

            Account admin2 = new Account();
            admin2.setUserName("admin2");
            admin2.setPassword("1234");
            admin2.setRole("ADMIN");

            Account admin3 = new Account();
            admin3.setUserName("admin3");
            admin3.setPassword("1234");
            admin3.setRole("ADMIN");

            // === USER ACCOUNTS ===
            Account user1 = new Account();
            user1.setUserName("user1");
            user1.setPassword("1234");
            user1.setRole("USER");

            Account user2 = new Account();
            user2.setUserName("user2");
            user2.setPassword("1234");
            user2.setRole("USER");

            Account user3 = new Account();
            user3.setUserName("user3");
            user3.setPassword("1234");
            user3.setRole("USER");

            // Save all accounts
            accountRepository.save(admin1);
            accountRepository.save(admin2);
            accountRepository.save(admin3);
            accountRepository.save(user1);
            accountRepository.save(user2);
            accountRepository.save(user3);

            System.out.println("✅ Created 6 accounts successfully!");
            System.out.println("   ADMIN accounts: admin1/1234, admin2/1234, admin3/1234");
            System.out.println("   USER accounts: user1/1234, user2/1234, user3/1234");
        }
    }
}