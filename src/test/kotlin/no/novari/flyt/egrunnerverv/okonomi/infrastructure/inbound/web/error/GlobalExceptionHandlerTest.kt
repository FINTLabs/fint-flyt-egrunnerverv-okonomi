package no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.error

import no.novari.flyt.egrunnerverv.okonomi.domain.error.CreateSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.GenericSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.GetSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.MissingIdentifierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.MultipleIdentifiersException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.TenantToCompanyException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.tenant.MissingGatewayBeanException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.tenant.NoAdapterMappingException
import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.lang.reflect.Method
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaMethod
import kotlin.test.assertEquals

class GlobalExceptionHandlerTest {
    private val handler = GlobalExceptionHandler()

    @Test
    fun `maps NoAdapterMappingException to internal server error`() {
        val response = handler.handleNoAdapterMapping(NoAdapterMappingException("novari-no"))

        assertEquals(500, response.statusCode.value())
        val body = requireNotNull(response.body)
        assertEquals(ApiErrorCode.MISSING_ADAPTER_MAPPING.id, body.errorCode)
    }

    @Test
    fun `maps MissingGatewayBeanException to internal server error`() {
        val response = handler.handleMissingGatewayBean(MissingGatewayBeanException("visma"))

        assertEquals(500, response.statusCode.value())
        val body = requireNotNull(response.body)
        assertEquals(ApiErrorCode.MISSING_GATEWAY_BEAN.id, body.errorCode)
    }

    @Test
    fun `maps domain validation exceptions`() {
        val missing = handler.handleDomainValidationException(MissingIdentifierException())
        val multiple = handler.handleDomainValidationException(MultipleIdentifiersException())

        assertEquals(ApiErrorCode.MISSING_FODSELSNUMMER_OR_ORGID.id, requireNotNull(missing.body).errorCode)
        assertEquals(ApiErrorCode.MULTIPLE_IDENTIFIERS.id, requireNotNull(multiple.body).errorCode)
    }

    @Test
    fun `maps MethodArgumentNotValidException`() {
        val bindingResult = BeanPropertyBindingResult(Any(), "payload")
        bindingResult.addError(ObjectError("payload", "Ugyldig input"))
        val ex =
            MethodArgumentNotValidException(
                MethodParameter.forExecutable(dummyMethodWithParam(), 0),
                bindingResult,
            )

        val response = handler.handleValidation(ex)

        assertEquals(ApiErrorCode.GENERIC_BAD_REQUEST.id, requireNotNull(response.body).errorCode)
    }

    @Test
    fun `maps MethodArgumentTypeMismatchException to generic bad request`() {
        val ex =
            MethodArgumentTypeMismatchException(
                "abc",
                String::class.java,
                "field",
                MethodParameter.forExecutable(dummyMethodWithParam(), 0),
                IllegalArgumentException("bad type"),
            )

        val response = handler.handleMethodArgumentTypeMismatch(ex)

        assertEquals(ApiErrorCode.GENERIC_BAD_REQUEST.id, requireNotNull(response.body).errorCode)
    }

    @Test
    fun `maps SupplierSyncException variants`() {
        val create = handler.handleSupplierSyncException(CreateSupplierException())
        val get = handler.handleSupplierSyncException(GetSupplierException())
        val orgToCompany = handler.handleSupplierSyncException(TenantToCompanyException())
        val generic = handler.handleSupplierSyncException(GenericSupplierException("fail"))

        assertEquals(ApiErrorCode.CREATE_SUPPLIER_ERROR.id, requireNotNull(create.body).errorCode)
        assertEquals(ApiErrorCode.GET_SUPPLIER_ERROR.id, requireNotNull(get.body).errorCode)
        assertEquals(ApiErrorCode.TENANT_TO_COMPANY_ERROR.id, requireNotNull(orgToCompany.body).errorCode)
        assertEquals(ApiErrorCode.GENERIC_SUPPLIER_ERROR.id, requireNotNull(generic.body).errorCode)
    }

    @Test
    fun `maps generic exception`() {
        val response = handler.handleGeneric(RuntimeException("oops"))

        assertEquals(ApiErrorCode.UNKNOWN_ERROR.id, requireNotNull(response.body).errorCode)
    }

    private fun dummyMethodWithParam(): Method =
        Dummy::class.memberFunctions.first { it.name == "dummyWithParam" }.javaMethod!!

    private class Dummy {
        fun dummyWithParam(value: String = "") {}
    }
}
