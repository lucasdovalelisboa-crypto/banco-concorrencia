package com.exemplo.banco.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exemplo.banco.entity.ContaBancaria;
import com.exemplo.banco.repository.ContaBancariaRepository;

/**
 * ============================================================
 * PARTE 1 - ALUNO A: SEM CONTROLE DE CONCORRÊNCIA
 * ============================================================
 *
 * Apenas @Transactional básico — nenhum mecanismo de bloqueio.
 *
 * PROBLEMA ESPERADO (Lost Update / Atualização Perdida):
 * ---------------------------------------------------------
 * Thread A lê saldo = 1000
 * Thread B lê saldo = 1000  ← lê o MESMO valor antes de A salvar
 * Thread A deposita 100 → salva saldo = 1100
 * Thread B deposita 200 → salva saldo = 1200  ← SOBRESCREVE o depósito de A!
 *
 * Saldo final correto deveria ser: 1300
 * Saldo real no banco:              1200  ← INCONSISTÊNCIA!
 * ============================================================
 */
@Service
public class ContaBancariaService {

    private final ContaBancariaRepository repository;

    public ContaBancariaService(ContaBancariaRepository repository) {
        this.repository = repository;
    }

    /**
     * Busca uma conta pelo ID.
     */
    @Transactional(readOnly = true)
    public ContaBancaria buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada: " + id));
    }

    /**
     * Realiza um depósito SEM controle de concorrência.
     *
     * Vulnerável a Lost Update quando chamado por múltiplas threads.
     */
    @Transactional
    public ContaBancaria depositar(Long id, BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor de depósito deve ser positivo.");
        }

        // 1. Lê o saldo atual do banco
        ContaBancaria conta = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada: " + id));

        // 2. Calcula novo saldo em memória (PONTO CRÍTICO: outra thread pode ter
        //    modificado o saldo entre o findById e o save abaixo!)
        BigDecimal novoSaldo = conta.getSaldo().add(valor);
        conta.setSaldo(novoSaldo);

        // 3. Salva — pode sobrescrever mudança de outra thread
        return repository.save(conta);
    }

    /**
     * Realiza um saque SEM controle de concorrência.
     *
     * Vulnerável a Lost Update e também a saldo negativo sob alta concorrência.
     */
    @Transactional
    public ContaBancaria sacar(Long id, BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor de saque deve ser positivo.");
        }

        ContaBancaria conta = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada: " + id));

        // Validação de saldo suficiente — mas pode falhar sob concorrência:
        // duas threads podem passar essa verificação ao mesmo tempo!
        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new IllegalStateException(
                "Saldo insuficiente. Saldo atual: " + conta.getSaldo() + ", tentativa de saque: " + valor
            );
        }

        BigDecimal novoSaldo = conta.getSaldo().subtract(valor);
        conta.setSaldo(novoSaldo);

        return repository.save(conta);
    }
}
