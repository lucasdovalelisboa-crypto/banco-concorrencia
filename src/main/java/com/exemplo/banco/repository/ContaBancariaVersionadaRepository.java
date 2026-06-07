package com.exemplo.banco.repository;

import com.exemplo.banco.entity.ContaBancariaVersionada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório JPA padrão para ContaBancaria.
 * Sem nenhuma customização — operações CRUD básicas fornecidas pelo Spring Data.
 */
@Repository
public interface ContaBancariaVersionadaRepository extends JpaRepository<ContaBancariaVersionada, Long> {
}
