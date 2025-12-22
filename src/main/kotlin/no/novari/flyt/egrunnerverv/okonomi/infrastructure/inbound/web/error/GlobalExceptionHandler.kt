package no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.error

import io.github.oshai.kotlinlogging.KotlinLogging
import no.novari.flyt.egrunnerverv.okonomi.domain.error.CreateSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.DomainValidationException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.GenericSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.GetSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.IdentifierTooLongException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.MissingIdentifierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.MultipleIdentifiersException
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
class GlobalExceptionHandler {
    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(DomainValidationException::class)
    fun handleDomainValidationException(ex: DomainValidationException): ResponseEntity<ErrorResponse> {
        val apiErrorCode =
            when (ex) {
                is MissingIdentifierException -> ApiErrorCode.MISSING_FODSELSNUMMER_OR_ORGID
                is MultipleIdentifiersException -> ApiErrorCode.MULTIPLE_IDENTIFIERS
                is IdentifierTooLongException -> ApiErrorCode.IDENTIFIER_TOO_LONG
            }
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
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    errorCode =
                        when (ex.rootCause) {
                            is InvalidOrganizationNumberException -> ApiErrorCode.INVALID_ORGANIZATION_NUMBER.id
                            else -> ApiErrorCode.GENERIC_BAD_REQUEST.id
                        },
                    errorMessage = ex.rootCause?.message ?: ex.cause?.message ?: ex.message,
                ),
            )
    }

    @ExceptionHandler(NoAdapterMappingException::class)
    fun handleNoAdapterMapping(ex: NoAdapterMappingException): ResponseEntity<ErrorResponse> {
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
            }
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
        logger.error(ex) {
            "Ukjent feil under håndtering av forespørsel"
        }
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    errorCode = ApiErrorCode.UNKNOWN_ERROR.id,
                    errorMessage = "En uventet feil oppsto. Prøv igjen, eller kontakt FLYT hvis problemet fortsetter.",
                ),
            )
    }
}
