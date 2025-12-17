package no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web

import com.fasterxml.jackson.databind.ObjectMapper
import no.novari.flyt.egrunnerverv.okonomi.application.supplier.SyncSupplierUseCase
import no.novari.flyt.egrunnerverv.okonomi.domain.model.Supplier
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierSyncResult
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.config.TenantIdConverter
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.dto.ErrorResponse
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.dto.SupplierRequest
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.error.ApiErrorCode
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.error.GlobalExceptionHandler
import no.novari.flyt.webresourceserver.security.client.sourceapplication.SourceApplicationAuthorityMappingService
import no.novari.flyt.webresourceserver.security.client.sourceapplication.SourceApplicationAuthorizationRequestService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import kotlin.test.assertContains

@WebMvcTest(controllers = [SupplierController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(TenantIdConverter::class, GlobalExceptionHandler::class, SupplierControllerTest.StubConfig::class)
@TestPropertySource(
    properties = [
        "adapter.supplier.by-tenant.novari-no=visma",
    ],
)
class SupplierControllerTest
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
        private val objectMapper: ObjectMapper,
    ) {
        @MockitoBean
        private lateinit var sourceApplicationAuthorizationRequestService: SourceApplicationAuthorizationRequestService

        @MockitoBean
        private lateinit var sourceApplicationAuthorityMappingService: SourceApplicationAuthorityMappingService

        @TestConfiguration
        class StubConfig {
            @Bean
            fun syncSupplierUseCase(): SyncSupplierUseCase {
                return object : SyncSupplierUseCase {
                    override fun getOrCreate(
                        supplier: Supplier,
                        supplierIdentity: SupplierIdentity,
                        tenantId: TenantId,
                    ): SupplierSyncResult {
                        return SupplierSyncResult.Created
                    }
                }
            }
        }

        private fun validRequest(
            fnr: String? = null,
            orgId: String? = "123456789",
        ): SupplierRequest =
            SupplierRequest(
                fodselsNummer = fnr,
                orgId = orgId,
                name = "Leverandør AS",
                kontoNummer = "1234.56.78901",
                street = "Gate 1",
                zip = "0010",
                city = "Oslo",
                email = "post@test.no",
            )

        @Test
        fun `returns ok when leverandor is created`() {
            mockMvc
                .post("/api/v1/egrunnerverv/okonomi/supplier") {
                    contentType = MediaType.APPLICATION_JSON
                    header("X-Tenant", "novari-no")
                    content = objectMapper.writeValueAsString(validRequest())
                }.andExpect {
                    status { isOk() }
                }
        }

        @Test
        fun `returns bad request when both identifiers missing`() {
            mockMvc
                .post("/api/v1/egrunnerverv/okonomi/supplier") {
                    contentType = MediaType.APPLICATION_JSON
                    header("X-Tenant", "novari-no")
                    content = objectMapper.writeValueAsString(validRequest(fnr = "", orgId = ""))
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.error_code") { value(ApiErrorCode.MISSING_FODSELSNUMMER_OR_ORGID.id) }
                }
        }

        @Test
        fun `returns bad request when both identifiers provided`() {
            mockMvc
                .post("/api/v1/egrunnerverv/okonomi/supplier") {
                    contentType = MediaType.APPLICATION_JSON
                    header("X-Tenant", "novari-no")
                    content = objectMapper.writeValueAsString(validRequest(fnr = "12345678901", orgId = "123456789"))
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.error_code") { value(ApiErrorCode.MULTIPLE_IDENTIFIERS.id) }
                }
        }

        @Test
        fun `returns validation error on invalid orgId`() {
            mockMvc
                .post("/api/v1/egrunnerverv/okonomi/supplier") {
                    contentType = MediaType.APPLICATION_JSON
                    header("X-Tenant", "novari-no")
                    content = objectMapper.writeValueAsString(validRequest(orgId = "abc"))
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.error_code") { value(ApiErrorCode.GENERIC_BAD_REQUEST.id) }
                }
        }

        @Test
        fun `returns invalid tenant error when tenant header is unknown`() {
            val response =
                mockMvc
                    .post("/api/v1/egrunnerverv/okonomi/supplier") {
                        contentType = MediaType.APPLICATION_JSON
                        header("X-Tenant", "unknown")
                        content = objectMapper.writeValueAsString(validRequest())
                    }.andExpect {
                        status { isBadRequest() }
                        jsonPath("$.error_code") { value(ApiErrorCode.INVALID_TENANT_IDENTIFIER.id) }
                    }.andReturn()
                    .response
                    .contentAsString

            val error = objectMapper.readValue(response, ErrorResponse::class.java)
            assertContains(error.errorMessage, "Ukjent tenant-verdi 'unknown'. Kontroller X-Tenant-headeren.")
        }
    }
