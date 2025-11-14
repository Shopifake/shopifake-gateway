# Shopifake Gateway

The API Gateway for the Shopifake microservices architecture. Routes requests to backend services using Istio Gateway and VirtualService resources.

## Features

- **Istio Gateway** for ingress traffic management
- **VirtualService** for intelligent routing to backend services
- **Path-based routing** to multiple microservices
- **Helm chart** for easy deployment and configuration
- **Namespace-agnostic** configuration

## Architecture

The gateway uses Istio's Gateway and VirtualService resources to route incoming traffic to the appropriate backend services:

- **Gateway**: Exposes the service mesh to external traffic via the Istio ingress gateway
- **VirtualService**: Defines routing rules that match request paths and forward them to the correct backend service

## Quick Start

### 1. Prerequisites

- Kubernetes cluster with Istio installed
- Istio ingress gateway running with the appropriate label selector
- Helm 3.x installed

### 2. Configure Routes

Edit `shopifake-gateway-chart/values.yaml` to configure your routes:

```yaml
gateway:
  enabled: true
  host: "*"  # or specific hostname
  port:
    number: 80
    name: http
    protocol: HTTP
  selector:
    istio: ingress  # matches your Istio ingress gateway pod labels
  routes:
    - name: shopifake-access
      match:
        - uri:
            prefix: /access
      rewrite:
        uri: /
      destination:
        host: shopifake-access
        port: 8080
    # ... more routes
```

### 3. Deploy

```bash
helm install istio-ingress istio/gateway -n shopifake-sandbox --wait

helm install shopifake-gateway ./shopifake-gateway-chart \
  --namespace <your-namespace>
```

### 4. Verify

```bash
# Check Gateway
kubectl get gateway -n <your-namespace> 

# Check VirtualService
kubectl get virtualservice -n <your-namespace>

# Test routing
curl http://<istio-ingress-ip>/access/...
```

## Configuration

### Gateway Configuration

The gateway is configured in `shopifake-gateway-chart/values.yaml`:

```yaml
gateway:
  enabled: true
  host: "*"                    # Accept all hosts (or specific hostname)
  port:
    number: 80                  # Port exposed by Istio ingress
    name: http
    protocol: HTTP
  selector:
    istio: ingress              # Must match your Istio ingress gateway pod labels
  routes:
    - name: service-name
      match:
        - uri:
            prefix: /path       # Path prefix to match
      rewrite:
        uri: /                  # Rewrite path before forwarding
      destination:
        host: service-name      # Kubernetes service name
        port: 8080              # Service port
```

### Route Configuration

Each route defines:
- **match**: URI patterns to match (e.g., `/access`, `/catalog`)
- **rewrite**: Optional URI rewriting (strips prefix before forwarding)
- **destination**: Target service and port

### Default Route

Unmatched requests can optionally be routed to a gateway service by enabling `gateway.defaultRoute.enabled` in values.yaml. By default, unmatched requests will return 404.

## Service Routing

The gateway routes requests to the following services:

- `/access` → `shopifake-access:8080`
- `/audit` → `shopifake-audit:8080`
- `/catalog` → `shopifake-catalog:8080`
- `/chatbot` → `shopifake-chatbot:8080`
- `/customers` → `shopifake-customers:8080`
- `/inventory` → `shopifake-inventory:8080`
- `/orders` → `shopifake-orders:8080`
- `/pricing` → `shopifake-pricing:8080`
- `/recommender` → `shopifake-recommender:8080`
- `/sales-dashboard` → `shopifake-sales-dashboard:8080`
- `/sites` → `shopifake-sites:8080`

## Requirements

### Backend Services

All backend services must:
- Be deployed in the same namespace (or use FQDN format: `service.namespace.svc.cluster.local`)
- Expose port 8080 (or update the port in route configuration)
- Have matching Kubernetes service names

### Istio Setup

- Istio must be installed in your cluster
- Istio ingress gateway must be running
- The gateway selector must match your ingress gateway pod labels (default: `istio: ingress`)

## Advanced Configuration

### A/B Testing

Enable A/B testing by configuring traffic splitting:

```yaml
abTesting:
  enabled: true
  v1Weight: 80
  v2Weight: 20
```

This requires a DestinationRule with subsets (automatically created when enabled).

### Custom Selectors

If your Istio ingress gateway uses different labels:

```yaml
gateway:
  selector:
    istio: ingressgateway  # or your custom labels
```

### Cross-Namespace Routing

To route to services in different namespaces, use FQDN format:

```yaml
destination:
  host: shopifake-access.other-namespace.svc.cluster.local
  port: 8080
```

## Troubleshooting

### Routes Not Working

1. Verify Istio Gateway is created:
   ```bash
   kubectl get gateway -n <namespace>
   ```

2. Check VirtualService configuration:
   ```bash
   kubectl get virtualservice -n <namespace> -o yaml
   ```

3. Verify backend services exist:
   ```bash
   kubectl get svc -n <namespace>
   ```

4. Check Istio ingress gateway:
   ```bash
   kubectl get pods -n istio-system -l istio=ingress
   ```

### Port Mismatch

Ensure the destination port in routes matches your service ports:
```yaml
destination:
  port: 8080  # Must match service.spec.ports[].port
```

## Project Structure

```
shopifake-gateway/
├── shopifake-gateway-chart/
│   ├── Chart.yaml
│   ├── values.yaml          # Main configuration
│   └── templates/
│       ├── gateway.yaml     # Istio Gateway resource
│       ├── virtualservice.yaml  # Istio VirtualService resource
│       ├── destinationrule.yaml # Optional: for A/B testing
│       └── ...
└── README.md
```

## References

- [Istio Gateway Documentation](https://istio.io/latest/docs/reference/config/networking/gateway/)
- [Istio VirtualService Documentation](https://istio.io/latest/docs/reference/config/networking/virtual-service/)
