package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.service

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.novari.flyt.egrunnerverv.okonomi.domain.logging.toMaskedLogMap
import no.novari.flyt.egrunnerverv.okonomi.domain.model.Supplier
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.config.VismaProperties
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaCreateSupplierException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaGetSupplierException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaTenantToCompanyException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.mapper.SupplierMapper
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.model.SupplierType
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.model.VUXml
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.model.VUXmlStoreResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Service
class VismaReskontroClient(
    @param:Qualifier("vismaRestClient") private val restClient: RestClient,
    private val props: VismaProperties,
    private val supplierMapper: SupplierMapper,
    private val logger: KLogger = KotlinLogging.logger {},
) {
    @Retryable(
        maxAttemptsExpression = "\${adapter.adapters.visma.retry.max-attempts}",
        backoff =
            Backoff(
                delayExpression = "\${adapter.adapters.visma.retry.initial-interval-ms}",
                maxDelayExpression = "10000",
            ),
    )
    fun getCustomerSupplierByIdentifier(
        supplierIdentity: SupplierIdentity,
        tenantId: TenantId,
    ): Supplier? {
        logGetCustomerSupplierByIdentifier(supplierIdentity, tenantId)

        val company = getCompanyFromTenant(tenantId)

        val xmlResponse =
            restClient
                .get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/erp_ws/oauth/reskontro/$company/0")
                        .queryParam("fnr", supplierIdentity.value)
                        .build()
                }.accept(MediaType.TEXT_XML)
                .retrieve()
                .body<VUXml>()
                ?: throw VismaGetSupplierException()

        return supplierMapper.mapSingleSupplier(xmlResponse)
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

    private fun getCompanyFromTenant(tenantId: TenantId): String {
        return props.company.byTenant[tenantId.id]
            ?: run {
                logger.atWarn {
                    message = "Fant ikke noe selskap for tenant"
                    arguments = arrayOf(kv("tenant", tenantId))
                }
                throw VismaTenantToCompanyException(tenantId)
            }
    }

    @Retryable(
        maxAttemptsExpression = "\${adapter.adapters.visma.retry.max-attempts}",
        backoff =
            Backoff(
                delayExpression = "\${adapter.adapters.visma.retry.initial-interval-ms}",
                maxDelayExpression = "10000",
            ),
    )
    fun createCustomerSupplier(
        supplier: Supplier,
        supplierIdentity: SupplierIdentity,
        tenantId: TenantId,
    ) {
        val company = getCompanyFromTenant(tenantId)
        val supplierType = SupplierType.LEVERANDOR // FIXME: Bestemmes av hvert fylke

        val requestBody =
            supplierMapper.mapToVismaRequest(
                supplier = supplier,
                supplierIdentity = supplierIdentity,
                company = company,
                division = DIVISION,
                type = supplierType,
            )

        val response =
            restClient
                .post()
                .uri("/erp_ws/oauth/reskontro")
                .contentType(MediaType.APPLICATION_XML)
                .accept(MediaType.TEXT_XML)
                .body(requestBody)
                .retrieve()
                .body<VUXmlStoreResponse>()
                ?: throw VismaCreateSupplierException()

        val result = response.customerSuppliers

        when {
            result.stored == "true" -> {
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
                logger.atError {
                    message = "Oppretting/oppdatering av leverandør i Visma feilet"
                    arguments =
                        arrayOf(
                            kv("leverandør", supplier.toMaskedLogMap()),
                            kv("supplierIdentity", supplierIdentity.toMaskedLogMap()),
                            kv("tenant", tenantId.id),
                            kv("result", result),
                        )
                }
            }
        }
    }

    companion object {
        private const val DIVISION = "0"
    }
}
