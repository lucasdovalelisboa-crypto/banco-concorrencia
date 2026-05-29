# Banco Concorrência - Spring Boot

## Integrantes da dupla

* Lucas do Vale Lisboa → Parte 1 (Cenário sem controle de concorrência)
* Leonei Maciel Cardoso Júnior → Parte 2 (Controle de concorrência com @Version)

---

# Objetivo

Demonstrar problemas de concorrência em sistemas transacionais utilizando Spring Boot, JPA/Hibernate e banco H2, além de aplicar controle de versão otimista para evitar inconsistências.

---

# Tecnologias utilizadas

* Java 21
* Spring Boot 3
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

## 2. Entrar na pasta do projeto

```bash
cd banco-concorrencia
```

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

```text
JDBC URL: jdbc:h2:mem:bancodb
User Name: sa
Password:
```

---

# Endpoints da aplicação

## Buscar conta

```http
GET /contas/{id}
```

## Depositar

```http
POST /contas/{id}/deposito
```

Body:

```json
{
  "valor": 100
}
```

## Sacar

```http
POST /contas/{id}/saque
```

Body:

```json
{
  "valor": 50
}
```

---

# Parte 1 - Sem Controle de Concorrência

Nesta etapa foi implementada a entidade `ContaBancaria` sem mecanismos de controle de concorrência, utilizando apenas `@Transactional`.

Foi possível reproduzir o problema de:

* Lost Update (Atualização Perdida)
* Inconsistência de saldo
* Sobrescrita de operações simultâneas

Os testes foram realizados utilizando Apache JMeter com múltiplas threads simultâneas.

---

# Parte 2 - Controle de Versão Otimista

(Espaço para o Aluno B complementar)

Implementação utilizando `@Version` para impedir inconsistências causadas por acessos simultâneos.

---

# Relatório de Conclusão

(Espaço para inserir prints e análises dos testes JMeter)

Comparação entre:

* Cenário sem controle de concorrência
* Cenário com controle de versão otimista
