package no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.config

import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.config.TenantAdapterProperties
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.error.InvalidTenantException
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class TenantIdConverter(
    private val tenantAdapterProperties: TenantAdapterProperties,
) : Converter<String, TenantId> {
    override fun convert(source: String): TenantId {
        val trimmed = source.trim()
        if (trimmed.isBlank()) throw InvalidTenantException(source)
        if (!tenantAdapterProperties.byTenant.containsKey(trimmed)) {
            throw InvalidTenantException(source)
        }
        return TenantId(trimmed)
    }
}
