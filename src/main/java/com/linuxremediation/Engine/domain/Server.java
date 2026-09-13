package com.linuxremediation.Engine.domain;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false)
    private Integer sshPort = 22;

    @Column(nullable = false)
    private String sshUser;

    @Column(nullable = true)
    private String privateKeyPath;

    @Column(nullable = true)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthType authType = AuthType.KEY;

    public enum AuthType {
        KEY,
        PASSWORD
    }
}
