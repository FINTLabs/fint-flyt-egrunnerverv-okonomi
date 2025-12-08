package no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.mapper

import no.novari.flyt.egrunnerverv.okonomi.domain.model.Supplier
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.dto.SupplierRequest

object SupplierRequestMapper {
    fun toDomainSupplier(payload: SupplierRequest): Supplier {
        return Supplier(
            name = payload.name,
            kontoNummer = payload.kontoNummer,
            street = payload.street,
            zip = payload.zip,
            city = payload.city,
            email = payload.email,
        )
    }
}
