# 🧪 Guia de Testes - JMeter (Aluno A)
## Concorrência SEM Bloqueio — Evidenciando o Lost Update

---

## 1. Iniciando a Aplicação

```bash
# Na raiz do projeto:
./mvnw spring-boot:run

# Ou em IDE: execute BancoConcorrenciaApplication.java
```

**Verificar se está rodando:**
```
GET http://localhost:8080/contas/1
```
Resposta esperada:
```json
{ "id": 1, "titular": "João Silva", "saldo": 1000.00 }
```

---

## 2. Testando manualmente (antes do JMeter)

### Depósito de R$ 100
```
POST http://localhost:8080/contas/1/deposito
Content-Type: application/json

{ "valor": 100.00 }
```

### Saque de R$ 50
```
POST http://localhost:8080/contas/1/saque
Content-Type: application/json

{ "valor": 50.00 }
```

---

## 3. Configurando o JMeter

### Passo a passo:

**A) Criar Plano de Teste**
1. Abra o JMeter
2. Clique com botão direito em "Test Plan" → Add → Threads (Users) → **Thread Group**

**B) Configurar o Thread Group**
| Campo | Valor |
|-------|-------|
| Number of Threads (users) | **50** |
| Ramp-Up Period (seconds) | **1** |
| Loop Count | **10** |

> Isso vai simular 50 usuários fazendo 10 requisições cada = **500 requisições simultâneas**

**C) Adicionar HTTP Request (Depósito)**
- Botão direito no Thread Group → Add → Sampler → **HTTP Request**
- Configurar:
  - Method: `POST`
  - Server Name: `localhost`
  - Port: `8080`
  - Path: `/contas/1/deposito`
  - Body Data: `{"valor": 10.00}`
- Aba "HTTP Headers" → Add Header:
  - Name: `Content-Type`
  - Value: `application/json`

**D) Adicionar HTTP Request (Saque)**
- Repetir o passo C com path `/contas/1/saque` e `{"valor": 5.00}`

**E) Adicionar Listeners (para ver resultados)**
- Botão direito no Thread Group → Add → Listener:
  - **View Results Tree** (ver cada requisição)
  - **Summary Report** (ver totais)
  - **Aggregate Report** (latência, throughput)

---

## 4. Calculando o Resultado Esperado vs Real

### Cenário do Teste:
- Saldo inicial: **R$ 1.000,00**
- 50 threads × 10 loops = 500 depósitos de R$ 10,00
- 50 threads × 10 loops = 500 saques de R$ 5,00 (em Thread Group separado)

### Cálculo correto (sem concorrência):
```
Saldo inicial:          R$ 1.000,00
+ 500 depósitos × 10:  R$ 5.000,00
- 500 saques × 5:      R$ 2.500,00
                       -----------
Saldo final esperado:  R$ 3.500,00
```

### O que acontece na prática (Lost Update):
O saldo final será **menor que R$ 3.500,00** porque várias threads:
1. Leem o mesmo saldo simultaneamente
2. Calculam em cima do mesmo valor base
3. Sobrescrevem uma à outra no momento do `save()`

---

## 5. Evidenciando o Problema (Prints para o Trabalho)

### Print 1 — Console do Spring Boot
Mostrar os SQLs `SELECT` e `UPDATE` intercalados:
```sql
-- Thread A faz SELECT:
select * from conta_bancaria where id=1  → saldo = 1000

-- Thread B faz SELECT (ao mesmo tempo):
select * from conta_bancaria where id=1  → saldo = 1000  ← MESMO valor!

-- Thread A salva:
update conta_bancaria set saldo=1100 where id=1

-- Thread B salva (SOBRESCREVE o trabalho de A!):
update conta_bancaria set saldo=1200 where id=1  ← deveria ser 1300!
```

### Print 2 — Console H2 após o teste
Acesse: http://localhost:8080/h2-console
- URL: `jdbc:h2:mem:bancodb`
- Execute: `SELECT * FROM CONTA_BANCARIA WHERE ID = 1;`
- Tire print mostrando o saldo final inconsistente

