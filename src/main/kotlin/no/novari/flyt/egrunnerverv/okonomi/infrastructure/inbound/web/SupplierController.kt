package no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web

import jakarta.validation.Valid
import no.novari.flyt.egrunnerverv.okonomi.application.supplier.SyncSupplierUseCase
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierSyncResult
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.dto.SupplierRequest
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.mapper.SupplierRequestMapper
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/egrunnerverv/okonomi/supplier")
class SupplierController(
    private val syncSupplierUseCase: SyncSupplierUseCase,
) {
    @PostMapping
    fun getOrCreateSupplier(
        @Valid @RequestBody supplier: SupplierRequest,
        @RequestHeader("X-Tenant") tenantId: TenantId, // TODO: Get tenant from a proper place
    ): ResponseEntity<Void> {
        val syncResult =
            syncSupplierUseCase.getOrCreate(
                supplier = SupplierRequestMapper.toDomainSupplier(supplier),
                supplierIdentity = SupplierIdentity.from(supplier.fodselsNummer, supplier.orgId),
                tenantId = tenantId,
            )

        return when (syncResult) {
            SupplierSyncResult.Created -> ResponseEntity.ok().build()
            SupplierSyncResult.Updated -> ResponseEntity.ok().build()
        }
    }
}
