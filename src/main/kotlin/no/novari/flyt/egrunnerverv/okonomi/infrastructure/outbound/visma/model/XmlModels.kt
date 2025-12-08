package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "VUXML")
data class VUXml(
    @field:JacksonXmlProperty(localName = "customerSuppliers")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val customerSuppliers: List<CustomerSuppliers>,
)

data class CustomerSuppliers(
    @field:JacksonXmlProperty(isAttribute = true)
    val company: String,
    @field:JacksonXmlProperty(isAttribute = true)
    val division: String,
    @field:JacksonXmlProperty(localName = "customerSupplier")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val customerSupplier: List<CustomerSupplier> = emptyList(),
)

data class CustomerSupplier(
    @field:JacksonXmlProperty(isAttribute = true)
    val csType: String,
    @field:JacksonXmlProperty(isAttribute = true)
    val csId: String? = null,
    val csName: String? = null,
    @field:JacksonXmlProperty(localName = "csAddress")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val csAddress: List<String>? = emptyList(),
    val csPostalAddress: PostalAddress? = null,
    val csEmail: String? = null,
    val csWWW: String? = null,
    val bankAccount: String? = null,
    val orgNo: String? = null,
)

data class PostalAddress(
    val zipCode: String? = null,
    val city: String? = null,
)

@JacksonXmlRootElement(localName = "VUXML")
data class VUXmlStoreResponse(
    @field:JacksonXmlProperty(localName = "customerSuppliers")
    val customerSuppliers: StoreResult,
)

data class StoreResult(
    @field:JacksonXmlProperty(isAttribute = true)
    val company: String,
    val stored: String?,
    val updated: String?,
    val errors: String?,
)
