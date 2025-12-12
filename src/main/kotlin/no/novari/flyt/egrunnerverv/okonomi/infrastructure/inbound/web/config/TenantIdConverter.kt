package no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.config

import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.config.AdapterSupplierProperties
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.error.InvalidTenantException
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class TenantIdConverter(
    private val props: AdapterSupplierProperties,
) : Converter<String, TenantId> {
    override fun convert(source: String): TenantId {
        val trimmed = source.trim()
        if (trimmed.isBlank()) throw InvalidTenantException(source)
        if (!props.byTenant.containsKey(trimmed)) {
            throw InvalidTenantException(source)
        }
        return TenantId(trimmed)
    }
}
