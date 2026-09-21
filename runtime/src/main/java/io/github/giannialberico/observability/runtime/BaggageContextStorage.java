package io.github.giannialberico.observability.runtime;

import java.util.concurrent.atomic.AtomicBoolean;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextStorage;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.api.instrumenter.LocalRootSpan;
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
        Span root = LocalRootSpan.fromContextOrNull(context);

        if (root == null) {
            return;
        }

        if (!root.getSpanContext().isValid()) {
            return;
        }

        Baggage.fromContext(context)
                .asMap()
                .forEach((key, entry) -> root.setAttribute(key, entry.getValue()));
    }
}
