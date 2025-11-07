package com.example.Impression.repositories;

import com.example.Impression.entities.TransactionInstruction;
import com.example.Impression.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionInstructionRepository extends JpaRepository<TransactionInstruction, UUID> {
        List<TransactionInstruction> findByStatutOrderByDateCreationAsc(TransactionStatus statut);

        List<TransactionInstruction> findByReservationIdOrderByDateCreationAsc(UUID reservationId);

        List<TransactionInstruction> findByPaiementIdOrderByDateCreationAsc(UUID paiementId);

        List<TransactionInstruction> findByToRibIdInAndStatutOrderByDateCreationAsc(List<UUID> toRibIds,
                        TransactionStatus statut);

        List<TransactionInstruction> findByFromRibIdInAndStatutOrderByDateCreationAsc(List<UUID> fromRibIds,
                        TransactionStatus statut);
}
