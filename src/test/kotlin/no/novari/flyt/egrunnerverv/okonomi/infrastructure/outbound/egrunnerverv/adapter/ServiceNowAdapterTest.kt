package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.adapter

import io.mockk.every
import io.mockk.mockk
import no.novari.flyt.egrunnerverv.okonomi.domain.error.GenericSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.ServiceNowSyncException
import no.novari.flyt.egrunnerverv.okonomi.domain.model.ExternalSupplierId
import no.novari.flyt.egrunnerverv.okonomi.domain.model.ServiceNowSysId
import no.novari.flyt.egrunnerverv.okonomi.domain.model.Supplier
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierSyncContext
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierSyncOutcome
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierSyncResult
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.error.ServiceNowPatchConsumerException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.service.ServiceNowClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ServiceNowAdapterTest {
    private val client = mockk<ServiceNowClient>()
    private val adapter = ServiceNowAdapter(client)
    private val identity = SupplierIdentity.OrgId("999999999")
    private val tenantId = TenantId("novari-no")

    @Test
    fun `translates patch exception to ServiceNowSyncException`() {
        val outcome =
            SupplierSyncOutcome(
                result = SupplierSyncResult.Created,
                supplier = supplier(ExternalSupplierId("ekstern-1234")),
            )
        val context = SupplierSyncContext(ServiceNowSysId("sys-1"))

        every { client.syncSupplier(outcome, context, tenantId) } throws
            ServiceNowPatchConsumerException(context.serviceNowSysId)

        val ex =
            assertFailsWith<ServiceNowSyncException> {
                adapter.syncSupplier(
                    supplierSyncOutcome = outcome,
                    syncContext = context,
                    supplierIdentity = identity,
                    tenantId = tenantId,
                )
            }

        assertEquals("Klarte ikke å oppdatere ServiceNow med ekstern ID", ex.message)
    }

    @Test
    fun `wraps unexpected exception as generic supplier exception`() {
        val outcome =
            SupplierSyncOutcome(
                result = SupplierSyncResult.Created,
                supplier = supplier(ExternalSupplierId("ekstern-1234")),
            )
        val context = SupplierSyncContext(ServiceNowSysId("sys-1"))

        every { client.syncSupplier(outcome, context, tenantId) } throws RuntimeException("boom")

        val ex =
            assertFailsWith<GenericSupplierException> {
                adapter.syncSupplier(
                    supplierSyncOutcome = outcome,
                    syncContext = context,
                    supplierIdentity = identity,
                    tenantId = tenantId,
                )
            }

        assertEquals("Ukjent feil i kommunikasjonen med ServiceNow-klienten", ex.message)
    }

    private fun supplier(externalId: ExternalSupplierId?): Supplier {
        return Supplier(
            externalId = externalId,
            name = "Leverandør AS",
            kontoNummer = "1234.56.78901",
            street = "Gate 1",
            zip = "0010",
            city = "Oslo",
            email = "post@test.no",
        )
    }
}
