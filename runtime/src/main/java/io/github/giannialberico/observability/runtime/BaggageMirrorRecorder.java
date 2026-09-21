package io.github.giannialberico.observability.runtime;

import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class BaggageMirrorRecorder {

    public void install() {
        BaggageContextStorage.install();
    }
}
