package com.ftgo.user.persistence.repository;

import com.ftgo.user.persistence.document.AppUserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface AppUserRepository
        extends MongoRepository<AppUserDocument, String> {
    Optional<AppUserDocument> findByUsername(String username);
}

