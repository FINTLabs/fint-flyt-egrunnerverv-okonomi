package no.novari.flyt.egrunnerverv.okonomi.exception

import no.novari.flyt.egrunnerverv.okonomi.model.enum.ApiErrorCode
import org.springframework.http.HttpStatus

sealed class ApiException(
    val httpStatus: HttpStatus,
    val errorCode: Int,
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class InvalidIdentifierException :
    ApiException(
        HttpStatus.BAD_REQUEST,
        ApiErrorCode.FODSELSNUMMER_OR_ORGID.id,
        "Enten fødselsnummer eller orgId må bli sendt",
    )

class VismaGetSupplierException(
    cause: Throwable? = null,
) : ApiException(
        HttpStatus.INTERNAL_SERVER_ERROR,
        ApiErrorCode.VISMA_GET_SUPPLIER_ERROR.id,
        "Klarte ikke å hente leverandør fra Visma",
        cause,
    )

class VismaCreateSupplierException(
    cause: Throwable? = null,
) : ApiException(
        HttpStatus.INTERNAL_SERVER_ERROR,
        ApiErrorCode.VISMA_CREATE_SUPPLIER_ERROR.id,
        "Klarte ikke å opprette leverandør i Visma",
        cause,
    )

class VismaOrganizationToCompanyException(
    cause: Throwable? = null,
) : ApiException(
        HttpStatus.INTERNAL_SERVER_ERROR,
        ApiErrorCode.VISMA_ORGANIZATION_TO_COMPANY_ERROR.id,
        "Klarte ikke å finne firma for gitt organisasjon",
        cause,
    )

class PayloadValidationException(
    msg: String,
) : ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.GENERIC_VALIDATION_ERROR.id, msg)
