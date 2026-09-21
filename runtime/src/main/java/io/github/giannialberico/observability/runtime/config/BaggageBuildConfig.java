package io.github.giannialberico.observability.runtime.config;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Build-time configuration for mirroring OpenTelemetry baggage attributes to
 * the local root span.
 *
 * <p>Both features are enabled by default. They can be disabled independently
 * with {@code quarkus.otel.baggage.span-processor.enabled} and
 * {@code quarkus.otel.baggage.context-wrapper.enabled}.</p>
 */
@ConfigMapping(prefix = "quarkus.otel.baggage")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface BaggageBuildConfig {

    /**
     * Configuration for the span processor that copies baggage available when
     * a local root span is started.
     */
    SpanProcessor spanProcessor();

    /**
     * Configuration for the context-storage hook that copies baggage whenever
     * an updated OpenTelemetry context becomes current.
     */
    ContextWrapper contextWrapper();

    @ConfigGroup
    interface SpanProcessor {

        /**
         * Enables the baggage span processor.
         *
         * <p>When enabled, baggage present in the span's parent context is
         * copied to the local root span at span start. Defaults to
         * {@code true}.</p>
         */
        @WithDefault("true")
        boolean enabled();

    }

    @ConfigGroup
    interface ContextWrapper {

        /**
         * Enables the context-storage hook for later baggage updates.
         *
         * <p>When enabled, baggage is copied to the local root span whenever
         * a context containing that baggage is attached. Defaults to
         * {@code true}.</p>
         */
        @WithDefault("true")
        boolean enabled();

    }

}
