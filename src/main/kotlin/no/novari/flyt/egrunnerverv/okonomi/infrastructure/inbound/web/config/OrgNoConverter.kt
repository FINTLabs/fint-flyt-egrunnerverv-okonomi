package no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.config

import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.config.AdapterSupplierProperties
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.error.InvalidOrganizationNumberException
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class OrgNoConverter(
    private val props: AdapterSupplierProperties,
) : Converter<Long, TenantId> {
    override fun convert(source: Long): TenantId {
        val tenant = props.byOrgNo[source] ?: throw InvalidOrganizationNumberException(source)
        return TenantId(tenant)
    }
}
