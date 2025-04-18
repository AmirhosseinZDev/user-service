package com.ftgo.user.userservice.entity;

import com.ftgo.user.userservice.entity.converter.GrantedAuthorityConverter;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Entity
public class BaseUser implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    private Long id;
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    @ElementCollection
    @CollectionTable(name = "user_authorities", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "authority")
    @Convert(converter = GrantedAuthorityConverter.class)
    private List<GrantedAuthority> authorities;

    public BaseUser() {
    }

    public BaseUser(String username, String password, String email, String phoneNumber) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }


}