### Print 3 — JMeter Summary Report
- Mostrar total de requisições enviadas
- Comparar com o saldo final no banco

---

## 6. Diagrama do Problema (para a apresentação)

```
THREAD A                    THREAD B
   |                            |
   |-- SELECT saldo = 1000 --→  |
   |                            |-- SELECT saldo = 1000 --→ (lê o mesmo!)
   |                            |
   |-- saldo + 100 = 1100       |-- saldo + 200 = 1200
   |                            |
   |-- UPDATE saldo = 1100 --→  |
   |                            |-- UPDATE saldo = 1200 --→ (SOBRESCREVE!)
   |                            |
   
   Saldo correto seria: 1300
   Saldo real no banco:  1200  ← LOST UPDATE!
```

---

## 7. Conclusão para o Relatório

O problema ocorre porque:
1. `@Transactional` básico garante atomicidade de cada operação isolada
2. Mas **não impede** que duas transações leiam o mesmo dado simultaneamente
3. A última a salvar sobrescreve o resultado da primeira (**Last Write Wins**)
4. Isso caracteriza o fenômeno de **Lost Update** (Atualização Perdida)

**Solução** (Aluno B): Utilizar `@Lock(LockModeType.PESSIMISTIC_WRITE)` ou
`@Version` (Optimistic Locking) para garantir acesso serializado ao registro.


#### 8. Configurando o JMeter para a Conta Versionada
Para comprovar o funcionamento da solução, criamos um novo cenário apontando para os novos endpoints implementados.

**A) Configurar o Novo Thread Group**
* No JMeter, foi criado um novo **Thread Group** (Cenário 2 - Controle Otimista) para simular o ataque à nova API.
* Os parâmetros de estresse foram intensificados e configurados para rodarem saques e depósitos simultaneamente:
  * **Number of Threads (users):** 100
  * **Ramp-Up Period (seconds):** 1
  * **Loop Count:** 10

**B) Alterar os Endpoints (Paths)**
As requisições HTTP foram adaptadas para acessar a entidade controlada.
* **HTTP Request (Depósito):** O Path mudou de `/contas/1/deposito` para `/contas-versionadas/1/deposito`
* **HTTP Request (Saque):** O Path mudou de `/contas/1/saque` para `/contas-versionadas/1/saque`

**C) Configuração do Header (HTTP Header Manager)**
Para que a requisição POST envie o corpo (body) no formato JSON corretamente sem causar erros de formatação no Spring Boot, é obrigatório adicionar um Gerenciador de Cabeçalhos.

**Passo a passo para adicionar:**
1. Clique com o botão direito sobre o seu **Thread Group** (ou diretamente sobre a sua Requisição HTTP).
2. Vá em **Add** (Adicionar) > **Config Element** (Elemento de Configuração) > **HTTP Header Manager** (Gerenciador de Cabeçalhos HTTP).
3. Na tela do *HTTP Header Manager*, clique no botão **Add** (Adicionar) localizado na parte inferior.
4. Preencha os campos da nova linha que aparecerá da seguinte forma:
   * **Name:** `Content-Type`
   * **Value:** `application/json`

*Nota: Se você adicionar o Header Manager diretamente no nível do Thread Group, ele aplicará essa configuração automaticamente para as duas requisições (Depósito e Saque).*

**D) Execução e Análise via Summary Report**
* Diferente do cenário sem controle que retornava 100% de status "200 OK" mascarando os erros (Lost Update), a configuração da Conta Versionada barra ativamente a sobrescrita.
* Ao checar o listener **Summary Report**, a leitura dos erros demonstra a proteção em funcionamento: as requisições que causam colisão de concorrência falham com **Erro HTTP 409 (Conflict)**, garantindo que o saldo seja calculado apenas em cima das requisições seguras.
```