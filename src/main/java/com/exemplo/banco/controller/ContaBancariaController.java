package com.exemplo.banco.controller;

import com.exemplo.banco.entity.ContaBancaria;
import com.exemplo.banco.service.ContaBancariaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Controller REST para operações de conta bancária.
 * Expõe endpoints conforme especificação do trabalho.
 */
@RestController
@RequestMapping("/contas")
public class ContaBancariaController {

    private final ContaBancariaService service;

    public ContaBancariaController(ContaBancariaService service) {
        this.service = service;
    }

    /**
     * GET /contas/{id}
     * Retorna os dados atuais da conta (útil para verificar saldo durante testes).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContaBancaria> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    /**
     * POST /contas/{id}/deposito
     * Body: { "valor": 100.00 }
     *
     * Adiciona o valor ao saldo da conta.
     */
    @PostMapping("/{id}/deposito")
    public ResponseEntity<?> depositar(@PathVariable Long id,
                                       @RequestBody Map<String, BigDecimal> body) {
        try {
            BigDecimal valor = body.get("valor");
            ContaBancaria atualizada = service.depositar(id, valor);
            return ResponseEntity.ok(Map.of(
                "mensagem", "Depósito realizado com sucesso",
                "conta", atualizada
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /contas/{id}/saque
     * Body: { "valor": 50.00 }
     *
     * Reduz o saldo da conta. Lança erro se saldo insuficiente.
     */
    @PostMapping("/{id}/saque")
    public ResponseEntity<?> sacar(@PathVariable Long id,
                                   @RequestBody Map<String, BigDecimal> body) {
        try {
            BigDecimal valor = body.get("valor");
            ContaBancaria atualizada = service.sacar(id, valor);
            return ResponseEntity.ok(Map.of(
                "mensagem", "Saque realizado com sucesso",
                "conta", atualizada
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
