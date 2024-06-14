package io.resiliencebench.resources.workload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.fabric8.generator.annotation.Min;

import java.util.ArrayList;
import java.util.List;

public class WorkloadSpec {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Integer> users = new ArrayList<>();
  @Min(1)
  @JsonPropertyDescription("Workload duration in seconds")
  private int duration;

  private CloudConfig cloud;

  private ScriptConfig script;

  public WorkloadSpec() {
  }

  public WorkloadSpec(List<Integer> users,
                      int duration,
                      String targetUrl,
                      CloudConfig cloud,
                      ScriptConfig script) {
    this.users = users;
    this.duration = duration;
    this.script = script;
  }

  public List<Integer> getUsers() {
    return users;
  }

  public int getDuration() {
    return duration;
  }

  public CloudConfig getCloud() {
    return cloud;
  }


  public ScriptConfig getScript() {
    return script;
  }
}
