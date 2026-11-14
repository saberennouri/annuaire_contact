package com.sfax2.annuaire.repo;

import com.sfax2.annuaire.domain.Establishment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EstablishmentRepository
        extends JpaRepository<Establishment, Long>, JpaSpecificationExecutor<Establishment> {
}
