package no.novari.flyt.egrunnerverv.okonomi.config

import no.novari.flyt.egrunnerverv.okonomi.exception.ApiException
import no.novari.flyt.egrunnerverv.okonomi.model.enum.ApiErrorCode
import no.novari.flyt.egrunnerverv.okonomi.model.resource.ApiErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): ResponseEntity<ApiErrorResponse> {
        val body =
            ApiErrorResponse(
                errorCode = ex.errorCode,
                errorMessage = ex.message ?: "Unknown error",
            )
        return ResponseEntity.status(ex.httpStatus).body(body)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiErrorResponse> {
        val msg = ex.bindingResult.allErrors.joinToString("; ") { it.defaultMessage ?: "Invalid input" }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse(1003, msg))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ApiErrorResponse> {
        // Dette handler uforutsette feil
        val body =
            ApiErrorResponse(
                errorCode = ApiErrorCode.UNKNOWN_ERROR.id,
                errorMessage = "Unexpected error: ${ex.message}",
            )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }
}
