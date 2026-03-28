package com.k.medtour.domain.aftercare.repository;

import com.k.medtour.domain.aftercare.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.items " +
            "WHERE i.journeyId = :journeyId AND i.deletedAt IS NULL")
    Optional<Invoice> findByJourneyIdWithItems(@Param("journeyId") Long journeyId);

    boolean existsByJourneyIdAndDeletedAtIsNull(Long journeyId);

    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.items " +
            "WHERE i.patientId = :patientId AND i.deletedAt IS NULL " +
            "ORDER BY i.issuedAt DESC")
    List<Invoice> findByPatientIdWithItems(@Param("patientId") Long patientId);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.deletedAt IS NULL")
    long countAllActive();
}
