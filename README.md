# Dockerfile Usage

```dockerfile
docker buildx build -t springboot-app .
docker run -d --name springboot-instance3 --network framework_server_app-network -p 8080:8080 springboot-app
```

# docker-compose Usage

```bash
docker compose up -d
```
