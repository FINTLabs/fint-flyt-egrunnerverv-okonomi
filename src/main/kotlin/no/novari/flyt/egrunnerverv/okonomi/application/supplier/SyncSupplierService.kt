package no.novari.flyt.egrunnerverv.okonomi.application.supplier

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import no.novari.flyt.egrunnerverv.okonomi.application.tenant.TenantGatewayResolver
import no.novari.flyt.egrunnerverv.okonomi.domain.model.Supplier
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierSyncContext
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierSyncOutcome
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.EgrunnervervPort
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierSyncResult

class SyncSupplierService(
    private val tenantGatewayResolver: TenantGatewayResolver,
    private val egrunnervervPort: EgrunnervervPort,
    private val meterRegistry: MeterRegistry,
) : SyncSupplierUseCase {
    override fun getOrCreate(
        supplier: Supplier,
        syncContext: SupplierSyncContext,
        supplierIdentity: SupplierIdentity,
        tenantId: TenantId,
    ): SupplierSyncOutcome {
        val sample = Timer.start(meterRegistry)
        var outcome = "unknown"
        try {
            val supplierSyncOutcome =
                tenantGatewayResolver
                    .resolve(tenantId)
                    .getOrCreate(
                        supplier = supplier,
                        supplierIdentity = supplierIdentity,
                        tenantId = tenantId,
                    )

            egrunnervervPort.syncSupplier(
                supplierSyncOutcome = supplierSyncOutcome,
                syncContext = syncContext,
                supplierIdentity = supplierIdentity,
                tenantId = tenantId,
            )

            outcome =
                when (supplierSyncOutcome.result) {
                    SupplierSyncResult.Created -> "created"
                    SupplierSyncResult.Updated -> "updated"
                }

            return supplierSyncOutcome
        } catch (e: Exception) {
            outcome = "failed"
            throw e
        } finally {
            sample.stop(
                Timer
                    .builder("supplier.sync.latency")
                    .publishPercentileHistogram()
                    .tag("tenant", tenantId.id)
                    .tag("outcome", outcome)
                    .register(meterRegistry),
            )
        }
    }
}
