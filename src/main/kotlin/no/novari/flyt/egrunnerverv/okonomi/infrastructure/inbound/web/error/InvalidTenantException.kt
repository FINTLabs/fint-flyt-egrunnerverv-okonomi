package no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.error

class InvalidTenantException(
    source: String,
) : RuntimeException("Ukjent tenant: $source")
