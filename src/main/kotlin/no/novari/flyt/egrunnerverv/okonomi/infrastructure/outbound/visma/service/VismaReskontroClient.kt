package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.service

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import io.micrometer.core.instrument.Timer
import net.logstash.logback.argument.StructuredArguments.kv
import no.novari.flyt.egrunnerverv.okonomi.domain.logging.toMaskedLogMap
import no.novari.flyt.egrunnerverv.okonomi.domain.model.Supplier
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.metrics.RetryMetricsListener
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.config.VismaProperties
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.config.VismaRestClientFactory
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaCreateSupplierException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaGetSupplierException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaIdentifierTooLongException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaTenantToCompanyException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.mapper.SupplierMapper
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.model.SupplierType
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.model.VUXml
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.model.VUXmlStoreResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.retry.support.RetryTemplate
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.util.concurrent.ConcurrentHashMap

@Service
class VismaReskontroClient(
    private val restClientFactory: VismaRestClientFactory,
    @param:Qualifier("vismaAuthorizedClientManager") private val manager: OAuth2AuthorizedClientManager,
    private val props: VismaProperties,
    private val supplierMapper: SupplierMapper,
    private val meterRegistry: MeterRegistry,
    private val logger: KLogger = KotlinLogging.logger {},
) {
    private val restClients = ConcurrentHashMap<TenantId, RestClient>()
    private val retryTemplates = ConcurrentHashMap<TenantId, RetryTemplate>()

    init {
        Gauge
            .builder("visma.restclient.cache.size") { restClients.size.toDouble() }
            .register(meterRegistry)
        Gauge
            .builder("visma.retrytemplate.cache.size") { retryTemplates.size.toDouble() }
            .register(meterRegistry)
    }

    fun getCustomerSupplierByIdentifier(
        supplierIdentity: SupplierIdentity,
        tenantId: TenantId,
    ): Supplier? {
        val sample = Timer.start(meterRegistry)
        var result = "unknown"
        try {
            return executeWithRetry(tenantId) { tenant ->
                logGetCustomerSupplierByIdentifier(supplierIdentity, tenantId)

                validateIdentifierLength(supplierIdentity, tenantId)

                val restClient = getRestClient(tenantId, tenant)
                val xmlResponse =
                    try {
                        restClient
                            .get()
                            .uri { uriBuilder ->
                                uriBuilder
                                    .path("/erp_ws/oauth/reskontro/${tenant.company}/0")
                                    .queryParam("fnr", supplierIdentity.value)
                                    .build()
                            }.headers { headers ->
                                val client = authorize(tenant)
                                headers.setBearerAuth(client.accessToken.tokenValue)
                                headers.add("Legacy-Auth", tenant.legacyAuth)
                            }.accept(MediaType.TEXT_XML)
                            .retrieve()
                            .body<VUXml>()
                            ?: throw VismaGetSupplierException(tenantId)
                    } catch (e: Exception) {
                        logger.atError {
                            message = "Klarte ikke å hente supplier fra Visma"
                            arguments =
                                arrayOf(
                                    kv("supplierIdentity", supplierIdentity.toMaskedLogMap()),
                                    kv("tenantId", tenantId),
                                    kv("cause", e),
                                )
                        }
                        if (e is VismaGetSupplierException) {
                            throw e
                        }

                        throw VismaGetSupplierException(tenantId)
                    }

                supplierMapper.mapSingleSupplier(xmlResponse)
            }.also {
                result = "success"
            }
        } catch (e: Exception) {
            result = "error"
            throw e
        } finally {
            sample.stop(
                Timer
                    .builder("visma.lookup.latency")
                    .publishPercentileHistogram()
                    .tag("tenant", tenantId.id)
                    .tag("result", result)
                    .register(meterRegistry),
            )
        }
    }

    fun createCustomerSupplier(
        supplier: Supplier,
        supplierIdentity: SupplierIdentity,
        tenantId: TenantId,
    ) {
        val sample = Timer.start(meterRegistry)
        var resultTag = "unknown"
        try {
            executeWithRetry(tenantId) { tenant ->
                validateIdentifierLength(supplierIdentity, tenantId)

                val restClient = getRestClient(tenantId, tenant)
                val supplierType = SupplierType.LEVERANDOR // FIXME: Bestemmes av hvert fylke

                val requestBody =
                    supplierMapper.mapToVismaRequest(
                        supplier = supplier,
                        supplierIdentity = supplierIdentity,
                        company = tenant.company,
                        division = DIVISION,
                        type = supplierType,
                    )

                val response =
                    try {
                        restClient
                            .post()
                            .uri("/erp_ws/oauth/reskontro")
                            .headers { headers ->
                                val client = authorize(tenant)
                                headers.setBearerAuth(client.accessToken.tokenValue)
                                headers.add("Legacy-Auth", tenant.legacyAuth)
                            }.contentType(MediaType.APPLICATION_XML)
                            .accept(MediaType.TEXT_XML)
                            .body(requestBody)
                            .retrieve()
                            .body<VUXmlStoreResponse>()
                            ?: throw VismaCreateSupplierException(tenantId)
                    } catch (e: Exception) {
                        logger.atError {
                            message = "klarte ikke å opprette leverandør i Visma"
                            arguments =
                                arrayOf(
                                    kv("supplier", supplier.toMaskedLogMap()),
                                    kv("supplierIdentity", supplierIdentity.toMaskedLogMap()),
                                    kv("tenant", tenantId.id),
                                    kv("cause", e),
                                )
                        }
                        if (e is VismaCreateSupplierException) {
                            throw e
                        }
                        throw VismaCreateSupplierException(tenantId)
                    }

                val result = response.customerSuppliers

                when {
                    result.stored == "true" -> {
                        resultTag = "stored"
                        logger.atInfo {
                            message = "Leverandør opprettet i Visma"
                            arguments =
                                arrayOf(
                                    kv("leverandør", supplier.toMaskedLogMap()),
                                    kv("supplierIdentity", supplierIdentity.toMaskedLogMap()),
                                    kv("tenant", tenantId.id),
                                    kv("result", result),
                                )
                        }
                    }

                    result.updated == "true" -> {
                        resultTag = "updated"
                        logger.atInfo {
                            message = "Leverandør oppdatert i Visma"
                            arguments =
                                arrayOf(
                                    kv("leverandør", supplier.toMaskedLogMap()),
                                    kv("supplierIdentity", supplierIdentity.toMaskedLogMap()),
                                    kv("tenant", tenantId.id),
                                    kv("result", result),
                                )
                        }
                    }

                    else -> {
                        resultTag = "error"
                        logger.atError {
                            message = "Kunne ikke opprette eller oppdatere leverandør i Visma"
                            arguments =
                                arrayOf(
                                    kv("leverandør", supplier.toMaskedLogMap()),
                                    kv("supplierIdentity", supplierIdentity.toMaskedLogMap()),
                                    kv("tenant", tenantId.id),
                                    kv("result", result),
                                )
                        }

                        throw VismaCreateSupplierException(tenantId)
                    }
                }
            }
        } catch (e: Exception) {
            if (resultTag == "unknown") {
                resultTag = "error"
            }
            throw e
        } finally {
            meterRegistry
                .counter(
                    "visma.create.count",
                    "tenant",
                    tenantId.id,
                    "result",
                    resultTag,
                ).increment()
            sample.stop(
                Timer
                    .builder("visma.create.latency")
                    .publishPercentileHistogram()
                    .tag("tenant", tenantId.id)
                    .tag("result", resultTag)
                    .register(meterRegistry),
            )
        }
    }

    private fun logGetCustomerSupplierByIdentifier(
        supplierIdentity: SupplierIdentity,
        tenantId: TenantId,
    ) {
        logger.atInfo {
            message = "Henter leverandør fra Visma"
            arguments =
                arrayOf(
                    kv("supplierIdentity", supplierIdentity.toMaskedLogMap()),
                    kv("tenant", tenantId.id),
                )
        }
    }

    private fun validateIdentifierLength(
        identity: SupplierIdentity,
        tenantId: TenantId,
    ) {
        if (identity.value.length > MAX_IDENTIFIER_LENGTH) {
            throw VismaIdentifierTooLongException(tenantId)
        }
    }

    private fun authorize(tenant: VismaProperties.Tenant): OAuth2AuthorizedClient {
        val auth =
            OAuth2AuthorizeRequest
                .withClientRegistrationId(tenant.registrationId)
                .principal(tenant.registrationId)
                .build()

        return manager.authorize(auth)
            ?: error("Autorisasjon mot Visma feilet. Sjekk OAuth2-konfigurasjonen.")
    }

    private fun getTenantConfig(tenantId: TenantId): VismaProperties.Tenant {
        return props.tenants[tenantId.id]
            ?: run {
                logger.atWarn {
                    message = "Fant ingen Visma-konfig for tenant"
                    arguments = arrayOf(kv("tenant", tenantId))
                }
                throw VismaTenantToCompanyException(tenantId)
            }
    }

    private fun getRestClient(
        tenantId: TenantId,
        tenant: VismaProperties.Tenant,
    ): RestClient {
        val timeouts = tenant.timeouts ?: props.timeouts
        return restClients.computeIfAbsent(tenantId) {
            restClientFactory.create(tenant.baseUrl, timeouts)
        }
    }

    private fun <T> executeWithRetry(
        tenantId: TenantId,
        action: (VismaProperties.Tenant) -> T,
    ): T {
        val tenant = getTenantConfig(tenantId)
        val retry = tenant.retry ?: props.retry
        val template =
            retryTemplates.computeIfAbsent(tenantId) {
                val created =
                    RetryTemplate
                        .builder()
                        .maxAttempts(retry.maxAttempts)
                        .exponentialBackoff(retry.initialIntervalMs, retry.multiplier, retry.maxIntervalMs)
                        .notRetryOn(
                            listOf(
                                VismaIdentifierTooLongException::class.java,
                                VismaTenantToCompanyException::class.java,
                            ),
                        ).build()

                created.registerListener(
                    RetryMetricsListener(
                        meterRegistry = meterRegistry,
                        tags = Tags.of("system", "visma", "tenant", tenantId.id),
                    ),
                )

                created
            }

        return template.execute<T, Exception> { action(tenant) }
    }

    companion object {
        private const val DIVISION = "0"
        private const val MAX_IDENTIFIER_LENGTH = 11
    }
}
