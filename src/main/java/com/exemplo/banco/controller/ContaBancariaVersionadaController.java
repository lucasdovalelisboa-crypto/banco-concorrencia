package com.exemplo.banco.controller;

import com.exemplo.banco.entity.ContaBancariaVersionada;
import com.exemplo.banco.service.ContaBancariaVersionadaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Controller REST para operações de conta bancária.
 * Expõe endpoints conforme especificação do trabalho.
 */
@RestController
@RequestMapping("/contas-versionadas")
public class ContaBancariaVersionadaController {

    private final ContaBancariaVersionadaService service;

    public ContaBancariaVersionadaController(ContaBancariaVersionadaService service) {
        this.service = service;
    }

    /**
     * GET /contas-versionadas/{id}
     * Retorna os dados atuais da conta (útil para verificar saldo durante testes).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContaBancariaVersionada> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    /**
     * POST /contas-versionadas/{id}/deposito
     * Body: { "valor": 100.00 }
     *
     * Adiciona o valor ao saldo da conta.
     */
    @PostMapping("/{id}/deposito")
    public ResponseEntity<?> depositar(@PathVariable Long id,
                                       @RequestBody Map<String, BigDecimal> body) {
        try {
            BigDecimal valor = body.get("valor");
            ContaBancariaVersionada atualizada = service.depositar(id, valor);
            return ResponseEntity.ok(Map.of(
                "mensagem", "Depósito realizado com sucesso",
                "conta", atualizada
            ));
            
            // PONTO DE ATENÇÃO: Tratamento exigido pela Parte 2
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT).body(Map.of(
                    "erro", "Conflito de concorrência: A conta foi atualizada por outra transação simultânea. Tente novamente."
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /contas-versionadas/{id}/saque
     * Body: { "valor": 50.00 }
     *
     * Reduz o saldo da conta. Lança erro se saldo insuficiente.
     */
    @PostMapping("/{id}/saque")
    public ResponseEntity<?> sacar(@PathVariable Long id,
                                   @RequestBody Map<String, BigDecimal> body) {
        try {
            BigDecimal valor = body.get("valor");
            ContaBancariaVersionada atualizada = service.sacar(id, valor);
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
