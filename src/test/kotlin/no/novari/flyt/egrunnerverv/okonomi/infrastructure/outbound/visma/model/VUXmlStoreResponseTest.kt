package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.model

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlin.test.Test
import kotlin.test.assertEquals

class VUXmlStoreResponseTest {
    @Test
    fun `deserializes store response`() {
        val xml =
            """
            <VUXML>
              <customerSuppliers company="123">
                <stored>true</stored>
                <updated>false</updated>
                <errors>none</errors>
              </customerSuppliers>
            </VUXML>
            """.trimIndent()

        val mapper = XmlMapper().registerKotlinModule()
        val response = mapper.readValue(xml, VUXmlStoreResponse::class.java)

        assertEquals("123", response.customerSuppliers.company)
        assertEquals("true", response.customerSuppliers.stored)
        assertEquals("false", response.customerSuppliers.updated)
        assertEquals("none", response.customerSuppliers.errors)
    }
}
