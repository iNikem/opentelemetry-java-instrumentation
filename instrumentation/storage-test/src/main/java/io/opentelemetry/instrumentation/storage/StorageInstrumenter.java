package io.opentelemetry.instrumentation.storage;

import static io.opentelemetry.context.ContextUtils.withScopedContext;
import static io.opentelemetry.instrumentation.storage.StorageDecorator.DECORATE;
import static java.util.Collections.singletonMap;
import static net.bytebuddy.matcher.ElementMatchers.named;

import com.google.auto.service.AutoService;
import io.opentelemetry.auto.bootstrap.InstrumentationContext;
import io.opentelemetry.auto.tooling.Instrumenter;
import io.opentelemetry.auto.tooling.Instrumenter.Default;
import io.opentelemetry.context.Scope;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.Argument;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

@AutoService(Instrumenter.class)
public class StorageInstrumenter extends Default {

  public StorageInstrumenter() {
    super("storage");
  }

  @Override
  public ElementMatcher<? super TypeDescription> typeMatcher() {
    return named("io.opentelemetry.benchmark.storage.StorageBenchmark");
  }

  @Override
  public Map<? extends ElementMatcher<? super MethodDescription>, String> transformers() {
    Map result = new HashMap();
    result.put(named("contextStorageInstrumented"), getClass().getName() + "$ContextStorageAdvice");
    result.put(named("fieldStorageInstrumented"), getClass().getName() + "$FieldStorageAdvice");
    return result;
  }

  @Override
  public String[] helperClassNames() {
    return new String[] {packageName + ".StorageDecorator"};
  }

  @Override
  public Map<String, String> contextStore() {
    return singletonMap("java.net.HttpURLConnection", String.class.getName());
  }

  public static class ContextStorageAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static Scope start(final @Argument(0) String value) {
      return withScopedContext(DECORATE.attach(value));
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void stop(@Advice.Enter final Scope scope) {
      scope.close();
    }
  }

  public static class FieldStorageAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void start(
        @Argument(0) final HttpURLConnection httpURLConnection, final @Argument(1) String value) {

      InstrumentationContext.get(HttpURLConnection.class, String.class)
          .put(httpURLConnection, value);
    }
  }
}
