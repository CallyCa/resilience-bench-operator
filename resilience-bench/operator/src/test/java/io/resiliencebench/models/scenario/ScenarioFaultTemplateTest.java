package io.resiliencebench.models.scenario;

import io.resiliencebench.models.fault.AbortFault;
import io.resiliencebench.models.fault.DelayFault;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ScenarioFaultTemplateTest {

  @Test
  public void fromWithDelay() {
    var fault = ScenarioFaultTemplate.create(10, new DelayFault(1000), null);
    assertEquals(10, fault.getPercentage());
    assertEquals(new DelayFault(1000), fault.getDelay());
    assertNull(fault.getAbort());
  }

  @Test
  public void fromWithoutDelay() {
    var fault = ScenarioFaultTemplate.create(10, null, null);
    assertNull(fault);
  }

  @Test
  public void fromWithAbort() {
    var fault = ScenarioFaultTemplate.create(10, null, new AbortFault(500));
    assertEquals(10, fault.getPercentage());
    assertEquals(new AbortFault(500), fault.getAbort());
    assertNull(fault.getDelay());
  }

  @Test
  public void fromWithoutAny() {
    var fault = ScenarioFaultTemplate.create(10, null, null);
    assertNull(fault);
  }
}