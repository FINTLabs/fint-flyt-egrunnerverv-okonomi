package no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.error

import no.novari.flyt.egrunnerverv.okonomi.domain.error.CreateSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.DomainValidationException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.GenericSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.GetSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.MissingIdentifierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.MultipleIdentifiersException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.OrganizationToCompanyException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.SupplierSyncException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(DomainValidationException::class)
    fun handleDomainValidationException(ex: DomainValidationException): ResponseEntity<ErrorResponse> {
        val apiErrorCode =
            when (ex) {
                is MissingIdentifierException -> ApiErrorCode.MISSING_FODSELSNUMMER_OR_ORGID
                is MultipleIdentifiersException -> ApiErrorCode.MULTIPLE_IDENTIFIERS
            }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    errorCode = apiErrorCode.id,
                    errorMessage = ex.message ?: "Ukjent feil",
                ),
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val msg = ex.bindingResult.allErrors.joinToString("; ") { it.defaultMessage ?: "Ugyldig input" }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    errorCode = ApiErrorCode.GENERIC_BAD_REQUEST.id,
                    errorMessage = msg,
                ),
            )
    }

    @ExceptionHandler(InvalidTenantException::class)
    fun handleInvalidTenant(ex: InvalidTenantException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    errorCode = ApiErrorCode.INVALID_TENANT_IDENTIFIER.id,
                    errorMessage = ex.message ?: "Ugyldig tenant-verdi",
                ),
            )

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    errorCode = ApiErrorCode.GENERIC_BAD_REQUEST.id,
                    errorMessage = ex.message,
                ),
            )

    @ExceptionHandler(SupplierSyncException::class)
    fun handleSupplierSyncException(ex: SupplierSyncException): ResponseEntity<ErrorResponse> {
        val msg = ex.message ?: "Ukjent feil i Visma klient"
        val apiErrorCode =
            when (ex) {
                is CreateSupplierException -> ApiErrorCode.CREATE_SUPPLIER_ERROR
                is GenericSupplierException -> ApiErrorCode.GENERIC_SUPPLIER_ERROR
                is GetSupplierException -> ApiErrorCode.GET_SUPPLIER_ERROR
                is OrganizationToCompanyException -> ApiErrorCode.ORGANIZATION_TO_COMPANY_ERROR
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
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    errorCode = ApiErrorCode.UNKNOWN_ERROR.id,
                    errorMessage = "Uventet feil: ${ex.message}",
                ),
            )
    }
}
