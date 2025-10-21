# 🚀 Plano de Implementação Incremental - MGT-Chat Refactoring

**Data:** 2025-10-21  
**Status:** EM EXECUÇÃO AUTOMÁTICA  
**Objetivo:** Refatorar + Adicionar features mantendo 100% compatibilidade

---

## 📋 ETAPA 1 - ANÁLISE CONCLUÍDA

### Classes a REMOVER (vazias/não usadas):
- ✅ `SpamTracker.java` → lógica inline em ChatEventHandler
- ✅ `PrivateMessageManager.java` → duplicado por TellCommand
- ✅ `FtbRanksIntegration.java` → vazia

### Classes a CRIAR:

#### 📦 Package: `service/`
1. **ChatFormatterService.java** - Formatação centralizada
2. **AntiSpamService.java** - Rate limiting + repeated messages
3. **MessageBroadcaster.java** - Envio otimizado
4. **PermissionService.java** - FTB Ranks + OP fallback
5. **ChatLogger.java** - Logs estruturados

#### 📦 Package: `storage/`
1. **IgnoreListStorage.java** - Thread-safe ignore list
2. **ConversationStorage.java** - Thread-safe tell history
3. **PlayerChannelStorage.java** - Mover de util para storage

#### 📦 Package: `integration/`
1. **FtbRanksIntegration.java** - Implementar prefix/suffix

#### 📦 Package: `commands/admin/`
1. **ReloadCommand.java** - /mgtchat reload
2. **MuteCommand.java** - /mute <player> [time]
3. **UnmuteCommand.java** - /unmute <player>
4. **ClearChatCommand.java** - /clearchat [player]
5. **ChatSpyCommand.java** - /spy [on|off]

#### 📦 Package: `sound/`
1. **ChatSounds.java** - Registro de sons

#### 📦 Package: `util/`
1. **PlayerDistanceCalculator.java** - Otimização espacial
2. **VanishDetector.java** - Detectar players invisíveis

---

## 🔨 ETAPA 2 - REFATORAÇÃO SEGURA (INICIANDO)

### Fase 2.1: Remover Dead Code
- [x] Deletar SpamTracker.java
- [x] Deletar PrivateMessageManager.java
- [x] Mover lógica para services dedicados

### Fase 2.2: Criar Services Base
- [ ] ChatFormatterService
- [ ] AntiSpamService
- [ ] MessageBroadcaster

### Fase 2.3: Criar Storage Layer
- [ ] IgnoreListStorage
- [ ] ConversationStorage
- [ ] PlayerChannelStorage

### Fase 2.4: Refatorar Comandos Existentes
- [ ] LocalCommand - usar services
- [ ] GlobalCommand - usar services
- [ ] TellCommand - usar services
- [ ] IgnoreCommand - usar storage

### Fase 2.5: Refatorar ChatEventHandler
- [ ] Usar AntiSpamService
- [ ] Usar ChatFormatterService
- [ ] Usar MessageBroadcaster
- [ ] Reduzir de 127 linhas para ~60 linhas

---

## ⚒️ ETAPA 3 - NOVAS IMPLEMENTAÇÕES

### Feature 3.1: Comandos Admin
- [ ] PermissionService (FTB Ranks + OP)
- [ ] ReloadCommand
- [ ] MuteCommand com duração
- [ ] UnmuteCommand
- [ ] ClearChatCommand
- [ ] ChatSpyCommand

### Feature 3.2: Integração FTB Ranks
- [ ] Implementar FtbRanksIntegration.getPrefix()
- [ ] Implementar FtbRanksIntegration.getSuffix()
- [ ] Integrar em ChatFormatterService
- [ ] Graceful fallback se FTB Ranks ausente

### Feature 3.3: Placeholders MGT-Core
- [ ] Expandir PlaceholderService no MGT-Core
- [ ] Adicionar {world}, {dimension}, {coords}
- [ ] Adicionar {rank}, {prefix}, {suffix}

### Feature 3.4: Sons
- [ ] Registrar ChatSounds no MGT-Core
- [ ] Implementar play() em TellCommand
- [ ] Config: sounds.enabled, sounds.private

### Feature 3.5: Logs Estruturados
- [ ] Criar ChatLogger
- [ ] Logar [GLOBAL], [LOCAL], [PRIVATE]
- [ ] Config: logging.enabled, logging.include-commands

### Feature 3.6: Bloqueio de Comandos
- [ ] CommandBlocker service
- [ ] Interceptar registro de comandos
- [ ] Config: blocked-commands.list

### Feature 3.7: Aviso Chat Local Vazio
- [ ] VanishDetector (espectadores + vanish)
- [ ] MessageBroadcaster retorna count
- [ ] Enviar aviso configurável se count == 0

---

## 🧪 ETAPA 4 - TESTES

### Testes Unitários
- [ ] AntiSpamServiceTest
- [ ] ChatFormatterServiceTest
- [ ] MessageBroadcasterTest
- [ ] IgnoreListStorageTest

### Testes de Integração
- [ ] Comandos admin com OP/FTB Ranks
- [ ] Sons tocando corretamente
- [ ] Logs aparecendo no console
- [ ] Chat local com/sem destinatários

---

## 📦 ETAPA 5 - FINALIZAÇÃO

- [ ] Gerar CHANGELOG.md
- [ ] Atualizar README.md
- [ ] Criar config example
- [ ] Validar compatibilidade reversa
- [ ] Build final e testes

---

## 📊 PROGRESSO GERAL

- [x] Etapa 1: Análise - 100%
- [ ] Etapa 2: Refatoração - 0%
- [ ] Etapa 3: Features - 0%
- [ ] Etapa 4: Testes - 0%
- [ ] Etapa 5: Finalização - 0%

**Tempo estimado total:** 24-32 horas

---

*Plano gerado automaticamente. Execução em andamento...*

