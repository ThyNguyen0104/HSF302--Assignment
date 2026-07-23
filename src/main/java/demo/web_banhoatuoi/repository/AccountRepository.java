package demo.web_banhoatuoi.repository;

import demo.web_banhoatuoi.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    // Find account by username (for login)
    Optional<Account> findByUserName(String userName);

    // Check if username exists (for registration/validation)
    boolean existsByUserName(String userName);
}