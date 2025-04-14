package com.ftgo.user.userservice.repository;

import com.ftgo.user.userservice.entity.BaseUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<BaseUser, Long> {
    BaseUser findByUsername(String username);
    BaseUser findByEmail(String email);
    BaseUser findByPhoneNumber(String phoneNumber);

}
