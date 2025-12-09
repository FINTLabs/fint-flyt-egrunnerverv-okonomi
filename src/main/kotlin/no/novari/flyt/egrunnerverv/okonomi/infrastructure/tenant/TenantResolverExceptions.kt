package no.novari.flyt.egrunnerverv.okonomi.infrastructure.tenant

class NoAdapterMappingException(
    tenant: String,
) : RuntimeException("Ingen adapter mapping for $tenant")

class MissingGatewayBeanException(
    beanName: String,
) : RuntimeException("Ingen gateway bean med navn $beanName")
