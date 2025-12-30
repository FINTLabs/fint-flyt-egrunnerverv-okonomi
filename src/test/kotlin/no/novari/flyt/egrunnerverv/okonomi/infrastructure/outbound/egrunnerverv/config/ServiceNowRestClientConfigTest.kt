package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.config

import com.sun.net.httpserver.HttpServer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.assertEquals

class ServiceNowRestClientConfigTest {
    private val config = ServiceNowRestClientConfig()

    @Test
    fun `serviceNowRestClient sets baseUrl and auth header`() {
        val props =
            ServiceNowProperties(
                baseUrl = "https://example.com",
                registrationId = "servicenow",
                oauth2 =
                    ServiceNowProperties.OAuth2Props(
                        username = "user",
                        password = "password",
                    ),
            )

        val manager = mockk<OAuth2AuthorizedClientManager>()
        val clientRegistration =
            ClientRegistration
                .withRegistrationId("servicenow")
                .authorizationGrantType(AuthorizationGrantType("password"))
                .tokenUri("https://token")
                .clientId("clientId")
                .clientSecret("clientSecret")
                .build()
        val authorizedClient =
            OAuth2AuthorizedClient(
                clientRegistration,
                "service-now-client",
                OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    "access-token",
                    Instant.now(),
                    Instant.now().plusSeconds(60),
                ),
            )
        every { manager.authorize(any<OAuth2AuthorizeRequest>()) } returns authorizedClient

        val capturedAuth = AtomicReference<String?>()
        val server =
            HttpServer.create(InetSocketAddress(0), 0).apply {
                createContext("/api/now/test") { exchange ->
                    capturedAuth.set(exchange.requestHeaders.getFirst("Authorization"))
                    val bytes = """{"result":"ok"}""".toByteArray()
                    exchange.responseHeaders.add("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    exchange.sendResponseHeaders(200, bytes.size.toLong())
                    exchange.responseBody.use { it.write(bytes) }
                }
                start()
            }
        val baseUrl = "http://localhost:${server.address.port}"

        val restClient: RestClient =
            config.serviceNowRestClient(
                props = props.copy(baseUrl = baseUrl),
                manager = manager,
                builder = RestClient.builder(),
            )

        val response =
            restClient
                .get()
                .uri("/api/now/test")
                .retrieve()
                .body<Map<String, String>>()

        assertEquals("ok", response?.get("result"))
        assertEquals("Bearer access-token", capturedAuth.get())
        verify(exactly = 1) { manager.authorize(any<OAuth2AuthorizeRequest>()) }
        server.stop(0)
    }
}
