package com.ftgo.user.persistence.entity;

import com.ftgo.user.persistence.entity.enumaration.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
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

    @Column(columnDefinition = "VARCHAR2(255)")
    private String phoneNumber;

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @JoinTable(name = "USER_ROLES", joinColumns = @JoinColumn(name = "USER_ID"))
    @Column(name = "ROLE", columnDefinition = "VARCHAR2(64)", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();
}
