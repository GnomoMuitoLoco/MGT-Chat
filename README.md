# 💬 MGT-Chat

**MGT-Chat** é um mod da família **Magnatas Original** que adiciona um sistema de chat avançado para servidores Minecraft.  
Ele depende do **MGT-Core** e expande as funcionalidades de comunicação, trazendo canais, mensagens privadas, filtros e integração com ranks.

---

## 🎯 Objetivo

Fornecer um chat mais organizado, personalizável e seguro para servidores, com suporte a múltiplos canais, mensagens privadas e ferramentas de moderação.

---

## ⚙️ O que já implementa

- **🌍 Canais de chat**
  - `/global` ou `/g` → envia mensagens para todo o servidor.
  - `/local` ou `/l` → envia mensagens apenas para jogadores próximos (configurável em blocos).

- **📩 Mensagens privadas**
  - `/tell <jogador> <mensagem>` → envia mensagem privada.
  - Aliases: `/msg`, `/w`.
  - `/r <mensagem>` → responde ao último jogador que enviou mensagem.
  - Aviso automático se o jogador alvo estiver offline ou se não houver ninguém para responder.

- **🎨 Formatação configurável**
  - Prefixos globais e locais definidos no `mgtchat-config.toml`.
  - Cor padrão para mensagens privadas (`tellColor`).
  - Suporte a cores hexadecimais (`&#RRGGBB`) e códigos `&`/`§`.

- **🛡️ Anti-spam**
  - Delay mínimo entre mensagens.
  - Bloqueio de mensagens repetidas consecutivas.

- **🚫 Filtro de palavras**
  - Lista de palavras proibidas configurável.
  - Substituição automática por `***` ou outro texto definido.

- **👑 Integração com FTB Ranks**
  - Uso de prefixos de ranks no chat.
  - Formato de mensagens com placeholders (`{prefix}`, `{player}`, `{message}`).

---

## 🛠️ O que vai implementar

- **📑 Placeholders avançados (via MGT-Core)**
  - Exemplo: `{sender_player_displayname}`, `{receiver_player_name}`, `{message}`.
  - Formatos configuráveis para `/tell` e `/r`:
    ```toml
    tellFormatTo = "&fSussurrou para {receiver_player_displayname}: {message}"
    tellFormatFrom = "{sender_player_displayname} sussurrou: {message}"
    replyFormatTo = "&fRespondeu para {receiver_player_displayname}: {message}"
    replyFormatFrom = "{sender_player_displayname} respondeu: {message}"
    ```

- **🔔 Sistema de notificações**
  - Sons ou destaques visuais ao receber mensagens privadas.

- **📜 Logs de chat**
  - Registro opcional de mensagens em arquivo para moderação.

---

## 📌 Observação

Este mod **não é distribuído separadamente** e faz parte do ecossistema **Magnatas Original**.  
Ele depende do **MGT-Core** para funcionar corretamente.

---
