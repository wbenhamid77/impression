package com.example.Impression.repositories;

import com.example.Impression.entities.PasswordResetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, UUID> {

    List<PasswordResetCode> findByUtilisateur_IdAndUtiliseFalse(UUID utilisateurId);

    Optional<PasswordResetCode> findByUtilisateur_EmailAndCodeAndUtiliseFalse(String email, String code);
}
