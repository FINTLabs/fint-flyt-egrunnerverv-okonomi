package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.service

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.novari.flyt.egrunnerverv.okonomi.domain.model.ExternalSupplierId
import no.novari.flyt.egrunnerverv.okonomi.domain.model.ServiceNowSysId
import no.novari.flyt.egrunnerverv.okonomi.domain.model.Supplier
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierSyncContext
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierSyncOutcome
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierSyncResult
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.error.MissingExternalIdException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.retry.support.RetryTemplate
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import kotlin.test.assertFailsWith

class ServiceNowClientTest {
    private lateinit var server: MockRestServiceServer
    private lateinit var client: ServiceNowClient
    private val tenantId = TenantId("novari-no")

    @BeforeEach
    fun setup() {
        val restClientBuilder = RestClient.builder().baseUrl("http://localhost")
        server = MockRestServiceServer.bindTo(restClientBuilder).bufferContent().build()
        client =
            ServiceNowClient(
                restClientBuilder.build(),
                RetryTemplate
                    .builder()
                    .maxAttempts(1)
                    .fixedBackoff(1)
                    .build(),
                SimpleMeterRegistry(),
            )
    }

    @Test
    fun `syncSupplier sends patch request and parses response`() {
        val outcome =
            SupplierSyncOutcome(
                result = SupplierSyncResult.Created,
                supplier = supplier(ExternalSupplierId("ekstern-1234")),
            )
        val context = SupplierSyncContext(ServiceNowSysId("sys-1"))

        server
            .expect(requestTo("http://localhost/csm_consumer/sys-1?sysparam_fields=sys_id,u_leverandor_id"))
            .andExpect(method(HttpMethod.PATCH))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(
                content().json(
                    """
                    {
                      "u_leverandor_id": "ekstern-1234"
                    }
                    """.trimIndent(),
                ),
            ).andRespond(
                withSuccess(
                    """
                    {
                        "sys_id": "sys-1",
                        "u_leverandor_id": "ekstern-1234"
                    }
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON,
                ),
            )

        client.syncSupplier(outcome, context, tenantId)

        server.verify()
    }

    @Test
    fun `syncSupplier throws missing external id exception when external id is missing`() {
        val outcome = SupplierSyncOutcome(SupplierSyncResult.Created, supplier(null))
        val context = SupplierSyncContext(ServiceNowSysId("sys-1"))

        assertFailsWith<MissingExternalIdException> {
            client.syncSupplier(outcome, context, tenantId)
        }
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
