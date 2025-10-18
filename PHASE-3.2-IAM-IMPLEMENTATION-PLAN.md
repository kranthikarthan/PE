# Phase 3.2 IAM Service - Implementation Plan 🚀

**Date**: October 18, 2025  
**Architecture**: Azure AD B2C + Spring Boot OAuth2 Resource Server  
**Timeline**: 2 days to production  
**Status**: READY TO BUILD ✅

---

## 📋 PHASE 3.2 SEQUENCING

### **DAY 1: Azure AD B2C Setup + Spring Boot OAuth2 Configuration**

#### Step 1.1: Azure AD B2C Tenant Configuration (30 mins)
- [ ] Create Azure AD B2C tenant
- [ ] Register application (Payments Engine API)
- [ ] Create 2 user flows: "Sign-up and Sign-in" + "Password reset"
- [ ] Configure MFA (SMS + TOTP)
- [ ] Get tenant ID, client ID, issuer URI
- [ ] Export JWKS endpoint (for token validation)

#### Step 1.2: Spring Boot OAuth2 Resource Server (2 hours)
- [ ] Create `iam-service` microservice (Maven)
- [ ] Add Spring Security + OAuth2 Resource Server
- [ ] Configure JWT validation (from B2C)
- [ ] Create `@Configuration` for OAuth2 properties
- [ ] Implement `JwtAuthenticationConverter` (extract claims → SecurityContext)
- [ ] Test endpoint: `/health` (no auth required)

#### Step 1.3: Integration Testing (1 hour)
- [ ] Get JWT token from Azure AD B2C
- [ ] Call `/api/auth/validate` endpoint
- [ ] Verify token claims extracted correctly
- [ ] Test expiry (should fail on expired token)

**DAY 1 Deliverable**: ✅ Spring Boot app validates JWT from Azure AD B2C

---

### **DAY 2: Role Mapping, RBAC, Audit Logging**

#### Step 2.1: Role Mapping Service (1.5 hours)
- [ ] Create `RoleEntity` (user_id, tenant_id, role, created_at)
- [ ] Create `RoleRepository` (find roles by user + tenant)
- [ ] Create `RoleService` (assign, revoke, list roles)
- [ ] Create `RoleController` (GET /users/{id}/roles)

#### Step 2.2: RBAC Enforcement (1 hour)
- [ ] Create `@RoleRequired(roles = {"bank_admin"})` annotation
- [ ] Implement `RoleRequiredAspect` (check role before execution)
- [ ] Test: endpoint restricted to specific roles
- [ ] Test: audit logs permission checks

#### Step 2.3: Audit Logging (1 hour)
- [ ] Create `AuditEventEntity` (who, what, when, result, tenant_id)
- [ ] Create `AuditLogService` (log all access attempts)
- [ ] Log: Token validation, role checks, failures
- [ ] Create `GET /audit/logs` (admin only)

#### Step 2.4: Multi-Tenancy Enforcement (1 hour)
- [ ] Extract `X-Tenant-ID` header
- [ ] Validate tenant matches JWT claim
- [ ] Propagate tenant context to all DB queries (RLS)
- [ ] Test: User from Tenant A cannot access Tenant B data

**DAY 2 Deliverable**: ✅ Full RBAC + audit logging + multi-tenancy

---

## 🏗️ DETAILED SERVICE ARCHITECTURE

### **Project Structure**

```
iam-service/
├── pom.xml                              (Dependencies)
├── src/main/java/com/payments/iam/
│   ├── IamServiceApplication.java       (Main entry point)
│   ├── config/
│   │   ├── SecurityConfig.java          (OAuth2 + JWT)
│   │   ├── OAuth2Properties.java        (B2C config)
│   │   └── TenantContext.java           (Multi-tenancy)
│   ├── domain/
│   │   ├── User.java                    (From JWT token)
│   │   ├── Role.java                    (JPA entity)
│   │   ├── Permission.java              (Role → Permissions)
│   │   └── AuditEvent.java              (Audit log)
│   ├── repository/
│   │   ├── RoleRepository.java          (JPA)
│   │   ├── PermissionRepository.java    (JPA)
│   │   └── AuditEventRepository.java    (JPA)
│   ├── service/
│   │   ├── TokenValidationService.java  (JWT validation)
│   │   ├── RoleService.java             (Role management)
│   │   ├── PermissionService.java       (Permission checks)
│   │   └── AuditService.java            (Audit logging)
│   ├── controller/
│   │   ├── AuthController.java          (Token validation)
│   │   ├── RoleController.java          (Role management)
│   │   └── AuditController.java         (Audit queries)
│   ├── aspect/
│   │   ├── RoleRequiredAspect.java      (RBAC via AOP)
│   │   └── AuditingAspect.java          (Auto-logging)
│   ├── dto/
│   │   ├── TokenValidationRequest.java
│   │   ├── TokenValidationResponse.java
│   │   ├── RoleAssignmentRequest.java
│   │   └── AuditLogResponse.java
│   ├── exception/
│   │   ├── UnauthorizedException.java
│   │   ├── ForbiddenException.java
│   │   └── IamExceptionHandler.java     (Global @ControllerAdvice)
│   └── security/
│       └── JwtAuthenticationConverter.java
├── src/main/resources/
│   ├── application.yml                  (OAuth2 config)
│   └── db/migration/
│       └── V7__Create_iam_tables.sql    (Already exists)
└── README.md
```

