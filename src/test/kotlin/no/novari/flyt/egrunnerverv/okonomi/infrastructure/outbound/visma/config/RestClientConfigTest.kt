package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.config

import com.sun.net.httpserver.HttpServer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.model.VUXml
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.net.InetSocketAddress
import java.time.Instant
import kotlin.test.assertEquals

class RestClientConfigTest {
    private val config = RestClientConfig()

    @Test
    fun `vismaRestClient sets baseUrl and auth headers`() {
        val props =
            VismaProperties(
                baseUrl = "https://example.com",
                legacyAuth = "legacy-token",
                oauth =
                    VismaProperties.OAuthProps(
                        tokenUrl = "https://token",
                        clientId = "clientId",
                        clientSecret = "clientSecret",
                    ),
                company = VismaProperties.Company(byTenant = mapOf("novari-no" to "visma")),
            )
        val xmlMapper = config.xmlMapper()

        val manager = mockk<OAuth2AuthorizedClientManager>()
        val clientRegistration =
            ClientRegistration
                .withRegistrationId("visma")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .tokenUri("https://token")
                .clientId("clientId")
                .clientSecret("clientSecret")
                .build()
        val authorizedClient =
            OAuth2AuthorizedClient(
                clientRegistration,
                "visma-client",
                OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    "access-token",
                    Instant.now(),
                    Instant.now().plusSeconds(60),
                ),
            )
        every { manager.authorize(any<OAuth2AuthorizeRequest>()) } returns authorizedClient

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

        val restClient: RestClient =
            config.vismaRestClient(
                xmlMapper = xmlMapper,
                props = props.copy(baseUrl = baseUrl),
                manager = manager,
                builder = RestClient.builder(),
            )

        val response =
            restClient
                .get()
                .uri("/erp_ws/oauth/test")
                .retrieve()
                .body<VUXml>()

        val body = requireNotNull(response)
        assertEquals("123", body.customerSuppliers.first().company)
        verify(exactly = 1) { manager.authorize(any<OAuth2AuthorizeRequest>()) }
        server.stop(0)
    }
}
