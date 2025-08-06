# Android Client API Usage Guide

This document explains how to integrate with the VPN backend APIs from an Android client application. The system consists of two main API services that both operate on port 443 through an Nginx Proxy Manager setup.

## Architecture Overview

### Network Configuration
- **User Support API**: Runs on port 8080 internally, exposed via Nginx proxy on port 443
- **WireGuard Server API**: Runs on port 443 internally, also proxied through Nginx on port 443
- **Domain Routing**: Both services are accessible through HTTPS (port 443) but routed to different containers based on domain/path configuration

```
Internet (443) → Nginx Proxy Manager → {
  ├── User Support API (port 8080)
  └── WireGuard Server API (port 443)
}
```

## 1. User Support API

### Base Configuration
- **Internal Port**: 8080
- **External Access**: HTTPS port 443 via proxy
- **Rate Limiting**: 5 requests per minute per IP
- **CORS**: Enabled for all origins

### Available Endpoints

#### Health Check
```http
GET /api/health
```

**Response:**
```json
{
  "status": "healthy",
  "message": "User Support API is running",
  "version": "1.0.0"
}
```

#### Support Email Signup
```http
POST /api/support
Content-Type: application/json

{
  "email": "user@example.com"
}
```

**Response (201 Created):**
```json
{
  "message": "Successfully signed up for project updates",
  "email": "user@example.com",
  "duplicate": false
}
```

#### Node Operator Interest
```http
POST /api/node-operator
Content-Type: application/json

{
  "email": "operator@example.com"
}
```

**Response (201 Created):**
```json
{
  "message": "Successfully registered interest in becoming a node operator",
  "email": "operator@example.com",
  "duplicate": false
}
```

#### Statistics
```http
GET /api/stats
```

**Response:**
```json
{
  "support_count": 150,
  "node_operator_count": 25,
  "total_signups": 175
}
```

### Android Implementation Example

```kotlin
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.call.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

class UserSupportApiClient(private val baseUrl: String) {
    
    private val client = HttpClient(Android) {
        // SSL configuration for port 443
        engine {
            connectTimeout = 30_000
            socketTimeout = 30_000
        }
    }
    
    @Serializable
    data class SupportRequest(val email: String)
    
    @Serializable
    data class SupportResponse(
        val message: String,
        val email: String,
        val duplicate: Boolean
    )
    
    suspend fun signupForSupport(email: String): Result<SupportResponse> {
        return try {
            val response = client.post("$baseUrl/api/support") {
                contentType(ContentType.Application.Json)
                setBody(SupportRequest(email))
            }
            Result.success(response.body<SupportResponse>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signupAsNodeOperator(email: String): Result<SupportResponse> {
        return try {
            val response = client.post("$baseUrl/api/node-operator") {
                contentType(ContentType.Application.Json)
                setBody(SupportRequest(email))
            }
            Result.success(response.body<SupportResponse>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun checkHealth(): Result<Boolean> {
        return try {
            val response = client.get("$baseUrl/api/health")
            Result.success(response.status == HttpStatusCode.OK)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## 2. WireGuard Server API

### Base Configuration
- **Port**: 443 (both internal and external)
- **Authentication**: Bearer token required
- **SSL**: Auto-generated certificates via Let's Encrypt

### API Authentication

All WireGuard API endpoints require Bearer token authentication:

```http
Authorization: Bearer your-api-key-here
```

The API key is auto-generated at startup and stored in `/config/server/api_key`.

### Available Endpoints

#### Server Health Check
```http
GET /health
```

**Response:**
```json
{
  "status": "healthy",
  "wireguard_status": "running",
  "server_public_key": "...",
  "peers_count": 5
}
```

#### Server Status (Authenticated)
```http
GET /api/server/status
Authorization: Bearer your-api-key
```

**Response:**
```json
{
  "server_public_key": "...",
  "listen_port": 51820,
  "peers_count": 5,
  "interface_address": "10.0.0.1/24"
}
```

#### Create New Peer (Authenticated)
```http
POST /api/peer/create
Authorization: Bearer your-api-key
Content-Type: application/json

