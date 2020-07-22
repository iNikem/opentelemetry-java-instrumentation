package io.opentelemetry.benchmark.storage;

import java.net.HttpURLConnection;
import java.net.URI;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@Fork(
    jvmArgsAppend = {
      "-javaagent:C:/git/opentelemetry-java-instrumentation/opentelemetry-javaagent/build/libs/opentelemetry-javaagent-0.7.0-SNAPSHOT-all.jar",
      "-Dota.exporter=logging"
    })
@State(Scope.Thread)
public class StorageBenchmark {

  private HttpURLConnection httpURLConnection;
  private String value;

  @Setup
  public void setup() throws Exception {
    httpURLConnection = (HttpURLConnection) new URI("https://google.com").toURL().openConnection();
    value = "abc";
  }

  @Benchmark
  public String noStorage() {
    return value;
  }

  @Benchmark
  public String contextStorage() {
    return contextStorageInstrumented(value);
  }

  private String contextStorageInstrumented(String value) {
    return value;
  }

  @Benchmark
  public String fieldStorage() {
    return fieldStorageInstrumented(httpURLConnection, value);
  }

  private String fieldStorageInstrumented(HttpURLConnection httpURLConnection, String value) {
    return value;
  }
}
