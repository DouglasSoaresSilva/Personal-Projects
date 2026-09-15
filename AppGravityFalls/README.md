# △ Codificador Gravity Falls

App em **Kotlin + Jetpack Compose** que codifica e descodifica as 3 cifras clássicas da série *Gravity Falls* (Alex Hirsch), com estética inspirada no site [thisisnotawebsitedotcom.com](https://thisisnotawebsitedotcom.com/).

## Tema claro / escuro automático (sem opção manual)

O app **segue o sistema** via `isSystemInDarkTheme()` — não há botão de troca:

- **Celular em modo escuro** → terminal CRT preto/verde do Bill (fundo preto, texto verde-fósforo, scanlines).
- **Celular em modo claro** → Diário do Dipper (fundo pergaminho `#F3EAD3`, texto marrom, destaques em vermelho-journal e dourado-queimado).

Para testar: mude o tema do celular/emulador em Configurações → Tema, o app muda sozinho.

## Cifras implementadas

| Cifra | Como funciona | Exemplo |
|---|---|---|
| **César** | Cada letra deslocada 3 posições. Codificar = +3 (A→D), Descodificar = −3 (D→A) | `WELCOME TO GRAVITY FALLS` ⇄ `ZHOFRPH WR JUDYLWB IDOOV` |
| **Atbash** | Alfabeto invertido: A↔Z, B↔Y, C↔X… (simétrica) | `WELCOME` ⇄ `DVOXLNV` |
| **A1Z26** | Letra → número: A=1 … Z=26. `/` = espaço | `WELCOME` ⇄ `23 5 12 3 15 13 5` |

## Estrutura

```
AppGravityFalls/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/gravityfalls/codificador/
        │   ├── MainActivity.kt          # Activity + setContent
        │   ├── ciphers/Ciphers.kt       # lógica pura das 3 cifras
        │   ├── history/HistoryStore.kt  # histórico (SharedPreferences + JSON)
        │   └── ui/
        │       ├── CipherScreen.kt      # UI + histórico
        │       └── theme/Theme.kt       # paletas dark CRT + light Diário
        └── res/values/strings.xml, themes.xml
```

## Como abrir e rodar

1. Abra o **Android Studio** → *Open* → selecione a pasta `AppGravityFalls`.
2. Aguarde o sync do Gradle (usa AGP 8.5.2, Kotlin 2.0.21, Compose BOM 2024.10.00).
3. Rode em um emulador ou dispositivo físico (minSdk 26).

Ou via linha de comando (com o SDK já instalado):

```powershell
cd C:\Users\Admin\Documents\AppGravityFalls
.\gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## Funções do app

- Alternância **CODIFICAR / DESCODIFICAR** (estilo botão vermelho do site).
- Seleção de cifra em cards `[●]` tipo terminal.
- Conversão **automática** ao digitar + botão manual.
- Botões **EXEMPLO** (carrega `WELCOME TO GRAVITY FALLS` e equivalentes), **LIMPAR**, **TROCAR ⇄** (joga o resultado para a entrada e inverte o modo) e **COPIAR RESULTADO**.
- **Histórico de conversões** (persistido mesmo fechando o app):
  - grava a cada toque em CODIFICAR/DESCODIFICAR (máx. 50, mais recentes primeiro);
  - toque num item para **reutilizar** (restaura cifra, modo e entrada);
  - lixeira em cada card para **apagar um** item;
  - botão **LIMPAR TUDO** com diálogo de confirmação.
- A1Z26 tolerante: aceita espaços, vírgulas e hífens na descodificação; `/` = espaço.
- Preserva maiúsculas/minúsculas em César e Atbash; ignora pontuação sem quebrar.

## Testes rápidos

- César descodificar: `ZHOFRPH WR JUDYLWB IDOOV` → `WELCOME TO GRAVITY FALLS`
- Atbash codificar: `WELCOME` → `DVOXLNV`
- A1Z26 codificar: `WELCOME` → `23 5 12 3 15 13 5`
- A1Z26 descodificar: `23 5 12 3 15 13 5 / 20 15 / 7 18 1 22 9 20 25 / 6 1 12 12 19` → `WELCOME TO GRAVITY FALLS`

> ⚠ NÃO CONFIE NO TRIÂNGULO. A realidade é uma ilusão, o universo é um holograma, compre ouro, adeus!
