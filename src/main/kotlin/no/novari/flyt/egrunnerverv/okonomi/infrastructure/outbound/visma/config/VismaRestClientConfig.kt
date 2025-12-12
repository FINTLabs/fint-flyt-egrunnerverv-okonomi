package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter
import org.springframework.retry.annotation.EnableRetry
import org.springframework.security.oauth2.client.ClientCredentialsOAuth2AuthorizedClientProvider
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
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

    @Bean("vismaAuthorizedClientManager")
    fun vismaAuthorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        oAuth2AuthorizedClientRepository: OAuth2AuthorizedClientRepository,
    ): OAuth2AuthorizedClientManager {
        return DefaultOAuth2AuthorizedClientManager(
            clientRegistrationRepository,
            oAuth2AuthorizedClientRepository,
        ).apply {
            setAuthorizedClientProvider(ClientCredentialsOAuth2AuthorizedClientProvider())
        }
    }

    @Bean("vismaRestClient")
    fun vismaRestClient(
        xmlMapper: ObjectMapper,
        props: VismaProperties,
        @Qualifier("vismaAuthorizedClientManager") manager: OAuth2AuthorizedClientManager,
        builder: RestClient.Builder = RestClient.builder(),
    ): RestClient {
        val settings =
            ClientHttpRequestFactorySettings
                .defaults()
                .withConnectTimeout(props.timeouts.connect)
                .withReadTimeout(props.timeouts.read)

        val requestFactory =
            ClientHttpRequestFactoryBuilder
                .detect()
                .build(settings)

        return builder
            .baseUrl(props.baseUrl)
            .requestFactory(requestFactory)
            .messageConverters { it.add(MappingJackson2XmlHttpMessageConverter(xmlMapper)) }
            .requestInterceptor { req, body, exec ->
                val auth =
                    OAuth2AuthorizeRequest
                        .withClientRegistrationId(props.registrationId)
                        .principal("visma-client")
                        .build()
                val client =
                    manager.authorize(auth)
                        ?: error("Autorisasjon mot Visma feilet. Sjekk OAuth2-konfigurasjonen.")
                req.headers.setBearerAuth(client.accessToken.tokenValue)
                req.headers.add("Legacy-Auth", props.legacyAuth)
                exec.execute(req, body)
            }.build()
    }
}
