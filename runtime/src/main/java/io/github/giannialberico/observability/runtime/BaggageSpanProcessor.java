package io.github.giannialberico.observability.runtime;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.LocalRootSpan;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.internal.ExtendedSpanProcessor;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Copies baggage that exists when a local-root span is started to that span.
 *
 * <p>Later baggage changes are handled by {@link BaggageContextStorage}; this
 * processor is deliberately not dependent on the thread that eventually ends
 * the span.</p>
 */
@ApplicationScoped
@Unremovable
public class BaggageSpanProcessor implements ExtendedSpanProcessor {

    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {
        if (LocalRootSpan.fromContextOrNull(parentContext) != null) {
            return;
        }

        // if no LocalRootSpan from parentContext
        // span is root span

        Baggage.fromContext(parentContext)
                .asMap()
                .forEach((key, entry) -> span.setAttribute(AttributeKey.stringKey(key), entry.getValue()));
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnding(ReadWriteSpan span) {
        // no-op
    }

    @Override
    public boolean isOnEndingRequired() {
        return false;
    }

    @Override
    public void onEnd(ReadableSpan span) {
        // no-op
    }

    @Override
    public boolean isEndRequired() {
        return false;
    }
}
