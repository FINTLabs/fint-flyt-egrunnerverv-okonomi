package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.config

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
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
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

        val restClientBuilder = RestClient.builder()
        val server = MockRestServiceServer.bindTo(restClientBuilder).build()
        val restClient: RestClient = config.vismaRestClient(xmlMapper, props, manager, restClientBuilder)

        server
            .expect(requestTo("https://example.com/erp_ws/oauth/test"))
            .andExpect(method(org.springframework.http.HttpMethod.GET))
            .andExpect(header("Authorization", "Bearer access-token"))
            .andExpect(header("Legacy-Auth", "legacy-token"))
            .andRespond(
                withSuccess(
                    """
                    <VUXML>
                        <customerSuppliers company="123" division="0"></customerSuppliers>
                    </VUXML>
                    """.trimIndent(),
                    MediaType.TEXT_XML,
                ),
            )

        val response =
            restClient
                .get()
                .uri("/erp_ws/oauth/test")
                .retrieve()
                .body(VUXml::class.java)

        val body = requireNotNull(response)
        assertEquals("123", body.customerSuppliers.first().company)
        server.verify()
        verify(exactly = 1) { manager.authorize(any<OAuth2AuthorizeRequest>()) }
    }
}
