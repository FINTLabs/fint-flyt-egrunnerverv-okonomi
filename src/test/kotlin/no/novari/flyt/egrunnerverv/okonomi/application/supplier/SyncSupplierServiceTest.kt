package no.novari.flyt.egrunnerverv.okonomi.application.supplier

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.novari.flyt.egrunnerverv.okonomi.application.tenant.TenantGatewayResolver
import no.novari.flyt.egrunnerverv.okonomi.domain.model.Supplier
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierGatewayPort
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierSyncResult
import kotlin.test.Test
import kotlin.test.assertEquals

class SyncSupplierServiceTest {
    private val resolver = mockk<TenantGatewayResolver>()
    private val gateway = mockk<SupplierGatewayPort>()
    private val service = SyncSupplierService(resolver)

    private val supplier =
        Supplier(
            name = "Leverandør AS",
            kontoNummer = "1234.56.78901",
            street = "Gate 1",
            zip = "0010",
            city = "Oslo",
            email = "post@test.no",
        )
    private val identity = SupplierIdentity.OrgId("999999999")
    private val tenant = TenantId.NOVARI

    @Test
    fun `delegates to resolved gateway and returns result`() {
        every { resolver.resolve(tenant) } returns gateway
        every { gateway.getOrCreate(supplier, identity, tenant) } returns SupplierSyncResult.Created

        val result = service.getOrCreate(supplier, identity, tenant)

        assertEquals(SupplierSyncResult.Created, result)
        verify(exactly = 1) { resolver.resolve(tenant) }
        verify(exactly = 1) { gateway.getOrCreate(supplier, identity, tenant) }
    }
}
