package demo.web_banhoatuoi.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @Column(name = "account_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int accountId; // Use camelCase

    @Column(name = "user_name")
    private String userName; // Use camelCase

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    private String role;
}