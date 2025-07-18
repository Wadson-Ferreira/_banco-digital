# Banco Digital API

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.x-brightgreen)
![Maven](https://img.shields.io/badge/Maven-4.0.0-red)
![Status](https://img.shields.io/badge/status-conclu%C3%ADdo-success)

---

### Proposta do Projeto

API de um banco digital fictício feita para demonstrar aprendizados do bootcamp **EDUC360 da https://codigodebase.com.br/**. O projeto foi desenvolvido com base no desafio final proposto, que foi dividido em duas partes: a primeira utilizando Java puro com execução via console, e a segunda (este repositório) evoluindo o conceito para uma API RESTful robusta utilizando o ecossistema Spring.

### Descrição do Projeto

O projeto consiste em uma API REST para um banco digital fictício, oferecendo um conjunto completo de funcionalidades bancárias. A API gerencia o ciclo de vida de clientes, contas, cartões e seguros, implementando regras de negócio complexas para transações financeiras, limites, taxas e contratação de produtos.

A arquitetura segue os princípios de design de software com separação de camadas (`controller`, `service`, `repository`), utilizando DTOs para comunicação e validação, e garantindo a integridade dos dados através de transações.

### Tecnologias Utilizadas

* **Java 21**: Versão moderna do Java utilizada como base da linguagem.
* **Spring Boot 3.3**: Framework principal para criação da aplicação, gerenciamento de dependências e configuração automática.
* **Spring Data JPA**: Para persistência de dados e abstração da camada de repositório.
* **Hibernate**: Implementação JPA para mapeamento objeto-relacional (ORM).
* **H2 Database**: Banco de dados em memória, utilizado para agilidade no desenvolvimento e testes.
* **Maven**: Ferramenta de gerenciamento de dependências e build do projeto.
* **Lombok**: Para reduzir código boilerplate (getters, setters, construtores).
* **MapStruct**: Para realizar o mapeamento entre Entidades e DTOs de forma performática e automatizada.
* **Spring Validation**: Para validação declarativa dos dados de entrada nos DTOs.
* **JUnit 5 & Mockito**: Para a construção de testes unitários robustos.

---

### Estrutura do Projeto

O projeto é organizado em uma arquitetura de camadas bem definida para garantir a separação de responsabilidades e a manutenibilidade:

* `config`: Classes de configuração do Spring, como a inicialização de dados (`DataSeedingConfig`).
* `controller`: Camada de API REST. Responsável por expor os endpoints, receber requisições HTTP e retornar respostas JSON.
    * `exception`: Contém o `ResourceExceptionHandler` para tratamento global de exceções, padronizando as respostas de erro.
* `dto`: Data Transfer Objects. Usados para definir os "contratos" da API.
    * `request`: DTOs para receber dados, contendo as anotações de validação.
    * `response`: DTOs para enviar dados como resposta.
    * `mapper`: Interfaces do MapStruct que automatizam a conversão entre Entidades e DTOs.
* `entity`: Entidades JPA que representam as tabelas do banco de dados.
    * `enums`: Enumerações utilizadas no modelo de domínio.
* `repository`: Interfaces do Spring Data JPA, responsáveis pela comunicação com o banco de dados.
* `service`: Camada de serviço, onde reside toda a lógica de negócio da aplicação.

---

### Como Rodar o Projeto

**Pré-requisitos:**
* **Java JDK 21** ou superior instalado.
* **Apache Maven** instalado e configurado nas variáveis de ambiente.
* Uma IDE de sua preferência (IntelliJ, VS Code, Eclipse).
* Uma ferramenta para testes de API, como **Postman** ou **Insomnia**.

**Passos para Execução:**

1.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/Wadson-Ferreira/_banco-digital.git](https://github.com/Wadson-Ferreira/_banco-digital.git)
    ```

2.  **Navegue até o diretório do projeto:**
    ```bash
    cd _banco-digital
    ```

3.  **Compile e instale as dependências com o Maven:**
    ```bash
    mvn clean install
    ```

4.  **Execute a aplicação:**
    ```bash
    mvn spring-boot:run
    ```

A API estará rodando em `http://localhost:8080`.

**Acessando o Banco de Dados H2:**
* Após iniciar a aplicação, acesse `http://localhost:8080/h2-console` no seu navegador.
* Use a seguinte URL JDBC para conectar: `jdbc:h2:mem:bancodigital`
* Use o usuário `Admin` e deixe a senha em branco.

---

### Endpoints da API

A seguir estão todos os endpoints disponíveis na API, separados por recurso.

#### Clientes
| Método | Rota | Descrição | Corpo da Requisição (Exemplo) |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/clientes` | Cria um novo cliente. | `ClienteCreateRequestDTO` |
| `GET` | `/api/v1/clientes/{id}` | Busca um cliente pelo seu ID. | N/A |
| `GET` | `/api/v1/clientes/cpf/{cpf}` | Busca um cliente pelo seu CPF. | N/A |
| `PUT` | `/api/v1/clientes/{id}` | Atualiza os dados de um cliente. | `ClienteUpdateRequestDTO` |
| `DELETE` | `/api/v1/clientes/{id}` | Deleta um cliente. | N/A |

#### Contas
| Método | Rota | Descrição | Corpo da Requisição (Exemplo) |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/contas` | Abre uma nova conta para um cliente. | `AbrirContaRequestDTO` |
| `GET` | `/api/v1/contas/{numero}` | Busca os detalhes de uma conta. | N/A |
| `GET` | `/api/v1/contas/cliente/{clienteId}`| Lista todas as contas de um cliente. | N/A |
| `POST` | `/api/v1/contas/{numero}/depositos` | Realiza um depósito na conta. | `TransacaoRequestDTO` |
| `POST` | `/api/v1/contas/{numero}/saques` | Realiza um saque da conta. | `TransacaoRequestDTO` |
| `POST` | `/api/v1/contas/transferencias` | Transfere um valor entre contas. | Parâmetros: `origem`, `destino`, `valor` |
| `DELETE` | `/api/v1/contas/{numero}` | Encerra (deleta) uma conta. | N/A |

#### Cartões
| Método | Rota | Descrição | Corpo da Requisição (Exemplo) |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/cartoes/credito` | Cria um novo cartão de crédito. | `CartaoCreditoCreateRequestDTO` |
| `POST` | `/api/v1/cartoes/debito` | Cria um novo cartão de débito. | `CartaoDebitoCreateRequestDTO` |
| `GET` | `/api/v1/cartoes/conta/{numeroConta}`| Lista todos os cartões de uma conta. | N/A |
| `POST` | `/api/v1/cartoes/{id}/pagamentos` | Realiza um pagamento com um cartão. | `PagamentoRequestDTO` |
| `PATCH`| `/api/v1/cartoes/{id}/limite-credito`| Ajusta o limite de um cartão de crédito. | `LimiteRequestDTO` |
| `PATCH`| `/api/v1/cartoes/{id}/limite-diario-debito`| Ajusta o limite diário de um cartão de débito. | `LimiteRequestDTO` |
| `PATCH`| `/api/v1/cartoes/{id}/estado` | Altera o status (ATIVO/INATIVO) de um cartão. | `AlterarEstadoCartaoRequestDTO` |
| `DELETE`| `/api/v1/cartoes/{id}` | Cancela (deleta) um cartão. | N/A |

#### Seguros e Apólices
| Método | Rota | Descrição | Corpo da Requisição (Exemplo) |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/seguros` | Lista os tipos de seguro disponíveis. | N/A |
| `POST` | `/api/v1/apolices` | Contrata um seguro para um cartão de crédito. | `ContratarSeguroRequestDTO` |
| `GET` | `/api/v1/apolices/cliente/{clienteId}`| Lista as apólices ativas de um cliente. | N/A |
| `DELETE` | `/api/v1/apolices/{id}` | Cancela uma apólice de seguro fraude. | N/A |
| `PATCH`| `/api/v1/apolices/{id}/renovar-viagem`| Renova um seguro viagem, adicionando mais dias. | `RenovarSeguroViagemRequestDTO` |

#### Endereços (Serviço de Consulta)
| Método | Rota | Descrição | Corpo da Requisição (Exemplo) |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/enderecos/cep/{cep}` | Consulta um CEP na API externa (BrasilAPI). | N/A |

---

### Testes Unitários nas Classes de Serviço

A camada de serviço foi extensivamente testada utilizando **JUnit 5** e **Mockito** para garantir a correção da lógica de negócio. A abordagem de testes unitários foca em isolar cada classe de serviço, simulando (`mockando`) o comportamento de suas dependências (como Repositórios e outros Serviços) para validar exclusivamente as regras de negócio implementadas.

**Principais Cenários Cobertos pelos Testes:**

* **`ClienteServiceTest`**:
    * Valida a criação de clientes com dados corretos.
    * Garante que a criação falha ao tentar usar um CPF já existente, esperando uma `DataIntegrityException`.
    * Testa a regra de negócio que impede o cadastro de clientes menores de 18 anos.
    * Verifica a busca e atualização de clientes, além do tratamento de erro para IDs não encontrados (`ObjectNotFoundException`).
    * Testa o cenário de exclusão de cliente e a prevenção contra a exclusão de clientes com contas vinculadas.

* **`ContaServiceTest`**:
    * Assegura que a abertura de contas (`Corrente` e `Poupança`) aplica corretamente as taxas e rendimentos baseados na categoria do cliente.
    * Testa as operações de depósito e saque, validando as alterações de saldo.
    * Cobre cenários de falha, como saque com saldo insuficiente e depósito com valor negativo, esperando uma `BusinessException`.
    * Valida a lógica de transferência, garantindo que os métodos de sacar e depositar são chamados corretamente.
    * Testa a exclusão de contas, permitindo apenas se a conta não possuir cartões ativos.

* **`CartaoServiceTest`**:
    * Valida a criação de cartões de crédito e débito, associando-os corretamente a uma conta.
    * Testa o processo de pagamento com crédito, cobrindo cenários de sucesso e falha por limite insuficiente.
    * Testa o pagamento com débito, verificando a interação com o `ContaService` para o saque e a validação do limite diário.
    * Cobre os métodos de ajuste de limite para ambos os tipos de cartão e a alteração de status (ATIVO/INATIVO).

* **`SeguroServiceTest` e `ApoliceServiceTest`**:
    * Garante que a contratação de seguro só é permitida para cartões de crédito.
    * Valida a lógica de cálculo de custo e cobertura para os seguros `FRAUDE` e `VIAGEM` com base nas regras de negócio (percentual do limite, dias de viagem).
    * Testa a cobrança do valor do seguro na fatura do cartão.
    * Verifica a regra que emite um aviso ao contratar seguro viagem se um seguro fraude já estiver ativo.
    * Valida o cancelamento de apólices, incluindo o cálculo de estorno proporcional e a desvinculação do cartão.
    * Testa a renovação de seguro viagem, confirmando a cobrança adicional e a extensão da data de vigência.

* **`EnderecoServiceTest`**:
    * Simula chamadas à API externa `BrasilAPI` usando `RestTemplate`.
    * Testa o caminho de sucesso, onde um CEP válido retorna os dados de endereço esperados.
    * Cobre cenários de falha, como CEP com formato inválido (`BusinessException`) e CEP não encontrado na API externa (`ObjectNotFoundException`).

---

### Contribuições e Agradecimentos

Este projeto foi desenvolvido como parte de um processo de aprendizado e está aberto para quem quiser utilizá-lo para fins de estudo.

Sinta-se à vontade para clonar, fazer um fork, abrir issues ou submeter pull requests. Ajustes, novas funcionalidades, melhorias na documentação e críticas construtivas são sempre bem-vindos!

Um agradecimento especial a todos os colegas do bootcamp **EDUC360** pela colaboração e troca de conhecimento, e à **https://codigodebase.com.br/** pela oportunidade e pelos ensinamentos que tornaram este projeto possível.
