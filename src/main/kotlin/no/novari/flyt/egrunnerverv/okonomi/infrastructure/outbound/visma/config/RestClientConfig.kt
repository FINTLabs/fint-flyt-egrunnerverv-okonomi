package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter
import org.springframework.security.oauth2.client.ClientCredentialsOAuth2AuthorizedClientProvider
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {
    @Bean
    fun xmlMapper(): ObjectMapper {
        return XmlMapper().registerKotlinModule()
    }

    @Bean
    fun authorizedClientManager(
        repo: ClientRegistrationRepository,
        repository: OAuth2AuthorizedClientRepository,
    ): OAuth2AuthorizedClientManager {
        return DefaultOAuth2AuthorizedClientManager(repo, repository).apply {
            setAuthorizedClientProvider(ClientCredentialsOAuth2AuthorizedClientProvider())
        }
    }

    @Bean("vismaRestClient")
    fun vismaRestClient(
        xmlMapper: ObjectMapper,
        props: VismaProperties,
        manager: OAuth2AuthorizedClientManager,
        builder: RestClient.Builder = RestClient.builder(),
    ): RestClient {
        // TODO: Configure timeouts/retries
        return builder
            .baseUrl(props.baseUrl)
            .messageConverters { it.add(MappingJackson2XmlHttpMessageConverter(xmlMapper)) }
            .requestInterceptor { req, body, exec ->
                val auth =
                    OAuth2AuthorizeRequest
                        .withClientRegistrationId("visma")
                        .principal("visma-client")
                        .build()
                val client =
                    manager.authorize(auth)
                        ?: error("Klarte ikke å autorisere med Visma")
                req.headers.setBearerAuth(client.accessToken.tokenValue)
                req.headers.add("Legacy-Auth", props.legacyAuth)
                exec.execute(req, body)
            }.build()
    }
}
