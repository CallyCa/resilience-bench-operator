package io.resiliencebench.resources;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.resiliencebench.resources.benchmark.Benchmark;
import io.resiliencebench.resources.benchmark.Source;
import io.resiliencebench.resources.benchmark.Target;
import io.resiliencebench.resources.scenario.Scenario;
import io.resiliencebench.resources.scenario.ScenarioFaultTemplate;
import io.resiliencebench.resources.scenario.ScenarioSpec;
import io.resiliencebench.resources.scenario.ScenarioWorkload;
import io.resiliencebench.resources.workload.Workload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.resiliencebench.support.Annotations.OWNED_BY;

public final class ScenarioFactory {

  public ScenarioFactory() {
    throw new IllegalStateException("Utility class");
  }

  private static Map<String, Object> convertJsonNodeToMap(JsonNode jsonNode) {
    Map<String, Object> resultMap = new HashMap<>();

    if (jsonNode != null && jsonNode.isObject()) {
      jsonNode.fields().forEachRemaining(entry -> resultMap.put(entry.getKey(), convertJsonNode(entry.getValue())));
    }
    return resultMap;
  }

  public static Object convertJsonNode(JsonNode jsonNode) {
    if (jsonNode.isObject()) {
      return convertJsonNodeToMap(jsonNode);
    } else if (jsonNode.isArray()) {
      List<Object> list = new ArrayList<>();
      jsonNode.elements().forEachRemaining(element -> list.add(convertJsonNode(element)));
      return list;
    } else if (jsonNode.isTextual()) {
      return jsonNode.textValue();
    } else if (jsonNode.isBoolean()) {
      return jsonNode.booleanValue();
    } else if (jsonNode.isNumber()) {
      if (jsonNode.isDouble() || jsonNode.isFloatingPointNumber()) {
        return jsonNode.doubleValue();
      } else {
        return jsonNode.longValue();

      }
    } else if (jsonNode.isNull()) {
      return null;
    }

    return jsonNode;
  }

  public static List<Map<String, Object>> expandServiceParameters(Source serviceSource) {
    return ListExpansion.expandConfigTemplate(serviceSource.getPatternConfig());
  }

  public static List<Scenario> create(Benchmark benchmark, Workload workload) {
    List<Scenario> scenarios = new ArrayList<>();
    var workloadUsers = workload.getSpec().getUsers();
    var workloadName = workload.getMetadata().getName();

    for (var connection : benchmark.getSpec().getConnections()) {
      var target = connection.target();
      var source = connection.source();

      for (var faultPercentage : target.getFault().getPercentage()) {
        var fault =
                ScenarioFaultTemplate.create(faultPercentage, target.getFault().getDelay(), target.getFault().getAbort());

        for (var sourcePatternsParameters : expandServiceParameters(source)) {
          for (var workloadUser : workloadUsers) {
            var scenario = createScenario(workloadName, target, source, fault, sourcePatternsParameters, workloadUser);
            scenario.setMetadata(createMeta(scenario, benchmark));
            scenarios.add(scenario);
          }
        }
      }
    }
    return scenarios;
  }

  private static Scenario createScenario(
          String workloadName,
          Target target,
          Source source,
          ScenarioFaultTemplate fault,
          Map<String, Object> sourcePatternsParameters, Integer workloadUser
  ) {
    return new Scenario(
            new ScenarioSpec(
                    target.getService(),
                    source.getService(),
                    sourcePatternsParameters,
                    new ScenarioWorkload(workloadName, workloadUser),
                    fault
            )
    );
  }

  private static ObjectMeta createMeta(Scenario scenario, Benchmark benchmark) {
    return new ObjectMetaBuilder()
            .withName(scenario.toString())
            .withNamespace(benchmark.getMetadata().getNamespace())
            .addToAnnotations(OWNED_BY, benchmark.getMetadata().getName())
            .addToAnnotations("resiliencebench.io/scenario-uid", scenario.toString())
            .build();
  }
}