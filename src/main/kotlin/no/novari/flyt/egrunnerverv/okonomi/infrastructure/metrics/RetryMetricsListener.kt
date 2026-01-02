package no.novari.flyt.egrunnerverv.okonomi.infrastructure.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.RetryListener

class RetryMetricsListener(
    private val meterRegistry: MeterRegistry,
    private val tags: Tags,
) : RetryListener {
    override fun <T, E : Throwable?> open(
        context: RetryContext,
        callback: RetryCallback<T, E>,
    ): Boolean {
        return true
    }

    override fun <T, E : Throwable?> close(
        context: RetryContext,
        callback: RetryCallback<T, E>,
        throwable: Throwable?,
    ) {
        // no-op
    }

    override fun <T, E : Throwable?> onError(
        context: RetryContext,
        callback: RetryCallback<T, E>,
        throwable: Throwable,
    ) {
        meterRegistry.counter("external.retry.attempts", tags).increment()
    }
}
