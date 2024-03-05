package io.resiliencebench.execution.istio.steps;

import io.fabric8.istio.api.networking.v1beta1.VirtualService;
import io.fabric8.istio.client.IstioClient;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.resiliencebench.execution.ExecutorStep;
import io.resiliencebench.resources.scenario.Scenario;
import io.resiliencebench.resources.service.ResilientService;
import io.resiliencebench.support.CustomResourceRepository;

import static java.lang.String.format;

public abstract class IstioExecutorStep<TResult extends HasMetadata> extends ExecutorStep<TResult> {

  private final IstioClient istioClient;

  public IstioExecutorStep(KubernetesClient kubernetesClient, IstioClient istioClient) {
    super(kubernetesClient);
    this.istioClient = istioClient;
  }

  protected IstioClient istioClient() {
    return istioClient;
  }

  protected VirtualService findVirtualService(String namespace, String name) {
    var serviceRepository = new CustomResourceRepository<>(kubernetesClient(), ResilientService.class);
    var targetService = serviceRepository.get(namespace, name);

    if (targetService.isPresent()) {
      var virtualServiceName = targetService.get().getMetadata().getAnnotations().get("resiliencebench.io/virtual-service");
      return istioClient
              .v1beta1()
              .virtualServices()
              .inNamespace(namespace)
              .withName(virtualServiceName)
              .get();
    } else {
      throw new RuntimeException(format("Service not found: %s.%s", namespace, name));
    }
  }

  @Override
  public abstract TResult execute(Scenario scenario);
}