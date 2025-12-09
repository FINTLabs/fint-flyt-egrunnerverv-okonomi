package no.novari.flyt.egrunnerverv.okonomi.infrastructure.config

import no.novari.flyt.egrunnerverv.okonomi.application.supplier.SyncSupplierService
import no.novari.flyt.egrunnerverv.okonomi.application.supplier.SyncSupplierUseCase
import no.novari.flyt.egrunnerverv.okonomi.application.tenant.TenantGatewayResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationUseCaseConfig(
    private val tenantGatewayResolver: TenantGatewayResolver,
) {
    @Bean
    fun syncSupplierUseCase(): SyncSupplierUseCase = SyncSupplierService(tenantGatewayResolver)
}
