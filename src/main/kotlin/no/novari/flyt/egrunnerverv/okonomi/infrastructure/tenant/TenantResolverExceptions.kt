package no.novari.flyt.egrunnerverv.okonomi.infrastructure.tenant

class NoAdapterMappingException(
    tenant: String,
) : RuntimeException("Fant ingen adapter-konfigurasjon for tenant '$tenant'")

class MissingGatewayBeanException(
    beanName: String,
) : RuntimeException("Fant ingen gateway med navnet '$beanName'. Sjekk konfigurasjonen.")
