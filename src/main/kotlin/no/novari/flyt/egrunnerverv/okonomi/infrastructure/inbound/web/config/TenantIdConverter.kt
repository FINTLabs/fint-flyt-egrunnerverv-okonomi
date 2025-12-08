package no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.config

import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.error.InvalidTenantException
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class TenantIdConverter : Converter<String, TenantId> {
    override fun convert(source: String): TenantId =
        TenantId.entries.firstOrNull { it.id.equals(source, ignoreCase = true) }
            ?: throw InvalidTenantException(source)
}
