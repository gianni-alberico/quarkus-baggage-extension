package io.github.giannialberico.observability.deployment;

import io.github.giannialberico.observability.runtime.BaggageMirrorRecorder;
import io.github.giannialberico.observability.runtime.BaggageSpanProcessor;
import io.github.giannialberico.observability.runtime.config.BaggageBuildConfig;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BaggageProcessor {

    private static final String FEATURE = "custom";
    private static final Logger log = LoggerFactory.getLogger(BaggageProcessor.class);

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void registerBaggageSpanProcessor(BuildProducer<AdditionalBeanBuildItem> producer, BaggageBuildConfig baggageBuildConfig) {
        if (baggageBuildConfig.spanProcessor().enabled()) {
            log.info("Baggage processor enabled");
            producer.produce(AdditionalBeanBuildItem.unremovableOf(BaggageSpanProcessor.class));
        }
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void installBaggageMirror(BaggageMirrorRecorder recorder, BaggageBuildConfig baggageBuildConfig) {
        if (baggageBuildConfig.contextWrapper().enabled()) {
            log.info("Install baggage mirror");
            recorder.install();
        }
    }
}
