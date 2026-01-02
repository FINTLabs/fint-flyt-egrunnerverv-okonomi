package no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.error

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.novari.flyt.egrunnerverv.okonomi.domain.error.CreateSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.GenericSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.GetSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.IdentifierTooLongException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.MissingIdentifierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.MultipleIdentifiersException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.ServiceNowSyncException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.TenantToCompanyException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.dto.ErrorResponse
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.tenant.MissingGatewayBeanException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.tenant.NoAdapterMappingException
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GlobalExceptionHandlerTest {
    private val handler = GlobalExceptionHandler(SimpleMeterRegistry())

    @Test
    fun `handleDomainValidationException maps missing identifier`() {
        val response = handler.handleDomainValidationException(MissingIdentifierException())

        assertError(
            response = response,
            status = HttpStatus.BAD_REQUEST,
            code = ApiErrorCode.MISSING_FODSELSNUMMER_OR_ORGID,
        )
    }

    @Test
    fun `handleDomainValidationException maps multiple identifiers`() {
        val response = handler.handleDomainValidationException(MultipleIdentifiersException())

        assertError(
            response = response,
            status = HttpStatus.BAD_REQUEST,
            code = ApiErrorCode.MULTIPLE_IDENTIFIERS,
        )
    }

    @Test
    fun `handleDomainValidationException maps identifier too long`() {
        val response = handler.handleDomainValidationException(IdentifierTooLongException())

        assertError(
            response = response,
            status = HttpStatus.BAD_REQUEST,
            code = ApiErrorCode.IDENTIFIER_TOO_LONG,
        )
    }

    @Test
    fun `handleValidation returns generic bad request with combined message`() {
        val bindingResult = BeanPropertyBindingResult(Any(), "request")
        bindingResult.addError(ObjectError("request", "first error"))
        bindingResult.addError(ObjectError("request", "second error"))
        val exception = MethodArgumentNotValidException(methodParameter(), bindingResult)

        val response = handler.handleValidation(exception)

        assertError(
            response = response,
            status = HttpStatus.BAD_REQUEST,
            code = ApiErrorCode.GENERIC_BAD_REQUEST,
        )
        assertEquals("first error; second error", response.body?.errorMessage)
    }

    @Test
    fun `handleInvalidOrganizationNumber returns bad request with org number code`() {
        val response = handler.handleInvalidOrganizationNumber(InvalidOrganizationNumberException(123L))

        assertError(
            response = response,
            status = HttpStatus.BAD_REQUEST,
            code = ApiErrorCode.INVALID_ORGANIZATION_NUMBER,
        )
    }

    @Test
    fun `handleMethodArgumentTypeMismatch maps invalid org number`() {
        val exception =
            MethodArgumentTypeMismatchException(
                "abc",
                Long::class.java,
                "orgNo",
                methodParameter(),
                InvalidOrganizationNumberException(123L),
            )

        val response = handler.handleMethodArgumentTypeMismatch(exception)

        assertError(
            response = response,
            status = HttpStatus.BAD_REQUEST,
            code = ApiErrorCode.INVALID_ORGANIZATION_NUMBER,
        )
    }

    @Test
    fun `handleMethodArgumentTypeMismatch maps generic mismatch`() {
        val exception =
            MethodArgumentTypeMismatchException(
                "abc",
                Long::class.java,
                "orgNo",
                methodParameter(),
                NumberFormatException("For input string: abc"),
            )

        val response = handler.handleMethodArgumentTypeMismatch(exception)

        assertError(
            response = response,
            status = HttpStatus.BAD_REQUEST,
            code = ApiErrorCode.GENERIC_BAD_REQUEST,
        )
    }

    @Test
    fun `handleNoAdapterMapping returns 500 with missing adapter mapping code`() {
        val response = handler.handleNoAdapterMapping(NoAdapterMappingException("novari-no"))

        assertError(
            response = response,
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            code = ApiErrorCode.MISSING_ADAPTER_MAPPING,
        )
    }

    @Test
    fun `handleMissingGatewayBean returns 500 with missing gateway bean code`() {
        val response = handler.handleMissingGatewayBean(MissingGatewayBeanException("visma"))

        assertError(
            response = response,
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            code = ApiErrorCode.MISSING_GATEWAY_BEAN,
        )
    }

    @Test
    fun `handleSupplierSyncException maps create supplier`() {
        val response = handler.handleSupplierSyncException(CreateSupplierException())

        assertError(
            response = response,
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            code = ApiErrorCode.CREATE_SUPPLIER_ERROR,
        )
    }

    @Test
    fun `handleSupplierSyncException maps generic supplier`() {
        val response = handler.handleSupplierSyncException(GenericSupplierException("boom"))

        assertError(
            response = response,
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            code = ApiErrorCode.GENERIC_SUPPLIER_ERROR,
        )
    }

    @Test
    fun `handleSupplierSyncException maps get supplier`() {
        val response = handler.handleSupplierSyncException(GetSupplierException())

        assertError(
            response = response,
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            code = ApiErrorCode.GET_SUPPLIER_ERROR,
        )
    }

    @Test
    fun `handleSupplierSyncException maps tenant to company`() {
        val response = handler.handleSupplierSyncException(TenantToCompanyException())

        assertError(
            response = response,
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            code = ApiErrorCode.TENANT_TO_COMPANY_ERROR,
        )
    }

    @Test
    fun `handleSupplierSyncException maps ServiceNow sync`() {
        val response = handler.handleSupplierSyncException(ServiceNowSyncException("boom"))

        assertError(
            response = response,
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            code = ApiErrorCode.SERVICE_NOW_SYNC_ERROR,
        )
    }

    @Test
    fun `handleGeneric returns unknown error`() {
        val response = handler.handleGeneric(Exception("boom"))

        assertError(
            response = response,
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            code = ApiErrorCode.UNKNOWN_ERROR,
        )
        assertEquals(response.body?.errorMessage?.contains("En uventet feil oppsto"), true)
    }

    private fun methodParameter(): MethodParameter {
        val method = DummyController::class.java.getDeclaredMethod("handle", Long::class.javaPrimitiveType)
        return MethodParameter(method, 0)
    }

    private fun assertError(
        response: ResponseEntity<ErrorResponse>,
        status: HttpStatus,
        code: ApiErrorCode,
    ) {
        assertEquals(status, response.statusCode)
        assertEquals(code.id, response.body?.errorCode)
        assertNotNull(response.body?.errorMessage)
    }

    private class DummyController {
        @Suppress("UNUSED_PARAMETER")
        fun handle(orgNo: Long) {
            // Used to construct MethodParameter for tests.
        }
    }
}
