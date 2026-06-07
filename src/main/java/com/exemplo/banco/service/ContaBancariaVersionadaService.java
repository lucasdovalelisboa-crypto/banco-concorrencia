package com.exemplo.banco.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exemplo.banco.entity.ContaBancariaVersionada;
import com.exemplo.banco.repository.ContaBancariaVersionadaRepository;

/**
 * ============================================================
 * PARTE 2 - ALUNO B: COM CONTROLE DE CONCORRÊNCIA OTIMISTA
 * ============================================================
 * Serviço que gerencia as operações da ContaBancariaVersionada.
 * Utiliza a anotação @Version na entidade para evitar o problema de Lost Update (Atualização Perdida). 
 * Se múltiplas threads tentarem modificar o mesmo registro ao mesmo tempo, 
 * o JPA lançará uma ObjectOptimisticLockingFailureException.
 * ============================================================
 */
@Service
public class ContaBancariaVersionadaService {

    private final ContaBancariaVersionadaRepository repository;

    public ContaBancariaVersionadaService(ContaBancariaVersionadaRepository repository) {
        this.repository = repository;
    }

    /**
     * Busca uma conta pelo ID.
     */
    @Transactional(readOnly = true)
    public ContaBancariaVersionada buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada: " + id));
    }

     /**
     * Realiza um depósito COM controle de concorrência otimista (@Version).
     *
     * Previne o problema de Lost Update. Se múltiplas threads tentarem
     * atualizar o mesmo registro simultaneamente, o JPA lançará uma exceção.
     */
    @Transactional
    public ContaBancariaVersionada depositar(Long id, BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor de depósito deve ser positivo.");
        }

        // 1. Lê o saldo atual do banco (incluindo a versão atual)
        ContaBancariaVersionada conta = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada: " + id));

        // 2. Calcula novo saldo em memória 
        BigDecimal novoSaldo = conta.getSaldo().add(valor);
        conta.setSaldo(novoSaldo);

        // 3. Salva — Se outra thread modificou o saldo enquanto estávamos no passo 2,
        // o framework detectará a mudança de versão e lançará ObjectOptimisticLockingFailureException
        return repository.save(conta);
    }

    /**
     * Realiza um saque COM controle de concorrência otimista (@Version).
     * Previne o Lost Update e garante que a validação de saldo não seja burlada por requisições simultâneas.
     */
    @Transactional
    public ContaBancariaVersionada sacar(Long id, BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor de saque deve ser positivo.");
        }

        ContaBancariaVersionada conta = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada: " + id));

        // Validação de saldo suficiente — Agora protegida pela concorrência.
        // Se outra thread sacar dinheiro entre essa verificação e o save() abaixo,
        // a versão no banco vai mudar e o Hibernate barrará esta transação.
        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new IllegalStateException(
                "Saldo insuficiente. Saldo atual: " + conta.getSaldo() + ", tentativa de saque: " + valor
            );
        }

        BigDecimal novoSaldo = conta.getSaldo().subtract(valor);
        conta.setSaldo(novoSaldo);
        
        // Salva — Se a versão da conta foi alterada por outra thread no meio do processo,
        // lançará a ObjectOptimisticLockingFailureException, impedindo o saldo de ficar negativo.
        return repository.save(conta);
    }
}
