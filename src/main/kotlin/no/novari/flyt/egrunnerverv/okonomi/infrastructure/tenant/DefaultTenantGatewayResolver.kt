package no.novari.flyt.egrunnerverv.okonomi.infrastructure.tenant

import no.novari.flyt.egrunnerverv.okonomi.application.tenant.TenantGatewayResolver
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierGatewayPort
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.config.AdapterSupplierProperties
import org.springframework.stereotype.Component

@Component
class DefaultTenantGatewayResolver(
    private val props: AdapterSupplierProperties,
    private val gateways: Map<String, SupplierGatewayPort>,
) : TenantGatewayResolver {
    override fun resolve(tenantId: TenantId): SupplierGatewayPort {
        val beanName =
            props.byTenant[tenantId.id]
                ?: throw NoAdapterMappingException(tenantId.id)

        return gateways[beanName]
            ?: throw MissingGatewayBeanException(beanName)
    }
}
