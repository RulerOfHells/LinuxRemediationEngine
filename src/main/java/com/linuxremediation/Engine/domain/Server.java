package com.linuxremediation.Engine.domain;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.AssertTrue;

@Entity
@Table(name = "servers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Server {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String hostname;

    @Column(nullable = false)
    private String ipAddress;

    @Builder.Default
    @Column(nullable = false)
    private Integer sshPort = 22;

    @Column(nullable = false)
    private String sshUser;

    private String privateKeyPath;
    private String password;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthType authType = AuthType.KEY;

    public enum AuthType {
        KEY,
        PASSWORD
    }

    @AssertTrue(message = "Either privateKeyPath or password must be provided based on the authType")
    private boolean isValidAuthCredentials() {
        if (authType == AuthType.KEY) {
            return privateKeyPath != null && !privateKeyPath.isBlank();
        } else if (authType == AuthType.PASSWORD) {
            return password != null && !password.isBlank();
        }
        return false;
    }
}
