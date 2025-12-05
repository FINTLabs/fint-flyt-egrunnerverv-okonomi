package no.novari.flyt.egrunnerverv.okonomi.controller

import no.novari.flyt.egrunnerverv.okonomi.model.enum.Organization
import no.novari.flyt.egrunnerverv.okonomi.model.payload.GetOrCreateSupplier
import no.novari.flyt.egrunnerverv.okonomi.service.SupplierService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/egrunnerverv/okonomi/supplier")
class SupplierController(
    private val supplierService: SupplierService,
) {
    // TODO: Håndter feil bedre
    @PostMapping
    fun getOrCreateSupplier(
        // TODO: Valider request
        @RequestBody supplier: GetOrCreateSupplier,
        @RequestHeader(
            "X-Organization",
            defaultValue = "NOVARI",
        ) organization: Organization, // TODO: Get organization from a proper place
    ): ResponseEntity<Void> {
        supplierService.getOrCreate(supplier, organization)

        // Alltid returner "OK" uansett om leverandør ble opprettet eller oppdatert (om nødvendig)
        return ResponseEntity.ok().build()

        // Ved feil:
        // return ResponseEntity.badRequest().build()
        // inkluder `error_code` og `error_message`
    }
}
