package no.novari.flyt.egrunnerverv.okonomi.infrastructure.tenant

class NoAdapterMappingException(
    organization: String,
) : RuntimeException("Ingen adapter mapping for $organization")

class MissingGatewayBeanException(
    beanName: String,
) : RuntimeException("Ingen gateway bean med navn $beanName")
