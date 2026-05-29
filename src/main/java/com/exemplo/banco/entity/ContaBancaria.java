package com.exemplo.banco.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entidade que representa uma Conta Bancária.
 *
 * PARTE 1 (Aluno A): Implementação PADRÃO, sem nenhum controle de concorrência.
 * Isso permite demonstrar o problema de Lost Update (Atualização Perdida)
 * quando múltiplas threads acessam o mesmo registro simultaneamente.
 */
@Entity
@Table(name = "conta_bancaria")
public class ContaBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titular;

    /**
     * Saldo monetário: SEMPRE BigDecimal para valores financeiros.
     * Usar double/float causaria erros de arredondamento.
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;

    // Construtores
    public ContaBancaria() {}

    public ContaBancaria(String titular, BigDecimal saldo) {
        this.titular = titular;
        this.saldo = saldo;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitular() { return titular; }
    public void setTitular(String titular) { this.titular = titular; }

    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }

    @Override
    public String toString() {
        return "ContaBancaria{id=" + id + ", titular='" + titular + "', saldo=" + saldo + "}";
    }
}
