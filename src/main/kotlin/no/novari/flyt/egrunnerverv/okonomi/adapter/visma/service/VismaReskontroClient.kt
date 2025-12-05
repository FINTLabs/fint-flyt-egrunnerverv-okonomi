package no.novari.flyt.egrunnerverv.okonomi.adapter.visma.service

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.novari.flyt.egrunnerverv.okonomi.adapter.visma.config.VismaProperties
import no.novari.flyt.egrunnerverv.okonomi.adapter.visma.mapper.SupplierMapper
import no.novari.flyt.egrunnerverv.okonomi.adapter.visma.model.VUXml
import no.novari.flyt.egrunnerverv.okonomi.adapter.visma.model.VUXmlStoreResponse
import no.novari.flyt.egrunnerverv.okonomi.model.enum.Organization
import no.novari.flyt.egrunnerverv.okonomi.model.enum.SupplierType
import no.novari.flyt.egrunnerverv.okonomi.model.payload.GetOrCreateSupplier
import no.novari.flyt.egrunnerverv.okonomi.model.resource.Supplier
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class VismaReskontroClient(
    @param:Qualifier("vismaRestClient") private val restClient: RestClient,
    private val props: VismaProperties,
    private val supplierMapper: SupplierMapper,
    private val logger: KLogger = KotlinLogging.logger {},
) {
    fun getCustomerSupplierByIdentifier(
        identifier: String,
        organization: Organization,
    ): Supplier? {
        logger.atInfo {
            message = "Henter leverandør"
            arguments = arrayOf(kv("fnr", identifier))
        }

        val company = getCompanyFromOrganization(organization)

        val xmlResponse =
            restClient
                .get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/erp_ws/oauth/reskontro/$company/0")
                        .queryParam("fnr", identifier)
                        .build()
                }.accept(MediaType.TEXT_XML)
                .retrieve()
                .body(VUXml::class.java)
                ?: throw IllegalStateException("Ugyldig svar fra Visma")

        return supplierMapper.mapSingleSupplier(xmlResponse)
    }

    private fun getCompanyFromOrganization(organization: Organization): String {
        return props.companyByOrganization.get(organization.id)
            ?: run {
                logger.atWarn {
                    message = "Fant ikke noe selskap gitt organisation"
                    arguments = arrayOf(kv("organisasjon", organization))
                }
                throw IllegalStateException("Fant ikke selskap")
            }
    }

    fun createCustomerSupplier(
        supplier: GetOrCreateSupplier,
        organization: Organization,
    ) {
        logger.atInfo {
            message = "Oppretter leverandør"
            arguments = arrayOf(kv("leverandør", supplier))
        }

        val company = getCompanyFromOrganization(organization)
        val supplierType = SupplierType.LEVERANDOR // FIXME: Bestemmes av hvert fylke

        val requestBody =
            supplierMapper.mapToVismaRequest(
                supplier = supplier,
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
                .body(VUXmlStoreResponse::class.java)
                ?: throw IllegalStateException("Ugyldig svar fra Visma")

        val result = response.customerSuppliers

        logger.atInfo {
            message = "Leverandør opprettet"
            arguments = arrayOf(kv("leverandør", supplier), kv("result", result))
        }
    }

    companion object {
        private const val DIVISION = "0"
    }
}
