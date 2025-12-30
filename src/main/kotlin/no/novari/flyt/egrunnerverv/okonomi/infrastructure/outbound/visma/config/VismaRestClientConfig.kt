package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.annotation.EnableRetry
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ClientCredentialsOAuth2AuthorizedClientProvider
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.web.client.RestClient

@Configuration
@EnableRetry
@EnableConfigurationProperties(
    VismaProperties::class,
)
class VismaRestClientConfig {
    @Bean
    fun xmlMapper(): ObjectMapper {
        return XmlMapper().registerKotlinModule()
    }

    @Bean
    fun vismaRestClientFactory(
        xmlMapper: ObjectMapper,
        builder: RestClient.Builder = RestClient.builder(),
    ): VismaRestClientFactory {
        return DefaultVismaRestClientFactory(xmlMapper, builder)
    }

    @Bean("vismaAuthorizedClientManager")
    fun vismaAuthorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientService: OAuth2AuthorizedClientService,
    ): OAuth2AuthorizedClientManager {
        return AuthorizedClientServiceOAuth2AuthorizedClientManager(
            clientRegistrationRepository,
            authorizedClientService,
        ).apply {
            setAuthorizedClientProvider(ClientCredentialsOAuth2AuthorizedClientProvider())
        }
    }
}
