package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.service

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.novari.flyt.egrunnerverv.okonomi.domain.model.Supplier
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.config.VismaProperties
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaIdentifierTooLongException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaTenantToCompanyException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.mapper.SupplierMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class VismaReskontroClientTest {
    private lateinit var server: MockRestServiceServer
    private lateinit var client: VismaReskontroClient

    private val props =
        VismaProperties(
            registrationId = "visma",
            baseUrl = "http://localhost",
            legacyAuth = "legacy",
            company = VismaProperties.Company(byTenant = mapOf(TenantId("novari-no").id to "123")),
        )

    @BeforeEach
    fun setup() {
        val xmlMapper = XmlMapper().registerKotlinModule()
        val restClientBuilder =
            RestClient
                .builder()
                .baseUrl(props.baseUrl)
                .messageConverters { converters ->
                    converters.add(
                        org.springframework.http.converter.xml
                            .MappingJackson2XmlHttpMessageConverter(xmlMapper),
                    )
                }
        server = MockRestServiceServer.bindTo(restClientBuilder).bufferContent().build()
        val restClient = restClientBuilder.build()

        client =
            VismaReskontroClient(
                restClient = restClient,
                props = props,
                supplierMapper = SupplierMapper(),
            )
    }

    @Test
    fun `getCustomerSupplierByIdentifier returns mapped supplier`() {
        val xmlResponse =
            """
            <VUXML>
              <customerSuppliers company="123" division="0">
                <customerSupplier csType="L">
                  <csName>Test Leverandør</csName>
                  <csAddress>Gate 1</csAddress>
                  <csPostalAddress>
                    <zipCode>0010</zipCode>
                    <city>Oslo</city>
                  </csPostalAddress>
                  <csEmail>post@test.no</csEmail>
                  <bankAccount>1234.56.78901</bankAccount>
                  <orgNo>12345678901</orgNo>
                </customerSupplier>
              </customerSuppliers>
            </VUXML>
            """.trimIndent()

        server
            .expect(requestTo("http://localhost/erp_ws/oauth/reskontro/123/0?fnr=12345678901"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(queryParam("fnr", "12345678901"))
            .andRespond(withSuccess(xmlResponse, MediaType.TEXT_XML))

        val supplier =
            client.getCustomerSupplierByIdentifier(
                SupplierIdentity.Fodselsnummer("12345678901"),
                TenantId("novari-no"),
            )

        assertNotNull(supplier)
        assertEquals("Test Leverandør", supplier.name)
        assertEquals("1234.56.78901", supplier.kontoNummer)
        assertEquals("Gate 1", supplier.street)
        assertEquals("0010", supplier.zip)
        assertEquals("Oslo", supplier.city)
        assertEquals("post@test.no", supplier.email)

        server.verify()
    }

    @Test
    fun `getCustomerSupplierByIdentifier fails when identifier is too long`() {
        val missingProps =
            VismaProperties(
                registrationId = "visma",
                baseUrl = "http://localhost",
                legacyAuth = "legacy",
                company = VismaProperties.Company(byTenant = emptyMap()),
            )
        val restClient = RestClient.builder().baseUrl(missingProps.baseUrl).build()
        val localClient =
            VismaReskontroClient(
                restClient = restClient,
                props = missingProps,
                supplierMapper = SupplierMapper(),
            )

        assertFailsWith<VismaIdentifierTooLongException> {
            localClient.getCustomerSupplierByIdentifier(
                SupplierIdentity.OrgId("123456789012345678"),
                TenantId("novari-no"),
            )
        }
    }

    @Test
    fun `getCustomerSupplierByIdentifier fails when company mapping missing`() {
        val missingProps =
            VismaProperties(
                registrationId = "visma",
                baseUrl = "http://localhost",
                legacyAuth = "legacy",
                company = VismaProperties.Company(byTenant = emptyMap()),
            )
        val restClient = RestClient.builder().baseUrl(missingProps.baseUrl).build()
        val localClient =
            VismaReskontroClient(
                restClient = restClient,
                props = missingProps,
                supplierMapper = SupplierMapper(),
            )

        assertFailsWith<VismaTenantToCompanyException> {
            localClient.getCustomerSupplierByIdentifier(
                SupplierIdentity.Fodselsnummer("12345678901"),
                TenantId("novari-no"),
            )
        }
    }

    @Test
    fun `createCustomerSupplier handles stored response`() {
        val responseXml =
            """
            <VUXML>
              <customerSuppliers company="123">
                <stored>true</stored>
                <updated>false</updated>
                <errors/>
              </customerSuppliers>
            </VUXML>
            """.trimIndent()

        server
            .expect(requestTo("http://localhost/erp_ws/oauth/reskontro"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_XML))
            .andRespond(withSuccess(responseXml, MediaType.TEXT_XML))

        client.createCustomerSupplier(
            supplier =
                Supplier(
                    name = "Leverandør AS",
                    kontoNummer = "1234.56.78901",
                    street = "Gate 1",
                    zip = "0010",
                    city = "Oslo",
                    email = "post@test.no",
                ),
            supplierIdentity = SupplierIdentity.OrgId("999999999"),
            tenantId = TenantId("novari-no"),
        )

        server.verify()
    }

    @Test
    fun `createCustomerSupplier handles updated response`() {
        val responseXml =
            """
            <VUXML>
              <customerSuppliers company="123">
                <stored>false</stored>
                <updated>true</updated>
                <errors/>
              </customerSuppliers>
            </VUXML>
            """.trimIndent()

        server
            .expect(requestTo("http://localhost/erp_ws/oauth/reskontro"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_XML))
            .andRespond(withSuccess(responseXml, MediaType.TEXT_XML))

        client.createCustomerSupplier(
            supplier =
                Supplier(
                    name = "Leverandør AS",
                    kontoNummer = "1234.56.78901",
                    street = "Gate 1",
                    zip = "0010",
                    city = "Oslo",
                    email = "post@test.no",
                ),
            supplierIdentity = SupplierIdentity.OrgId("999999999"),
            tenantId = TenantId("novari-no"),
        )

        server.verify()
    }
}
