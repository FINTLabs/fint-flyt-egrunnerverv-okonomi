package no.novari.flyt.egrunnerverv.okonomi.domain.ports.out

import no.novari.flyt.egrunnerverv.okonomi.domain.model.Supplier
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierSyncOutcome
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId

interface SupplierGatewayPort {
    fun getOrCreate(
        supplier: Supplier,
        supplierIdentity: SupplierIdentity,
        tenantId: TenantId,
    ): SupplierSyncOutcome
}