{
  "name": "android-device-123"
}
```

**Response (201 Created):**
```json
{
  "peer_id": "uuid-here",
  "name": "android-device-123",
  "public_key": "peer-public-key",
  "allowed_ips": "10.0.0.2/32",
  "created_at": "2025-08-02T10:30:00Z"
}
```

#### Get Peer Configuration (Authenticated)
```http
GET /api/peer/{peer_id}/config
Authorization: Bearer your-api-key
```

**Response (File Download):**
```ini
[Interface]
PrivateKey = generated-private-key
Address = 10.0.0.2/32
DNS = 1.1.1.1,8.8.8.8

[Peer]
PublicKey = server-public-key
Endpoint = your-domain.com:51820
AllowedIPs = 0.0.0.0/0
PersistentKeepalive = 25
```

#### Delete Peer (Authenticated)
```http
DELETE /api/peer/{peer_id}
Authorization: Bearer your-api-key
```

**Response (204 No Content)**

#### List All Peers (Authenticated)
```http
GET /api/peers
Authorization: Bearer your-api-key
```

**Response:**
```json
{
  "peers": [
    {
      "peer_id": "uuid-1",
      "name": "android-device-123",
      "public_key": "...",
      "allowed_ips": "10.0.0.2/32",
      "created_at": "2025-08-02T10:30:00Z"
    }
  ],
  "total_count": 1
}
```

### Android Implementation Example

```kotlin
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.call.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

