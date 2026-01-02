package no.novari.flyt.egrunnerverv.okonomi.application.supplier

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import no.novari.flyt.egrunnerverv.okonomi.application.tenant.TenantGatewayResolver
import no.novari.flyt.egrunnerverv.okonomi.domain.model.ExternalSupplierId
import no.novari.flyt.egrunnerverv.okonomi.domain.model.ServiceNowSysId
import no.novari.flyt.egrunnerverv.okonomi.domain.model.Supplier
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierSyncContext
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierSyncOutcome
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.EgrunnervervPort
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierGatewayPort
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierSyncResult
import kotlin.test.Test
import kotlin.test.assertEquals

class SyncSupplierServiceTest {
    private val resolver = mockk<TenantGatewayResolver>()
    private val egrunnervervPort = mockk<EgrunnervervPort>()
    private val gateway = mockk<SupplierGatewayPort>()
    private val meterRegistry = SimpleMeterRegistry()
    private val service = SyncSupplierService(resolver, egrunnervervPort, meterRegistry)

    private val supplier =
        Supplier(
            externalId = ExternalSupplierId("visma-1234"),
            name = "Leverandør AS",
            kontoNummer = "1234.56.78901",
            street = "Gate 1",
            zip = "0010",
            city = "Oslo",
            email = "post@test.no",
        )
    private val syncContext = SupplierSyncContext(ServiceNowSysId("123-456-789"))
    private val identity = SupplierIdentity.OrgId("999999999")
    private val tenant = TenantId("novari-no")

    @Test
    fun `delegates to resolved gateway and returns result`() {
        every { resolver.resolve(tenant) } returns gateway
        every { gateway.getOrCreate(supplier = supplier, supplierIdentity = identity, tenantId = tenant) } returns
            SupplierSyncOutcome(SupplierSyncResult.Created, supplier)
        justRun {
            egrunnervervPort.syncSupplier(
                SupplierSyncOutcome(SupplierSyncResult.Created, supplier),
                syncContext,
                identity,
                tenant,
            )
        }

        val result =
            service.getOrCreate(
                supplier = supplier,
                syncContext = syncContext,
                supplierIdentity = identity,
                tenantId = tenant,
            )

        assertEquals(SupplierSyncResult.Created, result.result)
        verify(exactly = 1) { resolver.resolve(tenant) }
        verify(exactly = 1) {
            gateway.getOrCreate(
                supplier = supplier,
                supplierIdentity = identity,
                tenantId = tenant,
            )
        }
        verify(exactly = 1) {
            egrunnervervPort.syncSupplier(
                SupplierSyncOutcome(SupplierSyncResult.Created, supplier),
                syncContext,
                identity,
                tenant,
            )
        }
    }
}
