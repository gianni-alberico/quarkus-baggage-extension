package io.github.giannialberico.observability.runtime;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageEntry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextStorage;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.api.instrumenter.LocalRootSpan;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds a hook to OpenTelemetry context attachment so baggage updates are
 * mirrored when the updated context becomes current.
 */
public final class BaggageContextStorage {

    private static final AtomicBoolean INSTALLED = new AtomicBoolean();
    private static final Logger log = LoggerFactory.getLogger(BaggageContextStorage.class);

    private BaggageContextStorage() {
    }

    public static void install() {
        if (!INSTALLED.compareAndSet(false, true)) {
            return;
        }

        log.info("Installing context storage");

        ContextStorage.addWrapper(delegate -> new ContextStorage() {
            @Override
            public Scope attach(Context context) {
                Scope scope = delegate.attach(context);
                mirror(context);
                return scope;
            }

            @Override
            public Context current() {
                return delegate.current();
            }
        });

        log.info("Wrapper added: {}", ContextStorage.get());
    }

    private static void mirror(Context context) {
        ReadWriteSpan root = (ReadWriteSpan) LocalRootSpan.fromContext(context);
        Span current = Span.fromContext(context);

        Baggage baggage = Baggage.current();

        if (current.getSpanContext().isValid()) {
            baggage.asMap().forEach((key, entry) -> {
                if (root.getAttribute(AttributeKey.stringKey(key)) == null) {
                    current.setAttribute(key, entry.getValue());
                }
            });
        }

        if (root.getSpanContext().isValid()) {
            setBaggageToSpan(baggage, root);
        }
    }

    private static void setBaggageToSpan(Baggage baggage, Span span) {
        baggage.asMap().forEach((key, entry) -> span.setAttribute(key, entry.getValue()));
    }
}
