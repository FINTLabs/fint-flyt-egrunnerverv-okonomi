package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter
import org.springframework.web.client.RestClient

interface VismaRestClientFactory {
    fun create(
        baseUrl: String,
        timeouts: VismaProperties.Timeouts,
    ): RestClient
}

class DefaultVismaRestClientFactory(
    private val xmlMapper: ObjectMapper,
    private val builder: RestClient.Builder = RestClient.builder(),
) : VismaRestClientFactory {
    override fun create(
        baseUrl: String,
        timeouts: VismaProperties.Timeouts,
    ): RestClient {
        val settings =
            ClientHttpRequestFactorySettings
                .defaults()
                .withConnectTimeout(timeouts.connect)
                .withReadTimeout(timeouts.read)

        val requestFactory =
            ClientHttpRequestFactoryBuilder
                .detect()
                .build(settings)

        return builder
            .baseUrl(baseUrl)
            .requestFactory(requestFactory)
            .messageConverters { it.add(MappingJackson2XmlHttpMessageConverter(xmlMapper)) }
            .build()
    }
}
