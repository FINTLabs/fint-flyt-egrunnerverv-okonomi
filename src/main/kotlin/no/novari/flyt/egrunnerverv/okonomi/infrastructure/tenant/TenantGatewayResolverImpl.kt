package no.novari.flyt.egrunnerverv.okonomi.infrastructure.tenant

import no.novari.flyt.egrunnerverv.okonomi.application.tenant.TenantGatewayResolver
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierGatewayPort
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.config.TenantAdapterProperties
import org.springframework.stereotype.Component

@Component
class TenantGatewayResolverImpl(
    private val props: TenantAdapterProperties,
    private val gateways: Map<String, SupplierGatewayPort>,
) : TenantGatewayResolver {
    override fun resolve(tenantId: TenantId): SupplierGatewayPort =
        props.byOrganization[tenantId.id]
            ?.let { id -> gateways[id] ?: error("Ingen gateway bean med navn $id") }
            ?: error("Ingen adapter mapping for ${tenantId.id}")
}
