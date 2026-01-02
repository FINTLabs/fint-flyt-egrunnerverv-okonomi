package no.novari.flyt.egrunnerverv.okonomi.infrastructure.metrics

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.config.AdapterSupplierProperties
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.config.VismaProperties
import org.springframework.stereotype.Component

@Component
class MetricsRegistrar(
    meterRegistry: MeterRegistry,
    adapterSupplierProperties: AdapterSupplierProperties,
    vismaProperties: VismaProperties,
) {
    init {
        Gauge
            .builder("adapter.tenant.count") { adapterSupplierProperties.byTenant.size.toDouble() }
            .register(meterRegistry)
        Gauge
            .builder("adapter.orgno.count") { adapterSupplierProperties.byOrgNo.size.toDouble() }
            .register(meterRegistry)
        Gauge
            .builder("visma.tenant.count") { vismaProperties.tenants.size.toDouble() }
            .register(meterRegistry)
    }
}
