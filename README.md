# TrueLayer Pokedex (Java)

Una semplice REST API tipo *Pokedex* con due endpoint:

- `GET /pokemon/{name}` — info base del Pokémon (descrizione standard).
- `GET /pokemon/translated/{name}` — stessa risposta ma con descrizione *tradotta* (Yoda se leggendario o habitat `cave`, altrimenti Shakespeare). In caso di errore/limite della traduzione, si torna alla descrizione standard.

Requisiti presi dalla challenge (PokeAPI + FunTranslations).

## Perché **non** Spring Boot
Il dominio è piccolo: 2 endpoint, nessun DB. Per restare snelli e veloci ho usato [Javalin](https://javalin.io) (micro web framework) + Jackson. Niente auto-config, tempi di build e immagine Docker ridotti. In produzione si può passare a Spring Boot per DI avanzata, Observability, Actuator, Security, ecc. senza cambiare l’API pubblica.

## Stack
- Java 21
- Javalin 6
- OkHttp 4 (HTTP client)
- Jackson (JSON)
- JUnit 5 + Mockito (test)
- Docker (multi-stage build)

## Come eseguire

### Opzione A — Docker (consigliata)
```bash
# build immagine
docker build -t truelayer-pokedex:latest .

# run su :5000
docker run --rm -p 5000:5000 truelayer-pokedex:latest
```

### Opzione B — Maven locale
Richiede Java 21 e Maven.
```bash
mvn clean package
java -jar target/pokedex-1.0.0.jar
```

L’app ascolta su `http://localhost:5000`.

## Esempi
```bash
# Info base
curl http://localhost:5000/pokemon/mewtwo | jq

# Info tradotta
curl http://localhost:5000/pokemon/translated/mewtwo | jq
```

## Configurazione
Variabili d’ambiente (facoltative):
- `PORT` — porta http (default `5000`)
- `FUN_TRANSLATIONS_API_KEY` — segreto per FunTranslations (solo se ne hai uno; l’app gestisce anche la modalità free / rate-limited)

## Test
```bash
mvn -q test
```

## Note di design
- **Error handling**: 404 se Pokémon non esiste; qualsiasi errore della traduzione non blocca l’endpoint tradotto (fallback alla descrizione standard).
- **Selezione descrizione**: prendo la prima `flavor_text` in inglese e ripulisco i caratteri speciali (`\n`, `\f`).
- **Timeout & retry**: HTTP client con timeout e piccolo retry per richieste transitorie (solo PokéAPI). Rate limit FunTranslations => fallback.
- **Extensibility**: client HTTP incapsulati dietro interfacce -> facile mock nei test e sostituibili (es. uso di WebClient/Spring in futuro).

## Cose che farei in produzione
- Rate limiting locale, circuit breaker (es. Resilience4j), metrics/health (Prometheus + /health), structured logging (JSON), tracing (OpenTelemetry).
- Cache delle risposte PokeAPI per ridurre latenza e dipendenza dal servizio.
- E2E tests con WireMock/MockWebServer + test contract (es. Spring Cloud Contract o Pact).
- CI (GitHub Actions) con build, test, scan, push immagine.
