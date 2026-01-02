package no.novari.flyt.egrunnerverv.okonomi.infrastructure.config

import io.micrometer.core.instrument.MeterRegistry
import no.novari.flyt.egrunnerverv.okonomi.application.supplier.SyncSupplierService
import no.novari.flyt.egrunnerverv.okonomi.application.supplier.SyncSupplierUseCase
import no.novari.flyt.egrunnerverv.okonomi.application.tenant.TenantGatewayResolver
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.EgrunnervervPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationUseCaseConfig(
    private val tenantGatewayResolver: TenantGatewayResolver,
    private val egrunnervervPort: EgrunnervervPort,
    private val meterRegistry: MeterRegistry,
) {
    @Bean
    fun syncSupplierUseCase(): SyncSupplierUseCase {
        return SyncSupplierService(
            tenantGatewayResolver = tenantGatewayResolver,
            egrunnervervPort = egrunnervervPort,
            meterRegistry = meterRegistry,
        )
    }
}
