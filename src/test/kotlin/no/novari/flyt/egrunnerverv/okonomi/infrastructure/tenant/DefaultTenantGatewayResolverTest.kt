package no.novari.flyt.egrunnerverv.okonomi.infrastructure.tenant

import no.novari.flyt.egrunnerverv.okonomi.domain.model.Supplier
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierGatewayPort
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierSyncResult
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.config.AdapterLeverandorProperties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DefaultTenantGatewayResolverTest {
    private class StubGateway : SupplierGatewayPort {
        override fun getOrCreate(
            supplier: Supplier,
            supplierIdentity: SupplierIdentity,
            tenantId: TenantId,
        ): SupplierSyncResult = SupplierSyncResult.Created
    }

    @Test
    fun `resolve returns mapped gateway`() {
        val gateway = StubGateway()
        val resolver =
            DefaultTenantGatewayResolver(
                props = AdapterLeverandorProperties(byTenant = mapOf("novari-no" to "visma")),
                gateways = mapOf("visma" to gateway),
            )

        val resolved = resolver.resolve(TenantId("novari-no"))

        assertEquals(gateway, resolved)
    }

    @Test
    fun `resolve fails when mapping missing`() {
        val resolver =
            DefaultTenantGatewayResolver(
                props = AdapterLeverandorProperties(byTenant = emptyMap()),
                gateways = emptyMap(),
            )

        val ex =
            assertFailsWith<NoAdapterMappingException> {
                resolver.resolve(TenantId("novari-no"))
            }
        assertEquals("Ingen adapter mapping for novari-no", ex.message)
    }

    @Test
    fun `resolve fails when bean name not found`() {
        val resolver =
            DefaultTenantGatewayResolver(
                props = AdapterLeverandorProperties(byTenant = mapOf("novari-no" to "visma")),
                gateways = emptyMap(),
            )

        val ex =
            assertFailsWith<MissingGatewayBeanException> {
                resolver.resolve(TenantId("novari-no"))
            }
        assertEquals("Ingen gateway bean med navn visma", ex.message)
    }
}
