package no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.error

class InvalidOrganizationNumberException(
    source: Long,
) : RuntimeException("Ukjent orgNo '$source'. Kontroller {orgNo} i path.")
