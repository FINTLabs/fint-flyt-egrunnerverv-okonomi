package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.config

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.metrics.RetryMetricsListener
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.support.RetryTemplate
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(ServiceNowProperties::class)
class ServiceNowRestClientConfig {
    @Bean("serviceNowRetryTemplate")
    fun serviceNowRetryTemplate(
        props: ServiceNowProperties,
        meterRegistry: MeterRegistry,
    ): RetryTemplate {
        val template =
            RetryTemplate
                .builder()
                .maxAttempts(props.retry.maxAttempts)
                .exponentialBackoff(
                    props.retry.initialIntervalMs,
                    props.retry.multiplier,
                    props.retry.maxIntervalMs,
                ).build()

        template.registerListener(
            RetryMetricsListener(
                meterRegistry = meterRegistry,
                tags = Tags.of("system", "servicenow", "tenant", "unknown"),
            ),
        )

        return template
    }

    @Bean("serviceNowAuthorizedClientManager")
    fun serviceNowAuthorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientService: OAuth2AuthorizedClientService,
        props: ServiceNowProperties,
    ): OAuth2AuthorizedClientManager {
        val provider =
            OAuth2AuthorizedClientProviderBuilder
                .builder()
                .password()
                .refreshToken()
                .build()

        return AuthorizedClientServiceOAuth2AuthorizedClientManager(
            clientRegistrationRepository,
            authorizedClientService,
        ).apply {
            setAuthorizedClientProvider(provider)
            setContextAttributesMapper {
                mapOf(
                    OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME to props.oauth2.username,
                    OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME to props.oauth2.password,
                )
            }
        }
    }

    @Bean("serviceNowRestClient")
    fun serviceNowRestClient(
        props: ServiceNowProperties,
        @Qualifier("serviceNowAuthorizedClientManager") manager: OAuth2AuthorizedClientManager,
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
            .requestInterceptor { req, body, exec ->
                val authorizationRequest =
                    OAuth2AuthorizeRequest
                        .withClientRegistrationId(props.registrationId)
                        .principal("service-now-client")
                        .build()

                val client =
                    manager.authorize(authorizationRequest)
                        ?: error("Autorisasjon mot eGrunnerverv feilet. Sjekk OAuth2-konfigurasjonen.")

                req.headers.setBearerAuth(client.accessToken.tokenValue)
                exec.execute(req, body)
            }.build()
    }
}
