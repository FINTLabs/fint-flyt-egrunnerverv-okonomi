package no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.error

class InvalidOrganizationNumberException(
    source: Long,
) : RuntimeException("Ukjent organisasjonsnummer '$source'. Kontroller {orgNo} i URL.")
