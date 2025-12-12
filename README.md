# fint-flyt-egrunnerverv-okonomi
Økonomi-integrasjon for eGrunnerverv som enten henter eller oppretter leverandør i økonomisystem (per nå Visma) basert på data fra eGrunnerverv.

## Hva appen gjør
- Tilbyr ett HTTP-endepunkt `POST /api/v1/egrunnerverv/okonomi/supplier` som mottar leverandørdata og en `X-Tenant`-header (se [controller](src/main/kotlin/no/novari/flyt/egrunnerverv/okonomi/infrastructure/inbound/web/SupplierController.kt)).
- Validerer input (fødselsnummer/organisasjonsnummer-format, at kun ett av dem er oppgitt, felt påkrevd).
- Oversetter `X-Tenant` til en adapter/gateway via konfig, og ruter videre til riktig leverandøradapter.
- For Visma: henter leverandør med identifikator; tomt svar (200 OK med tom `<customerSuppliers>`) tolkes som “ikke funnet” og trigget opprettelse. Andre feil gir 500 med leverandørfeil.
- Feilhåndtering gjøres med standardiserte `errorCode`/`errorMessage`-felter. Se feiltabell under.

## Feilkoder (HTTP + `errorCode`)
- 400 `1002` – Mangler fødselsnummer eller organisasjonsnummer.
- 400 `1003` – Begge identifikatorer er satt (kun én tillatt).
- 400 `1004` – Generell valideringsfeil (feltvalidering).
- 400 `1005` – Ugyldig tenant-verdi i `X-Tenant`.
- 500 `1006`/`1007` – Mangler adapter-mapping eller gateway-bean for tenant.
- 500 `2000`–`2003` – Feil fra leverandør/Visma (hente/opprette/manglende selskapsmapping).
- 500 `9999` – Uventet feil.

## Konfigurasjon (viktigste)
- Se [`src/main/resources/application.yaml`](src/main/resources/application.yaml) for ikke-sensitive standardverdier (tenant → adapter/company, retry).
- Hemmelige verdier settes via miljøvariabler/secrets (OnePassword i Kubernetes). Verdiene beholder eksakt navn/casing fra 1Password (ingen auto-uppercasing):
  - `spring.security.oauth2.client.registration.*` og `spring.security.oauth2.client.provider.*` for Visma og ServiceNow hentes fra secrets.
  - `adapter.adapters.visma.*` (registration-id, base-url, legacy-auth) og `adapter.adapters.servicenow.*` hentes fra secrets/env-vars.
- Lokalt: legg inn nødvendige verdier i [`src/main/resources/application-local-staging.yaml`](src/main/resources/application-local-staging.yaml) eller sett miljøvariabler tilsvarende.

## Kjøre lokalt
- Bygg og tester: `./gradlew clean test`
- Starte app (lokalt): `./gradlew bootRun`
- Endepunkt (eksempel):
  ```bash
  curl -X POST http://localhost:8080/api/v1/egrunnerverv/okonomi/supplier \
    -H "Content-Type: application/json" \
    -H "X-Tenant: novari-no" \
    -d '{"u_fdselsnummer":"12345678901","name":"Test Leverandør","u_kontonummer":"1234.56.78901","street":"Gate 1","zip":"0001","city":"Oslo","email":"test@example.org"}'
  ```

## Logging og maskering
- Felt merket med `@LogMasked` maskeres i logger (f.eks. fødselsnummer). `toMaskedLogMap()` brukes for å logge strukturerte, maskerte verdier.

## Arkitektur
Se [`ARCHITECTURE.md`](ARCHITECTURE.md) for en mer detaljert gjennomgang (lagdeling, flyt, og diagrammer).