class WireGuardApiClient(
    private val baseUrl: String,
    private val apiKey: String
) {
    
    private val client = HttpClient(Android) {
        engine {
            connectTimeout = 30_000
            socketTimeout = 30_000
        }
    }
    
    @Serializable
    data class CreatePeerRequest(val name: String)
    
    @Serializable
    data class PeerResponse(
        val peer_id: String,
        val name: String,
        val public_key: String,
        val allowed_ips: String,
        val created_at: String
    )
    
    @Serializable
    data class ServerStatus(
        val server_public_key: String,
        val listen_port: Int,
        val peers_count: Int,
        val interface_address: String
    )
    
    suspend fun createPeer(deviceName: String): Result<PeerResponse> {
        return try {
            val response = client.post("$baseUrl/api/peer/create") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                setBody(CreatePeerRequest(deviceName))
            }
            Result.success(response.body<PeerResponse>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun downloadPeerConfig(peerId: String): Result<String> {
        return try {
            val response = client.get("$baseUrl/api/peer/$peerId/config") {
                header("Authorization", "Bearer $apiKey")
            }
            Result.success(response.body<String>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getServerStatus(): Result<ServerStatus> {
        return try {
            val response = client.get("$baseUrl/api/server/status") {
                header("Authorization", "Bearer $apiKey")
            }
            Result.success(response.body<ServerStatus>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deletePeer(peerId: String): Result<Boolean> {
        return try {
            val response = client.delete("$baseUrl/api/peer/$peerId") {
                header("Authorization", "Bearer $apiKey")
            }
            Result.success(response.status == HttpStatusCode.NoContent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## 3. Nginx Proxy Manager Setup

### Why Nginx Proxy Manager?

Both backend services need to be accessible via HTTPS on port 443, but they run on different internal ports:
- User Support API: Internal port 8080
- WireGuard Server API: Internal port 443

Nginx Proxy Manager provides:
- SSL termination with automatic Let's Encrypt certificates
- Domain-based routing to different backend services
- Centralized access management
- Load balancing capabilities

### Docker Network Configuration

Create a shared Docker network for service communication:

```bash
docker network create vpn-network
```

### Nginx Proxy Manager Docker Compose

```yaml
version: '3.8'

services:
  nginx-proxy-manager:
    image: 'jc21/nginx-proxy-manager:latest'
    container_name: nginx-proxy-manager
    restart: unless-stopped
    ports:
      - '80:80'     # HTTP (redirects to HTTPS)
      - '443:443'   # HTTPS (SSL termination)
      - '81:81'     # Admin interface
    volumes:
      - ./nginx-data:/data
      - ./nginx-letsencrypt:/etc/letsencrypt
    networks:
      - vpn-network

networks:
  vpn-network:
    external: true
```

### Backend Service Configuration

Update your existing docker-compose files to use the shared network:

#### User Support API
```yaml
# In /Backend/Usr-support/docker-compose.yml
version: '3.8'

services:
  user-support-api:
    build: .
    container_name: user-support-backend
    restart: unless-stopped
    # Remove external port mapping - access via proxy only
    # ports:
    #   - "8080:8080"
    volumes:
      - ./volumes/user_data:/app/data
    environment:
      - PORT=8080
      - PYTHONUNBUFFERED=1
    networks:
      - vpn-network

networks:
  vpn-network:
    external: true
```

#### WireGuard Server API  
```yaml
# In /Backend/Backend-server-template2/docker-compose.yml
services:
  wireguard-server:
    build: .
    container_name: wireguard-server
    restart: unless-stopped
    privileged: true
    # Keep UDP port for WireGuard, remove HTTPS port
    ports:
      - "51820:51820/udp"
      # Remove: - "443:443"
    volumes:
      - ./config:/config
      - ./ssl:/ssl
      - wireguard_data:/data
    environment:
      - VPN_DOMAIN=your-domain.com
      - SSL_EMAIL=admin@your-domain.com
      # ... other environment variables
    networks:
      - vpn-network

networks:
  vpn-network:
    external: true
```

### Proxy Host Configuration

In the Nginx Proxy Manager admin interface (port 81), create proxy hosts:

#### User Support API Proxy Host
- **Domain Names**: `support.your-domain.com`
- **Scheme**: `http`
- **Forward Hostname/IP**: `user-support-backend` (container name)
- **Forward Port**: `8080`
- **SSL**: Request new SSL certificate via Let's Encrypt

#### WireGuard Server API Proxy Host  
- **Domain Names**: `api.your-domain.com`
- **Scheme**: `https`
- **Forward Hostname/IP**: `wireguard-server` (container name)
- **Forward Port**: `443`
- **SSL**: Request new SSL certificate via Let's Encrypt

### Android Client Configuration

Update your Android client to use the proxied URLs:

```kotlin
class ApiClientManager {
    companion object {
        // Use different subdomains for different services
        private const val SUPPORT_API_BASE_URL = "https://support.your-domain.com"
        private const val WIREGUARD_API_BASE_URL = "https://api.your-domain.com"
    }
    
    val supportApi = UserSupportApiClient(SUPPORT_API_BASE_URL)
    val wireGuardApi = WireGuardApiClient(WIREGUARD_API_BASE_URL, getApiKey())
    
    private fun getApiKey(): String {
        // Retrieve stored API key from secure storage
        return SecurePreferences.getString("wireguard_api_key", "")
    }
}
```

## 4. Integration with Existing TORUS Architecture

### TorusVpnService Integration

Your existing `TorusVpnService.kt` can be enhanced to work with the new API structure:

```kotlin
class TorusVpnService {
    private val apiManager = ApiClientManager()
    
    suspend fun downloadServerConfig(serverId: String): Result<String> {
        return when {
            isTestServer(serverId) -> {
                // Use existing hardcoded test config
                Result.success(TorusServerConfig.hardcodedConfig)
            }
            else -> {
                // Use WireGuard API to create/download peer config
                val deviceName = "android_${getDeviceId()}"
                apiManager.wireGuardApi.createPeer(deviceName)
                    .mapCatching { peer ->
                        apiManager.wireGuardApi.downloadPeerConfig(peer.peer_id)
                            .getOrThrow()
                    }
            }
        }
    }
    
    suspend fun registerUserForUpdates(email: String): Result<Unit> {
        return apiManager.supportApi.signupForSupport(email)
            .map { Unit }
    }
}
```

### Configuration File Management

Update `FileUtils.kt` to handle API-downloaded configurations:

```kotlin
object FileUtils {
    suspend fun downloadAndCacheConfig(
        serverId: String,
        torusService: TorusVpnService
    ): Result<Uri> {
        return torusService.downloadServerConfig(serverId)
            .mapCatching { configContent ->
                val filename = "torus_${serverId}_${System.currentTimeMillis()}.conf"
                val file = File(getDownloadsDir(), filename)
                file.writeText(configContent)
                Uri.fromFile(file)
            }
    }
}
```

## 5. Error Handling and Security

### Common HTTP Error Codes

#### User Support API
- `400 Bad Request`: Invalid email format
- `429 Too Many Requests`: Rate limit exceeded (5/minute)
- `500 Internal Server Error`: Server-side issue

#### WireGuard Server API
- `401 Unauthorized`: Missing or invalid API key
- `404 Not Found`: Peer ID not found
- `500 Internal Server Error`: WireGuard configuration issue

### Security Considerations

1. **API Key Storage**: Store WireGuard API keys in Android Keystore
2. **Certificate Pinning**: Implement SSL certificate pinning for production
3. **Request Timeout**: Set appropriate timeouts for network requests
4. **Retry Logic**: Implement exponential backoff for failed requests

### Example Error Handling

```kotlin
suspend fun handleApiCall<T>(apiCall: suspend () -> Result<T>): Result<T> {
    return try {
        val result = apiCall()
        if (result.isFailure) {
            val exception = result.exceptionOrNull()
            when (exception) {
                is io.ktor.client.network.sockets.ConnectTimeoutException -> {
                    // Network timeout - maybe show retry option
                    Result.failure(NetworkException("Connection timeout"))
                }
                is io.ktor.client.plugins.ClientRequestException -> {
                    when (exception.response.status.value) {
                        401 -> Result.failure(AuthenticationException("Invalid API key"))
                        429 -> Result.failure(RateLimitException("Too many requests"))
                        else -> Result.failure(ApiException("Request failed: ${exception.message}"))
                    }
                }
                else -> result
            }
        } else {
            result
        }
    } catch (e: Exception) {
        Result.failure(NetworkException("Network error: ${e.message}"))
    }
}
```

## 6. Testing and Development

### Local Development Setup

For development, you can run services on different ports:

```kotlin
class ApiClientManager {
    companion object {
        private const val IS_DEBUG = BuildConfig.DEBUG
        
        private val SUPPORT_API_BASE_URL = if (IS_DEBUG) {
            "http://10.0.2.2:8080"  // Android emulator localhost
        } else {
            "https://support.your-domain.com"
        }
        
        private val WIREGUARD_API_BASE_URL = if (IS_DEBUG) {
            "https://10.0.2.2:443"
        } else {
            "https://api.your-domain.com"
        }
    }
}
```

### Testing Checklist

- [ ] User Support API health check responds
- [ ] Email signup works with valid emails
- [ ] Rate limiting prevents spam (5 requests/minute)
- [ ] WireGuard API authentication works
- [ ] Peer creation and config download successful
- [ ] SSL certificates are valid and trusted
- [ ] Network timeouts are handled gracefully
- [ ] Offline mode handles API failures appropriately

## 7. Backend SSL Configuration for Proxy Setup

### Important: WireGuard Server SSL Conflicts

Your WireGuard backend-server-template has extensive auto-SSL functionality that **MUST be disabled** when using Nginx Proxy Manager to avoid conflicts.

### Current Conflicting Configuration

The existing `Backend-server-template2/docker-compose.yml` has these problematic settings:

```yaml
ports:
  - "80:80"           # CONFLICTS with Nginx Proxy Manager
  - "443:443"         # CONFLICTS with Nginx Proxy Manager
  - "51820:51820/udp" # This is fine - VPN traffic

environment:
  - AUTO_SSL=1                           # MUST be disabled
  - SSL_EMAIL=admin@your-domain.com      # Not needed with proxy
  - SSL_STAGING=0                        # Not needed with proxy
```

### Required Configuration Changes

#### 1. WireGuard Server docker-compose.yml Modifications

```yaml
# Backend-server-template2/docker-compose.yml
services:
  wireguard-server:
    build: .
    container_name: wireguard-server
    restart: unless-stopped
    privileged: true
    sysctls:
      - net.ipv4.ip_forward=1
      - net.ipv4.conf.all.src_valid_mark=1
    cap_add:
      - NET_ADMIN
      - SYS_MODULE
    ports:
      # REMOVE these conflicting ports:
      # - "80:80"           # Remove - conflicts with Nginx
      # - "443:443"         # Remove - conflicts with Nginx
      - "51820:51820/udp" # Keep - VPN traffic only
    volumes:
      - ./config:/config
      - ./ssl:/ssl                # Optional - for manual SSL if needed
      - wireguard_data:/data
      - /lib/modules:/lib/modules:ro
    environment:
      # REQUIRED: Disable auto-SSL to prevent conflicts
      - AUTO_SSL=0                           # CRITICAL: Disable auto-SSL
      - SSL_ENABLED=false                    # Disable SSL in app code
      
      # Keep VPN domain for WireGuard config generation
      - VPN_DOMAIN=your-domain.com           # Used for peer configs
      
      # WireGuard Configuration (unchanged)
      - WG_INTERFACE_ADDRESS=10.0.0.1/24
      - WG_LISTEN_PORT=51820
      - WG_FORWARD_INTERFACE=eth+
      - SERVERURL=your-domain.com
      - SERVERPORT=51820
      - INTERNAL_SUBNET=10.0.0.0/24
      - PEERDNS=1.1.1.1,8.8.8.8
      - ALLOWEDIPS=0.0.0.0/0
      
      # System Configuration (unchanged)
      - PUID=1000
      - PGID=1000
      - TZ=UTC
      - INSTALL_DEPS=true
    
    # Add to shared network for Nginx access
    networks:
      - vpn-network

volumes:
  wireguard_data:
    driver: local

# Add shared network
networks:
  vpn-network:
    external: true
```

#### 2. User Support API docker-compose.yml Modifications

```yaml
# Usr-support/docker-compose.yml
version: '3.8'

services:
  user-support-api:
    build: .
    container_name: user-support-backend
    restart: unless-stopped
    # REMOVE external port mapping - access via proxy only
    # ports:
    #   - "8080:8080"
    volumes:
      - ./volumes/user_data:/app/data
    environment:
      - PORT=8080                    # Internal port only
      - PYTHONUNBUFFERED=1
    networks:
      - vpn-network                  # Add to shared network

# Add shared network
networks:
  vpn-network:
    external: true
```

#### 3. Create Shared Docker Network

Before starting any containers, create the shared network:

```bash
# Create the shared network first
docker network create vpn-network
```

#### 4. Nginx Proxy Manager Configuration

Create a separate docker-compose file for Nginx Proxy Manager:

```yaml
# nginx-proxy-manager/docker-compose.yml
version: '3.8'

services:
  nginx-proxy-manager:
    image: 'jc21/nginx-proxy-manager:latest'
    container_name: nginx-proxy-manager
    restart: unless-stopped
    ports:
      - '80:80'     # HTTP (Let's Encrypt challenges)
      - '443:443'   # HTTPS (SSL termination)
      - '81:81'     # Admin interface
    volumes:
      - ./nginx-data:/data
      - ./nginx-letsencrypt:/etc/letsencrypt
    networks:
      - vpn-network

networks:
  vpn-network:
    external: true
```

### Deployment Order

1. **Create network**: `docker network create vpn-network`
2. **Start Nginx Proxy Manager**: `cd nginx-proxy-manager && docker compose up -d`
3. **Start User Support API**: `cd Usr-support && docker compose up -d`
4. **Start WireGuard Server**: `cd Backend-server-template2 && docker compose up -d`

### Nginx Proxy Manager Setup

Access the admin interface at `http://your-server-ip:81` and create these proxy hosts:

#### User Support API Proxy Host
- **Domain Names**: `support.your-domain.com`
- **Scheme**: `http` (internal communication)
- **Forward Hostname/IP**: `user-support-backend`
- **Forward Port**: `8080`
- **SSL**: Request new SSL certificate

#### WireGuard Server API Proxy Host
- **Domain Names**: `api.your-domain.com`
- **Scheme**: `http` (internal communication, SSL terminated at proxy)
- **Forward Hostname/IP**: `wireguard-server`
- **Forward Port**: `80` (the app will run HTTP mode when AUTO_SSL=0)
- **SSL**: Request new SSL certificate

### Why This Configuration Works

1. **No Port Conflicts**: Only Nginx Proxy Manager binds to ports 80/443
2. **Single SSL Endpoint**: All certificate management handled by Nginx
3. **Internal HTTP**: Backend services communicate over HTTP within Docker network
4. **Domain-based Routing**: Different subdomains route to different services
5. **VPN Traffic Unaffected**: WireGuard UDP traffic (port 51820) bypasses proxy entirely

### Android Client Updates

Update your Android client to use the proxied endpoints:

```kotlin
class ApiClientManager {
    companion object {
        // Different subdomains for different services
        private const val SUPPORT_API_BASE_URL = "https://support.your-domain.com"
        private const val WIREGUARD_API_BASE_URL = "https://api.your-domain.com"
        
        // For development (if running services directly)
        private const val SUPPORT_API_DEV_URL = "http://10.0.2.2:8080"
        private const val WIREGUARD_API_DEV_URL = "http://10.0.2.2:80"  // Note: port 80 when SSL disabled
    }
    
    private val supportApiUrl = if (BuildConfig.DEBUG) SUPPORT_API_DEV_URL else SUPPORT_API_BASE_URL
    private val wireGuardApiUrl = if (BuildConfig.DEBUG) WIREGUARD_API_DEV_URL else WIREGUARD_API_BASE_URL
    
    val supportApi = UserSupportApiClient(supportApiUrl)
    val wireGuardApi = WireGuardApiClient(wireGuardApiUrl, getApiKey())
}
```

### Troubleshooting

#### Check Service Status
```bash
# Verify all containers are running
docker ps

# Check Nginx Proxy Manager logs
docker logs nginx-proxy-manager

# Check WireGuard server logs (should show SSL disabled)
docker logs wireguard-server | grep -i ssl

# Check User Support API logs
docker logs user-support-backend
```

#### Verify Network Connectivity
```bash
# Test internal connectivity between containers
docker exec nginx-proxy-manager nslookup user-support-backend
docker exec nginx-proxy-manager nslookup wireguard-server
```

#### SSL Certificate Issues
If SSL certificates aren't generating properly:
1. Ensure DNS records point to your server
2. Check Nginx Proxy Manager logs for Let's Encrypt errors
3. Verify port 80 is accessible from the internet for ACME challenges

## Conclusion

This setup provides a robust, scalable architecture for your Android VPN client to communicate with both the User Support API and WireGuard Server API through a single HTTPS endpoint (port 443) while maintaining proper SSL security and service isolation.

The Nginx Proxy Manager handles the complexity of routing requests to the appropriate backend services, while your Android client uses a clean, type-safe API interface built with Ktor that integrates seamlessly with your existing TORUS VPN architecture.

**Critical Success Factors:**
1. Disable AUTO_SSL in WireGuard server to prevent conflicts
2. Remove port 80/443 mappings from backend services
3. Use shared Docker network for service communication
4. Configure domain-based routing in Nginx Proxy Manager
5. Update Android client to use proxied endpoints