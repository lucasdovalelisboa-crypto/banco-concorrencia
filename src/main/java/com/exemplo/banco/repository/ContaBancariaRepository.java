package com.exemplo.banco.repository;

import com.exemplo.banco.entity.ContaBancaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório JPA padrão para ContaBancaria.
 * Sem nenhuma customização — operações CRUD básicas fornecidas pelo Spring Data.
 */
@Repository
public interface ContaBancariaRepository extends JpaRepository<ContaBancaria, Long> {
}
