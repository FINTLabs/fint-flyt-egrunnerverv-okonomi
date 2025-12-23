package no.novari.flyt.egrunnerverv.okonomi.domain.ports.out

import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierSyncContext
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierSyncOutcome
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId

interface EgrunnervervPort {
    fun syncSupplier(
        supplierSyncOutcome: SupplierSyncOutcome,
        syncContext: SupplierSyncContext,
        supplierIdentity: SupplierIdentity,
        tenantId: TenantId,
    )
}