---

## 🔐 SECURITY ARCHITECTURE

### **Token Flow**

```
1. CLIENT LOGIN
   Client → POST /login (username/password) → Azure AD B2C
   
2. B2C RETURNS JWT
   Azure AD B2C → JWT token (15-min expiry)
               → Refresh token (7-day expiry)
               → (Contains: sub, tenant_id, email, roles)

3. CLIENT CALLS API
   Client → GET /api/payments with Authorization: Bearer <JWT>
   
4. SPRING SECURITY VALIDATES
   Spring → GET https://b2c.../discovery/v2.0/keys
        → Validate JWT signature (RSA-256)
        → Extract claims → SecurityContext
        
5. SPRING CALLS SERVICE
   @RequestMapping("/api/payments")
   @RoleRequired(roles = {"customer", "bank_operator"})  ← RBAC check here
   public PaymentResponse getPayment() {
       // Audit logged automatically
       // Tenant context enforced via RLS
   }
```

---

## 📊 DATABASE SCHEMA

### **V7__Create_iam_tables.sql** (Already exists, reference)

```sql
-- Roles (static)
CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- User-Role Mapping (multi-tenant)
CREATE TABLE user_roles (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    tenant_id UUID NOT NULL,
    role_id UUID NOT NULL REFERENCES roles(id),
    created_at TIMESTAMP DEFAULT NOW(),
    created_by VARCHAR(255),
    UNIQUE(user_id, tenant_id, role_id),
    FOREIGN KEY(tenant_id) REFERENCES tenants(id)
);

-- Role-Permission Mapping
CREATE TABLE role_permissions (
    id UUID PRIMARY KEY,
    role_id UUID NOT NULL REFERENCES roles(id),
    permission VARCHAR(100) NOT NULL,  -- payment:create, user:delete
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(role_id, permission)
);

-- Audit Log
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(100),
    resource_id UUID,
    result VARCHAR(20),  -- SUCCESS, DENIED, ERROR
    details TEXT,
    timestamp TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY(tenant_id) REFERENCES tenants(id),
    INDEX idx_tenant_timestamp (tenant_id, timestamp),
    INDEX idx_user_timestamp (user_id, timestamp)
);
```

---

## 🎯 IMPLEMENTATION STEPS (In Sequence)

### **STEP 1: Create IAM Service Project**

```bash
mkdir -p iam-service/src/main/java/com/payments/iam
mkdir -p iam-service/src/main/resources
```

### **STEP 2: pom.xml Dependencies**

```xml
<!-- Spring Boot OAuth2 Resource Server -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<!-- JWT Token Processing -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
```

### **STEP 3: application.yml Configuration**

```yaml
spring:
  application:
    name: iam-service
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://<tenant>.b2clogin.com/<tenant>.onmicrosoft.com/v2.0/
          jwk-set-uri: https://<tenant>.b2clogin.com/<tenant>.onmicrosoft.com/discovery/v2.0/keys
          audiences: <application-client-id>
```

### **STEP 4: SecurityConfig.java**

```java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/health", "/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        return new JwtAuthenticationConverter();  // Extract claims → SecurityContext
    }
}
```

### **STEP 5: RoleService.java**

```java
@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuditService auditService;

    @Transactional
    public void assignRole(String userId, UUID tenantId, String roleName) {
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new NotFoundException("Role not found: " + roleName));
        
        UserRole userRole = UserRole.builder()
            .userId(userId)
            .tenantId(tenantId)
            .role(role)
            .createdBy(getCurrentUser())
            .build();
        
        userRoleRepository.save(userRole);
        
        auditService.log(AuditEvent.builder()
            .tenantId(tenantId)
            .userId(userId)
            .action("ROLE_ASSIGNED")
            .resource("user_role")
            .result("SUCCESS")
            .details("Role " + roleName + " assigned to user " + userId)
            .build());
    }

    public List<String> getUserRoles(String userId, UUID tenantId) {
        return userRoleRepository.findByUserIdAndTenantId(userId, tenantId)
            .stream()
            .map(ur -> ur.getRole().getName())
            .collect(Collectors.toList());
    }

    public boolean hasRole(String userId, UUID tenantId, String roleName) {
        return userRoleRepository
            .existsByUserIdAndTenantIdAndRoleName(userId, tenantId, roleName);
    }
}
```

### **STEP 6: RoleRequiredAspect.java**

