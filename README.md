# Banco Concorrência - Spring Boot

## Integrantes da dupla

* Lucas do Vale Lisboa → Parte 1 (Sem controle de concorrência)
* Leonei Maciel Cardoso Júnior → Parte 2 (Controle de concorrência com @Version)

---

# Objetivo

Demonstrar na prática problemas de concorrência em sistemas transacionais utilizando Spring Boot, JPA/Hibernate e banco H2.

O projeto apresenta dois cenários:

* Parte 1 → sistema sem controle de concorrência
* Parte 2 → sistema utilizando controle otimista com `@Version`

---

# Tecnologias utilizadas

* Java 21
* Spring Boot 3.3.5
* Spring Data JPA
* H2 Database
* Maven
* Apache JMeter

---

# Como executar o projeto

## 1. Clonar o repositório

```bash
git clone https://github.com/lucasdovalelisboa-crypto/banco-concorrencia.git
```

---

## 2. Entrar na pasta do projeto

```bash
cd banco-concorrencia
```

---

## 3. Executar a aplicação

```bash
mvn spring-boot:run
```

---

# Console H2

Acessar:

```text
http://localhost:8080/h2-console
```

Configuração:

| Campo     | Valor               |
| --------- | ------------------- |
| JDBC URL  | jdbc:h2:mem:bancodb |
| User Name | sa                  |
| Password  | (vazio)             |

---

# Endpoints

## Buscar conta

```http
GET /contas/{id}
```

---

## Depositar

```http
POST /contas/{id}/deposito
```

Body:

```json
{
  "valor": 10
}
```

---

## Sacar

```http
POST /contas/{id}/saque
```

Body:

```json
{
  "valor": 10
}
```

---

# Parte 1 — Sem Controle de Concorrência

Nesta etapa foi implementada a entidade `ContaBancaria` sem nenhum mecanismo de controle de concorrência, utilizando apenas `@Transactional`.

O objetivo foi demonstrar o problema de:

* Lost Update (Atualização Perdida)
* Inconsistência de saldo
* Sobrescrita de operações simultâneas

---

# Testes com Apache JMeter

Foi criado um cenário de concorrência utilizando múltiplas threads simultâneas.

## Configuração utilizada

| Configuração      | Valor |
| ----------------- | ----- |
| Threads           | 100   |
| Loops             | 20    |
| Valor do depósito | 10    |

---

# Resultado Esperado

Saldo inicial:

```text
1000
```

Saldo esperado após os depósitos concorrentes:

```text
21000
```

---

# Resultado Obtido

Após a execução concorrente no JMeter, o saldo final encontrado foi:

```text
4630
```

---

# Conclusão da Parte 1

O sistema apresentou inconsistência de saldo devido à ausência de controle de concorrência.

Múltiplas threads acessaram e atualizaram o mesmo registro simultaneamente, ocasionando o problema conhecido como Lost Update (Atualização Perdida).

Diversas operações foram sobrescritas durante a execução concorrente, causando diferença significativa entre o saldo esperado e o saldo real armazenado no banco de dados.

---

# Parte 2 — Controle de Versão Otimista

(Espaço reservado para implementação do Aluno B)

Será utilizado `@Version` para impedir inconsistências causadas por acessos simultâneos ao mesmo registro.

---

# Arquivos do projeto

* `teste-concorrencia.jmx` → cenário de testes do Apache JMeter
* `README.md` → documentação do projeto
