package no.novari.flyt.egrunnerverv.okonomi.application.tenant

import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierGatewayPort

fun interface TenantGatewayResolver {
    fun resolve(tenantId: TenantId): SupplierGatewayPort
}
