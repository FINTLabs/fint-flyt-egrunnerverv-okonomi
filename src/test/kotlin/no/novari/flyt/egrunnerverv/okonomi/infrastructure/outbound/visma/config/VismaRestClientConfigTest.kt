package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.config

import com.sun.net.httpserver.HttpServer
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.model.VUXml
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.net.InetSocketAddress
import kotlin.test.assertEquals

class VismaRestClientConfigTest {
    private val config = VismaRestClientConfig()

    @Test
    fun `vismaRestClient sets baseUrl`() {
        val xmlMapper = config.xmlMapper()

        val responseXml =
            """
            <VUXML>
                <customerSuppliers company="123" division="0"></customerSuppliers>
            </VUXML>
            """.trimIndent()
        val server =
            HttpServer.create(InetSocketAddress(0), 0).apply {
                createContext("/erp_ws/oauth/test") { exchange ->
                    val bytes = responseXml.toByteArray()
                    exchange.responseHeaders.add("Content-Type", MediaType.TEXT_XML_VALUE)
                    exchange.sendResponseHeaders(200, bytes.size.toLong())
                    exchange.responseBody.use { it.write(bytes) }
                }
                start()
            }
        val baseUrl = "http://localhost:${server.address.port}"

        val factory = config.vismaRestClientFactory(xmlMapper, RestClient.builder())
        val restClient: RestClient = factory.create(baseUrl, VismaProperties.Timeouts())

        val response =
            restClient
                .get()
                .uri("/erp_ws/oauth/test")
                .retrieve()
                .body<VUXml>()

        val body = requireNotNull(response)
        assertEquals("123", body.customerSuppliers.first().company)
        server.stop(0)
    }
}