```java
@Aspect
@Component
@RequiredArgsConstructor
public class RoleRequiredAspect {
    private final RoleService roleService;
    private final AuditService auditService;

    @Before("@annotation(roleRequired)")
    public void checkRole(JoinPoint joinPoint, RoleRequired roleRequired) {
        String userId = getCurrentUserId();
        UUID tenantId = getCurrentTenantId();
        
        boolean hasRole = Arrays.stream(roleRequired.roles())
            .anyMatch(role -> roleService.hasRole(userId, tenantId, role));
        
        if (!hasRole) {
            auditService.log(AuditEvent.builder()
                .tenantId(tenantId)
                .userId(userId)
                .action("ACCESS_DENIED")
                .resource(joinPoint.getSignature().getDeclaringTypeName())
                .result("DENIED")
                .details("Required roles: " + String.join(",", roleRequired.roles()))
                .build());
            
            throw new ForbiddenException("Access denied. Required roles: " + 
                String.join(",", roleRequired.roles()));
        }
    }
}
```

### **STEP 7: AuthController.java**

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "IAM Authentication")
public class AuthController {

    @GetMapping("/validate")
    @Operation(summary = "Validate current JWT token")
    public ResponseEntity<TokenValidationResponse> validate() {
        String userId = getCurrentUserId();
        UUID tenantId = getCurrentTenantId();
        List<String> roles = roleService.getUserRoles(userId, tenantId);
        
        return ResponseEntity.ok(TokenValidationResponse.builder()
            .userId(userId)
            .tenantId(tenantId)
            .roles(roles)
            .valid(true)
            .expiresAt(getTokenExpiry())
            .build());
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserProfileResponse> getCurrentUser() {
        // Return JWT claims + roles
    }
}
```

### **STEP 8: AuditController.java**

```java
@RestController
@RequestMapping("/api/audit")
@RoleRequired(roles = {"bank_admin", "support_agent"})
public class AuditController {

    @GetMapping("/logs")
    public Page<AuditLogResponse> getAuditLogs(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        UUID tenantId = getCurrentTenantId();
        return auditService.getLogsByTenant(tenantId, PageRequest.of(page, size));
    }
}
```

---

## 🧪 TESTING CHECKLIST

### **Unit Tests**
- [ ] RoleService (assign, revoke, check roles)
- [ ] TokenValidationService (valid/expired/invalid tokens)
- [ ] AuditService (log creation, queries)

### **Integration Tests**
- [ ] End-to-end: JWT from Azure AD B2C → Token validation → Role check → Audit log
- [ ] Multi-tenancy: User from Tenant A cannot access Tenant B data
- [ ] RBAC: Endpoint restricted to specific roles
- [ ] Security: Expired token returns 401

### **Manual Testing**
- [ ] Get JWT from Azure AD B2C portal
- [ ] Call `/api/auth/validate` with valid token → 200 OK
- [ ] Call `/api/auth/validate` with expired token → 401 Unauthorized
- [ ] Call `/api/audit/logs` without bank_admin role → 403 Forbidden

---

## 📊 ROLLOUT PLAN

### **Production Deployment**

1. **Build & Package**: `mvn clean package`
2. **Docker Image**: Create Dockerfile (similar to tenant-management-service)
3. **Kubernetes Deploy**: `kubectl apply -f iam-service-k8s.yaml`
4. **Config**: Pass Azure AD B2C credentials via ConfigMap + Secret
5. **Istio**: Register service in mesh (mTLS enabled)
6. **Health Checks**: Liveness/readiness probes

---

## 🔄 INTEGRATION WITH PHASE 3.1 (TENANT SERVICE)

### **Tenant Service now validates access via IAM Service**

```
User → API Gateway → Tenant Service

Tenant Service receives: X-User-ID, X-Tenant-ID, Authorization: Bearer <JWT>

Before returning tenant data:
1. Call IAM Service: POST /api/auth/validate
2. Check if user has 'bank_admin' role in tenant
3. If yes: Return tenant data (RLS enforced)
4. If no: Return 403 Forbidden
```

---

## ✅ DELIVERABLES BY END OF DAY 2

✅ **Azure AD B2C Tenant** (Configured, MFA ready)  
✅ **iam-service** (Spring Boot OAuth2 Resource Server)  
✅ **RoleService** (Assign, revoke, check roles)  
✅ **RBAC Enforcement** (@RoleRequired annotation)  
✅ **Audit Logging** (Every access tracked)  
✅ **Multi-Tenancy** (X-Tenant-ID validation)  
✅ **Controllers** (Auth, Role, Audit)  
✅ **Integration Tests** (80%+ coverage)  
✅ **Documentation** (README + Swagger)  

---

## 🚀 READY TO BUILD

**Start with**: Create `iam-service/pom.xml` and project structure

**Proceed with**: SecurityConfig + OAuth2 properties

**Test with**: Azure AD B2C JWT token

---

**Status**: IMPLEMENTATION PLAN COMPLETE ✅  
**Next Action**: "move to next step" to begin building
