# fint-flyt-egrunnerverv-okonomi
Økonomi-integrasjon for eGrunnerverv som enten henter eller oppretter leverandør i økonomisystem (per nå Visma) basert på data fra eGrunnerverv.

## Hva appen gjør
- Tilbyr ett HTTP-endepunkt `POST /api/v1/egrunnerverv/okonomi/supplier` som mottar leverandørdata og en `X-Tenant`-header (se [controller](src/main/kotlin/no/novari/flyt/egrunnerverv/okonomi/infrastructure/inbound/web/SupplierController.kt)).
- Validerer input (fødselsnummer/organisasjonsnummer-format, at kun ett av dem er oppgitt, felt påkrevd).
- Oversetter `X-Tenant` til en adapter/gateway via konfig, og ruter videre til riktig leverandøradapter.
- For Visma: forsøker først å hente leverandør med identifikator; hvis ikke funnet opprettes leverandør. Begge tilfeller gir `200 OK` uten body.
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
Se [`src/main/resources/application.yaml`](src/main/resources/application.yaml)  for eksempelverdier.
- `adapter.leverandor.by-tenant`: map fra tenant-id (headerverdi) til adapter-navn (f.eks. `visma`).
- `adapter.adapters.visma.*`: base-url, legacy-auth-header, OAuth2-klient, selskapsmapping `company.by-tenant`, retry-oppsett.
- `spring.security.oauth2.client.registration.visma`: client credentials for Visma.

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
