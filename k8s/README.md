# Kubernetes deployment (Docker Desktop)

Run from the repo root in zsh. Image `lost-found:1.0` must exist locally (`docker build -t lost-found:1.0 .`).

The Service uses `sessionAffinity: ClientIP`: data is in memory per pod, so a client is pinned to one pod. Open `http://localhost:30080/`; the footer shows which pod served it.

## 1. Deploy
```
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl get pods
kubectl get svc
```

## 2. Test
```
curl http://localhost:30080/actuator/health
curl http://localhost:30080/items
```

## 3. Scale
```
kubectl scale deployment/lost-found --replicas=5
kubectl scale deployment/lost-found --replicas=3
```

## 4. Probes and ConfigMap
```
kubectl describe pod -l app=lost-found
kubectl get configmap lost-found-config -o yaml
```

## 5. Rollback demo
```
kubectl set image deployment/lost-found lost-found=lost-found:broken
kubectl get pods
kubectl rollout undo deployment/lost-found
kubectl rollout status deployment/lost-found
kubectl rollout history deployment/lost-found
```

## Cleanup
```
kubectl delete -f k8s/
```
