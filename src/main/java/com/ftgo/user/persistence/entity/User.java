package com.ftgo.user.persistence.entity;

import com.ftgo.user.persistence.entity.converter.GrantedAuthorityConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "User", indexes = {
        @Index(name = "idx_user_username", columnList = "username", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    private String email;

    private String phoneNumber;

    @ElementCollection
    @CollectionTable(name = "user_authorities", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "authority")
    @Convert(converter = GrantedAuthorityConverter.class)
    private List<GrantedAuthority> authorities;
}
