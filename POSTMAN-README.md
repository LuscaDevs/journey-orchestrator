# Coleção Postman - Journey Orchestrator API

## 📋 Visão Geral

Esta coleção do Postman foi criada para testar todos os endpoints da API **Journey Orchestrator**, incluindo o novo recurso de **Transition History** (Histórico de Transições).

## 🚀 Configuração Inicial

### 1. Importar a Coleção

1. Abra o Postman
2. Clique em **Import**
3. Selecione o arquivo `postman-collection.json`
4. Confirme a importação

### 2. Configurar Variáveis de Ambiente

A coleção usa as seguintes variáveis:

- **baseUrl**: `http://localhost:8080` (URL base da API)
- **journeyCode**: Código da jornada (preenchido automaticamente)
- **instanceId**: ID da instância da jornada (preenchido automaticamente)

### 3. Pré-requisitos

- A aplicação **Journey Orchestrator** deve estar rodando em `localhost:8080`
- MongoDB deve estar acessível para persistência de dados

## 📚 Estrutura da Coleção

### 🗂️ Journey Definitions

Gerenciamento de definições de jornadas:

1. **Create Journey Definition** - Cria uma nova definição de jornada
2. **List All Journey Definitions** - Lista todas as jornadas disponíveis
3. **Get Journey Definition by Code** - Busca uma jornada específica pelo código

### 🔄 Journey Instances

Gerenciamento de instâncias de jornadas:

1. **Start Journey Instance** - Inicia uma nova instância de jornada
2. **Get Journey Instance by ID** - Busca uma instância específica
3. **Send Event to Journey Instance** - Envia eventos para transicionar estados

### 📊 Transition History

**NOVO RECURSO**: Histórico completo de transições de estados:

1. **Get Complete Transition History** - Recupera todo o histórico
2. **Get History with Date Range Filter** - Filtra por período de tempo
3. **Get History with Event Type Filter** - Filtra por tipo de evento
4. **Get History with Pagination** - Paginação dos resultados
5. **Get History with Combined Filters** - Filtros combinados

### 🎯 Workflow Examples

Exemplos práticos completos:

1. **Complete Onboarding Workflow** - Fluxo completo de onboarding de cliente

## 🔧 Como Usar

### Fluxo Básico de Teste

1. **Criar uma Jornada**
   ```
   POST /journeys
   ```
   Use o exemplo de "Customer Onboarding Journey"

2. **Iniciar uma Instância**
   ```
   POST /journey-instances
   ```
   Use o `journeyCode` retornado

3. **Enviar Eventos**
   ```
   POST /journey-instances/{instanceId}/events
   ```
   Use os eventos: `CREATE_PROFILE`, `UPLOAD_DOCUMENTS`, etc.

4. **Verificar Histórico**
   ```
   GET /journey-instances/{instanceId}/history
   ```

### Testes Automáticos

Cada requisição inclui testes automáticos que verificam:

- ✅ Status codes esperados
- ✅ Estrutura da resposta
- ✅ Presença de campos obrigatórios
- ✅ Tempo de resposta < 2s
- ✅ Headers corretos

### Variáveis Dinâmicas

A coleção gerencia automaticamente:

- `journeyCode`: Salvo após criar uma jornada
- `instanceId`: Salvo após iniciar uma instância

## 📝 Exemplos de Uso

### 1. Criar Jornada de Onboarding

```json
{
  "journeyCode": "customer-onboarding",
  "name": "Customer Onboarding Journey",
  "version": 1,
  "states": [
    {"name": "START", "type": "INITIAL"},
    {"name": "PROFILE_CREATED", "type": "INTERMEDIATE"},
    {"name": "DOCUMENTS_VERIFIED", "type": "INTERMEDIATE"},
    {"name": "ACCOUNT_APPROVED", "type": "INTERMEDIATE"},
    {"name": "COMPLETED", "type": "FINAL"}
  ],
  "transitions": [
    {"from": "START", "event": "CREATE_PROFILE", "target": "PROFILE_CREATED"},
    {"from": "PROFILE_CREATED", "event": "UPLOAD_DOCUMENTS", "target": "DOCUMENTS_VERIFIED"},
    {"from": "DOCUMENTS_VERIFIED", "event": "APPROVE_ACCOUNT", "target": "ACCOUNT_APPROVED"},
    {"from": "ACCOUNT_APPROVED", "event": "COMPLETE_ONBOARDING", "target": "COMPLETED"}
  ]
}
```

### 2. Iniciar Instância

```json
{
  "journeyDefinitionId": "customer-onboarding",
  "journeyVersion": 1,
  "initialData": {
    "customerId": "CUST-12345",
    "email": "customer@example.com",
    "name": "John Doe"
  }
}
```

### 3. Enviar Evento

```json
{
  "event": "CREATE_PROFILE",
  "data": {
    "profileData": {
      "firstName": "John",
      "lastName": "Doe",
      "phone": "+5511999998888",
      "address": "Rua Exemplo, 123 - São Paulo, SP"
    }
  }
}
```

## 🔍 Filtros do Histórico

### Filtro por Período

```
GET /journey-instances/{instanceId}/history?from=2026-03-28T10:00:00Z&to=2026-03-28T12:00:00Z
```

### Filtro por Tipo de Evento

```
GET /journey-instances/{instanceId}/history?eventType=CREATE_PROFILE
```

### Paginação

```
GET /journey-instances/{instanceId}/history?limit=10&offset=0
```

### Filtros Combinados

```
GET /journey-instances/{instanceId}/history?from=2026-03-28T10:00:00Z&to=2026-03-28T12:00:00Z&eventType=CREATE_PROFILE&limit=10&offset=0
```

## 📊 Estrutura da Resposta do Histórico

```json
{
  "instanceId": "uuid-da-instancia",
  "events": [
    {
      "id": "uuid-do-evento",
      "instanceId": "uuid-da-instancia",
      "fromState": "START",
      "toState": "PROFILE_CREATED",
      "event": {
        "type": "CREATE_PROFILE",
        "data": "dados do evento"
      },
      "timestamp": "2026-03-28T11:00:00Z",
      "metadata": {}
    }
  ],
  "totalCount": 5,
  "pagination": {
    "limit": 100,
    "offset": 0,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

## 🎯 Workflow Completo

Execute o folder **"Complete Onboarding Workflow"** para testar:

1. ✅ Criação da jornada
2. ✅ Início da instância
3. ✅ Envio sequencial de eventos
4. ✅ Verificação do histórico completo

## 🐛 Troubleshooting

### Aplicação não inicia

- Verifique se MongoDB está rodando
- Confirme se porta 8080 está livre
- Verifique logs da aplicação

### Erros de Validação

- Confirme estrutura JSON nos requests
- Verifique campos obrigatórios
- Valide tipos de dados

### Histórico Vazio

- Execute eventos na jornada primeiro
- Verifique se `instanceId` está correto
- Confirme se a jornada está ativa

## 📚 Documentação Adicional

- [OpenAPI Specification](api-spec/openapi.yaml)
- [Transition History Feature Spec](specs/004-transition-history/)
- [Architecture Documentation](docs/)

## 🚀 Próximos Passos

1. Importe a coleção no Postman
2. Inicie a aplicação Journey Orchestrator
3. Execute o workflow completo de exemplo
4. Explore os diferentes filtros do histórico
5. Teste seus próprios cenários de jornada

---

**Dica**: Use a aba **"Tests"** no Postman para ver os resultados dos testes automáticos e a aba **"Console"** para debugar requisições.
