package com.ftgo.user.persistence.entity;

import com.ftgo.user.persistence.entity.converter.GrantedAuthorityConverter;
import com.ftgo.user.persistence.entity.enumaration.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "User", indexes = {
        @Index(name = "idx_user_username", columnList = "username", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    private Long id;

    @Column(columnDefinition = "VARCHAR2(255)", nullable = false, unique = true)
    private String username;

    @Column(columnDefinition = "VARCHAR2(255)", nullable = false)
    private String password;

    @Column(columnDefinition = "VARCHAR2(255)")
    private String email;

    @Column(columnDefinition = "VARCHAR2(255)", unique = true)
    private String phoneNumber;

    @Column(columnDefinition = "VARCHAR2(64)", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;
}
