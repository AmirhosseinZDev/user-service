package com.ftgo.user.persistence.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * A JPA attribute converter for converting between {@link GrantedAuthority} and its String representation.
 * <p>
 * This converter is used to persist GrantedAuthority objects as Strings in the database and
 * to convert them back to GrantedAuthority objects when reading from the database.
 * <p>
 * The authorities are stored as a comma-separated string in the database and are converted
 * back to {@link GrantedAuthority} when the User entity is loaded.
 */
@Converter(autoApply = true)
public class GrantedAuthorityConverter implements AttributeConverter<GrantedAuthority, String> {

    // This class is used to convert GrantedAuthority to String and vice versa
    // It is used in the User entity to store the authorities in the database
    // The authorities are stored as a comma separated string in the database
    // The authorities are converted back to GrantedAuthority when the User is loaded from the database

    // Convert GrantedAuthority to String
    @Override
    public String convertToDatabaseColumn(GrantedAuthority attribute) {
        return attribute.getAuthority();
    }

    // Convert String to GrantedAuthority
    @Override
    public GrantedAuthority convertToEntityAttribute(String dbData) {
        return new SimpleGrantedAuthority(dbData);
    }
}
