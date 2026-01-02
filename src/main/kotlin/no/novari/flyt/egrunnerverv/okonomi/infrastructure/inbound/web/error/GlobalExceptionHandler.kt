package no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.error

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.MeterRegistry
import net.logstash.logback.argument.StructuredArguments.kv
import no.novari.flyt.egrunnerverv.okonomi.domain.error.CreateSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.DomainValidationException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.GenericSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.GetSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.IdentifierTooLongException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.MissingIdentifierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.MultipleIdentifiersException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.ServiceNowSyncException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.SupplierSyncException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.TenantToCompanyException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.dto.ErrorResponse
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.tenant.MissingGatewayBeanException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.tenant.NoAdapterMappingException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler(
    private val meterRegistry: MeterRegistry,
) {
    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(DomainValidationException::class)
    fun handleDomainValidationException(ex: DomainValidationException): ResponseEntity<ErrorResponse> {
        val apiErrorCode =
            when (ex) {
                is MissingIdentifierException -> ApiErrorCode.MISSING_FODSELSNUMMER_OR_ORGID
                is MultipleIdentifiersException -> ApiErrorCode.MULTIPLE_IDENTIFIERS
                is IdentifierTooLongException -> ApiErrorCode.IDENTIFIER_TOO_LONG
            }
        recordValidationError(apiErrorCode)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    errorCode = apiErrorCode.id,
                    errorMessage = requireNotNull(ex.message),
                ),
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val msg =
            ex.bindingResult.allErrors.joinToString("; ") {
                it.defaultMessage ?: "Ugyldig forespørsel. Se detaljer for feltfeil."
            }
        recordValidationError(ApiErrorCode.GENERIC_BAD_REQUEST)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    errorCode = ApiErrorCode.GENERIC_BAD_REQUEST.id,
                    errorMessage = msg,
                ),
            )
    }

    @ExceptionHandler(InvalidOrganizationNumberException::class)
    fun handleInvalidOrganizationNumber(ex: InvalidOrganizationNumberException): ResponseEntity<ErrorResponse> {
        recordValidationError(ApiErrorCode.INVALID_ORGANIZATION_NUMBER)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    errorCode = ApiErrorCode.INVALID_ORGANIZATION_NUMBER.id,
                    errorMessage = requireNotNull(ex.message),
                ),
            )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        val apiErrorCode =
            when (ex.rootCause) {
                is InvalidOrganizationNumberException -> ApiErrorCode.INVALID_ORGANIZATION_NUMBER
                else -> ApiErrorCode.GENERIC_BAD_REQUEST
            }
        recordValidationError(apiErrorCode)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    errorCode = apiErrorCode.id,
                    errorMessage = requireNotNull(ex.rootCause?.message ?: ex.cause?.message ?: ex.message),
                ),
            )
    }

    @ExceptionHandler(NoAdapterMappingException::class)
    fun handleNoAdapterMapping(ex: NoAdapterMappingException): ResponseEntity<ErrorResponse> {
        recordTenantError(ApiErrorCode.MISSING_ADAPTER_MAPPING)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    errorCode = ApiErrorCode.MISSING_ADAPTER_MAPPING.id,
                    errorMessage = requireNotNull(ex.message),
                ),
            )
    }

    @ExceptionHandler(MissingGatewayBeanException::class)
    fun handleMissingGatewayBean(ex: MissingGatewayBeanException): ResponseEntity<ErrorResponse> {
        recordTenantError(ApiErrorCode.MISSING_GATEWAY_BEAN)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    errorCode = ApiErrorCode.MISSING_GATEWAY_BEAN.id,
                    errorMessage = requireNotNull(ex.message),
                ),
            )
    }

    @ExceptionHandler(SupplierSyncException::class)
    fun handleSupplierSyncException(ex: SupplierSyncException): ResponseEntity<ErrorResponse> {
        val msg = requireNotNull(ex.message)
        val apiErrorCode =
            when (ex) {
                is CreateSupplierException -> ApiErrorCode.CREATE_SUPPLIER_ERROR
                is GenericSupplierException -> ApiErrorCode.GENERIC_SUPPLIER_ERROR
                is GetSupplierException -> ApiErrorCode.GET_SUPPLIER_ERROR
                is TenantToCompanyException -> ApiErrorCode.TENANT_TO_COMPANY_ERROR
                is ServiceNowSyncException -> ApiErrorCode.SERVICE_NOW_SYNC_ERROR
            }
        recordSyncFailure(apiErrorCode)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    errorCode = apiErrorCode.id,
                    errorMessage = msg,
                ),
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.atError {
            message = "Ukjent feil under håndtering av forespørsel"
            arguments =
                arrayOf(
                    kv("cause", ex.cause),
                )
        }
        recordSyncFailure(ApiErrorCode.UNKNOWN_ERROR)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    errorCode = ApiErrorCode.UNKNOWN_ERROR.id,
                    errorMessage = "En uventet feil oppsto. Prøv igjen, eller kontakt FLYT hvis problemet fortsetter.",
                ),
            )
    }

    private fun recordValidationError(apiErrorCode: ApiErrorCode) {
        meterRegistry
            .counter(
                "supplier.validation.error.count",
                "error_code",
                apiErrorCode.id.toString(),
            ).increment()
        recordSyncFailure(apiErrorCode)
    }

    private fun recordTenantError(apiErrorCode: ApiErrorCode) {
        meterRegistry
            .counter(
                "supplier.tenant.error.count",
                "error_code",
                apiErrorCode.id.toString(),
            ).increment()
        recordSyncFailure(apiErrorCode)
    }

    private fun recordSyncFailure(apiErrorCode: ApiErrorCode) {
        meterRegistry
            .counter(
                "supplier.sync.count",
                "tenant",
                "unknown",
                "identity_type",
                "unknown",
                "outcome",
                "failed",
                "error_code",
                apiErrorCode.id.toString(),
            ).increment()
    }
}
