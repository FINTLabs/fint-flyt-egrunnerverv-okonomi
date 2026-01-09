package no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web

import io.micrometer.core.instrument.MeterRegistry
import jakarta.validation.Valid
import no.novari.flyt.egrunnerverv.okonomi.application.supplier.SyncSupplierUseCase
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierSyncResult
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.config.OrgNoConverter
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.dto.SupplierRequest
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.mapper.SupplierRequestMapper
import no.novari.flyt.webresourceserver.UrlPaths.EXTERNAL_API
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(EXTERNAL_API + "/v1/egrunnerverv/okonomi/org/{orgNo}/supplier")
class SupplierController(
    private val syncSupplierUseCase: SyncSupplierUseCase,
    private val orgNoConverter: OrgNoConverter,
    private val meterRegistry: MeterRegistry,
) {
    @PostMapping
    fun getOrCreateSupplier(
        @Valid @RequestBody supplierRequest: SupplierRequest,
        @PathVariable orgNo: Long,
    ): ResponseEntity<Void> {
        val tenantId = orgNoConverter.convert(orgNo)
        val syncContext = SupplierRequestMapper.toSyncContext(supplierRequest)
        val identityType =
            when {
                supplierRequest.fodselsNummer != null -> "fnr"
                supplierRequest.orgId != null -> "org"
                else -> "unknown"
            }
        val syncOutcome =
            syncSupplierUseCase.getOrCreate(
                supplier = SupplierRequestMapper.toDomainSupplier(supplierRequest),
                syncContext = syncContext,
                supplierIdentity = SupplierIdentity.from(supplierRequest.fodselsNummer, supplierRequest.orgId),
                tenantId = tenantId,
            )

        val outcomeTag =
            when (syncOutcome.result) {
                SupplierSyncResult.Created -> "created"
                SupplierSyncResult.Updated -> "updated"
            }

        meterRegistry
            .counter(
                "supplier.sync.count",
                "tenant",
                tenantId.id,
                "identity_type",
                identityType,
                "outcome",
                outcomeTag,
            ).increment()

        return when (syncOutcome.result) {
            SupplierSyncResult.Created -> ResponseEntity.ok().build()
            SupplierSyncResult.Updated -> ResponseEntity.ok().build()
        }
    }
}
