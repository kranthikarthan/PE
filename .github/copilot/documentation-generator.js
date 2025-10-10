/**
 * GitHub Copilot Integration for Payment Engine
 * Automated documentation generation and code analysis
 */

const fs = require('fs');
const path = require('path');

class PaymentEngineDocumentationGenerator {
    constructor() {
        this.projectRoot = process.cwd();
        this.documentationPath = path.join(this.projectRoot, 'documentation');
        this.apiEndpoints = [];
        this.serviceComponents = [];
    }

    /**
     * Generate comprehensive API documentation using Copilot
     */
    async generateApiDocumentation() {
        console.log('🤖 GitHub Copilot: Generating API documentation...');
        
        // Scan for REST controllers
        const controllers = this.scanForControllers();
        
        // Generate OpenAPI specification
        const openApiSpec = this.generateOpenApiSpec(controllers);
        
        // Generate SDK examples
        const sdkExamples = this.generateSdkExamples();
        
        // Generate integration guides
        const integrationGuides = this.generateIntegrationGuides();
        
        return {
            openApiSpec,
            sdkExamples,
            integrationGuides
        };
    }

    /**
     * Scan project for REST controllers and extract endpoints
     */
    scanForControllers() {
        const controllers = [];
        const controllerPaths = [
            'services/core-banking/src/main/java/com/paymentengine/corebanking/controller',
            'services/payment-processing/src/main/java/com/paymentengine/payment-processing/controller',
            'services/api-gateway/src/main/java/com/paymentengine/gateway/controller'
        ];

        controllerPaths.forEach(controllerPath => {
            const fullPath = path.join(this.projectRoot, controllerPath);
            if (fs.existsSync(fullPath)) {
                const files = fs.readdirSync(fullPath);
                files.forEach(file => {
                    if (file.endsWith('.java')) {
                        const content = fs.readFileSync(path.join(fullPath, file), 'utf8');
                        const endpoints = this.extractEndpoints(content, file);
                        controllers.push(...endpoints);
                    }
                });
            }
        });

        return controllers;
    }

    /**
     * Extract API endpoints from controller source code
     */
    extractEndpoints(content, filename) {
        const endpoints = [];
        
        // Extract class-level RequestMapping
        const classMapping = content.match(/@RequestMapping\("([^"]+)"\)/);
        const basePath = classMapping ? classMapping[1] : '';
        
        // Extract method-level mappings
        const methodRegex = /@(Get|Post|Put|Delete|Patch)Mapping\("?([^"]*)"?\)/g;
        let match;
        
        while ((match = methodRegex.exec(content)) !== null) {
            const httpMethod = match[1].toUpperCase();
            const methodPath = match[2] || '';
            const fullPath = basePath + methodPath;
            
            // Extract method name
            const methodNameMatch = content.substring(match.index).match(/public\s+\w+\s+(\w+)\s*\(/);
            const methodName = methodNameMatch ? methodNameMatch[1] : 'unknown';
            
            endpoints.push({
                path: fullPath,
                method: httpMethod,
                methodName: methodName,
                controller: filename.replace('.java', ''),
                description: this.extractMethodDescription(content, match.index)
            });
        }
        
        return endpoints;
    }

    /**
     * Extract method description from JavaDoc comments
     */
    extractMethodDescription(content, methodIndex) {
        const beforeMethod = content.substring(0, methodIndex);
        const lines = beforeMethod.split('\n').reverse();
        
        let description = '';
        let inJavaDoc = false;
        
        for (const line of lines) {
            const trimmedLine = line.trim();
            
            if (trimmedLine.endsWith('*/')) {
                inJavaDoc = true;
                continue;
            }
            
            if (trimmedLine.startsWith('/**')) {
                break;
            }
            
            if (inJavaDoc && trimmedLine.startsWith('*')) {
                const comment = trimmedLine.substring(1).trim();
                if (comment && !comment.startsWith('@')) {
                    description = comment + ' ' + description;
                }
            }
        }
        
        return description.trim() || 'No description available';
    }

    /**
     * Generate OpenAPI 3.0 specification
     */
    generateOpenApiSpec(endpoints) {
        const spec = {
            openapi: '3.0.0',
            info: {
                title: 'Payment Engine API',
                description: 'Enterprise Payment Engine REST API',
                version: '1.0.0',
                contact: {
                    name: 'Payment Engine Team',
                    email: 'api-support@payment-engine.com',
                    url: 'https://docs.payment-engine.com'
                },
                license: {
                    name: 'Enterprise License',
                    url: 'https://payment-engine.com/license'
                }
            },
            servers: [
                {
                    url: 'https://api.payment-engine.com',
                    description: 'Production server'
                },
                {
                    url: 'https://staging-api.payment-engine.com',
                    description: 'Staging server'
                },
                {
                    url: 'http://localhost:8080',
                    description: 'Development server'
                }
            ],
            paths: {},
            components: {
                securitySchemes: {
                    BearerAuth: {
                        type: 'http',
                        scheme: 'bearer',
                        bearerFormat: 'JWT'
                    },
                    ApiKeyAuth: {
                        type: 'apiKey',
                        in: 'header',
                        name: 'X-API-Key'
                    }
                },
                schemas: this.generateSchemas()
            },
            security: [
                { BearerAuth: [] },
                { ApiKeyAuth: [] }
            ]
        };

        // Convert endpoints to OpenAPI paths
        endpoints.forEach(endpoint => {
            if (!spec.paths[endpoint.path]) {
                spec.paths[endpoint.path] = {};
            }
            
            spec.paths[endpoint.path][endpoint.method.toLowerCase()] = {
                summary: endpoint.methodName,
                description: endpoint.description,
                operationId: endpoint.methodName,
                tags: [endpoint.controller],
                responses: {
                    '200': {
                        description: 'Successful response',
                        content: {
                            'application/json': {
                                schema: { type: 'object' }
                            }
                        }
                    },
                    '400': { description: 'Bad Request' },
                    '401': { description: 'Unauthorized' },
                    '403': { description: 'Forbidden' },
                    '404': { description: 'Not Found' },
                    '500': { description: 'Internal Server Error' }
                }
            };
        });

        return spec;
    }

    /**
     * Generate common schemas for API documentation
     */
    generateSchemas() {
        return {
            Transaction: {
                type: 'object',
                properties: {
                    id: { type: 'string', format: 'uuid' },
                    transactionReference: { type: 'string' },
                    amount: { type: 'number', format: 'decimal' },
                    currencyCode: { type: 'string', example: 'USD' },
                    status: { 
                        type: 'string', 
                        enum: ['PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED'] 
                    },
                    createdAt: { type: 'string', format: 'date-time' }
                }
            },
            Account: {
                type: 'object',
                properties: {
                    id: { type: 'string', format: 'uuid' },
                    accountNumber: { type: 'string' },
                    balance: { type: 'number', format: 'decimal' },
                    currencyCode: { type: 'string', example: 'USD' },
                    status: { 
                        type: 'string', 
                        enum: ['ACTIVE', 'INACTIVE', 'SUSPENDED', 'CLOSED'] 
                    }
                }
            },
            PaymentType: {
                type: 'object',
                properties: {
                    id: { type: 'string', format: 'uuid' },
                    code: { type: 'string' },
                    name: { type: 'string' },
                    isSynchronous: { type: 'boolean' },
                    maxAmount: { type: 'number', format: 'decimal' },
                    processingFee: { type: 'number', format: 'decimal' }
                }
            },
            Error: {
                type: 'object',
                properties: {
                    code: { type: 'string' },
                    message: { type: 'string' },
                    details: { type: 'object' },
                    timestamp: { type: 'string', format: 'date-time' }
                }
            }
        };
    }

    /**
     * Generate SDK usage examples
     */
    generateSdkExamples() {
        return {
            javascript: {
                installation: 'npm install @payment-engine/sdk',
                basicUsage: `
const PaymentEngine = require('@payment-engine/sdk');

const client = new PaymentEngine({
  apiKey: 'your-api-key',
  baseUrl: 'https://api.payment-engine.com'
});

// Create a transaction
const transaction = await client.transactions.create({
  fromAccountId: 'acc_123',
  toAccountId: 'acc_456',
  paymentTypeId: 'rtp',
  amount: 1000.00,
  description: 'Payment for services'
});
                `
            },
            python: {
                installation: 'pip install payment-engine-sdk',
                basicUsage: `
from payment_engine import PaymentEngineClient

client = PaymentEngineClient(
    api_key='your-api-key',
    base_url='https://api.payment-engine.com'
)

# Create a transaction
transaction = client.transactions.create(
    from_account_id='acc_123',
    to_account_id='acc_456',
    payment_type_id='rtp',
    amount=1000.00,
    description='Payment for services'
)
                `
            },
            java: {
                installation: `
<dependency>
    <groupId>com.paymentengine</groupId>
    <artifactId>payment-engine-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
                `,
                basicUsage: `
PaymentEngineClient client = new PaymentEngineClient.Builder()
    .apiKey("your-api-key")
    .baseUrl("https://api.payment-engine.com")
    .build();

Transaction transaction = client.transactions().create(
    CreateTransactionRequest.builder()
        .fromAccountId("acc_123")
        .toAccountId("acc_456")
        .paymentTypeId("rtp")
        .amount(new BigDecimal("1000.00"))
        .description("Payment for services")
        .build()
);
                `
            }
        };
    }

    /**
     * Generate integration guides
     */
    generateIntegrationGuides() {
        return {
            ecommerce: this.generateEcommerceIntegration(),
            mobileApp: this.generateMobileAppIntegration(),
            partnerBank: this.generatePartnerBankIntegration(),
            webhook: this.generateWebhookIntegration()
        };
    }

    generateEcommerceIntegration() {
        return `
# E-commerce Integration Guide

## Overview
Integrate Payment Engine with your e-commerce platform for seamless payment processing.

## Implementation Steps

### 1. Initialize Payment Client
\`\`\`javascript
const paymentClient = new PaymentEngine({
  apiKey: process.env.PAYMENT_ENGINE_API_KEY,
  baseUrl: 'https://api.payment-engine.com'
});
\`\`\`

### 2. Process Checkout Payment
\`\`\`javascript
async function processCheckout(order) {
  try {
    const transaction = await paymentClient.transactions.create({
      fromAccountId: order.customerAccountId,
      toAccountId: order.merchantAccountId,
      paymentTypeId: order.paymentMethod,
      amount: order.total,
      description: \`Payment for order \${order.id}\`,
      externalReference: order.id
    });
    
    return { success: true, transactionId: transaction.id };
  } catch (error) {
    return { success: false, error: error.message };
  }
}
\`\`\`

### 3. Handle Webhook Notifications
\`\`\`javascript
app.post('/webhooks/payment-engine', (req, res) => {
  const event = req.body;
  
  switch (event.eventType) {
    case 'transaction.completed':
      // Mark order as paid
      updateOrderStatus(event.data.externalReference, 'paid');
      break;
    case 'transaction.failed':
      // Handle payment failure
      updateOrderStatus(event.data.externalReference, 'failed');
      break;
  }
  
  res.status(200).send('OK');
});
\`\`\`
        `;
    }

    generateMobileAppIntegration() {
        return `
# Mobile App Integration Guide

## React Native Integration

### 1. Install SDK
\`\`\`bash
npm install @payment-engine/react-native-sdk
\`\`\`

### 2. Initialize Client
\`\`\`javascript
import PaymentEngine from '@payment-engine/react-native-sdk';

const paymentClient = new PaymentEngine({
  apiKey: 'your-mobile-api-key',
  baseUrl: 'https://api.payment-engine.com',
  platform: 'mobile'
});
\`\`\`

### 3. Implement Payment Flow
\`\`\`javascript
const PaymentScreen = () => {
  const [loading, setLoading] = useState(false);
  
  const processPayment = async (paymentData) => {
    setLoading(true);
    try {
      const result = await paymentClient.transactions.create({
        ...paymentData,
        metadata: {
          deviceId: DeviceInfo.getUniqueId(),
          platform: Platform.OS
        }
      });
      
      // Handle success
      navigation.navigate('PaymentSuccess', { transaction: result });
    } catch (error) {
      // Handle error
      Alert.alert('Payment Failed', error.message);
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <PaymentForm onSubmit={processPayment} loading={loading} />
  );
};
\`\`\`
        `;
    }

    generatePartnerBankIntegration() {
        return `
# Partner Bank Integration Guide

## Core Banking System Integration

### 1. API Gateway Configuration
Configure your core banking system to communicate with Payment Engine:

\`\`\`yaml
# application.yml
payment-engine:
  partner-banks:
    - name: "Partner Bank ABC"
      api-url: "https://api.partnerbank.com"
      authentication:
        type: "oauth2"
        client-id: "\${PARTNER_BANK_CLIENT_ID}"
        client-secret: "\${PARTNER_BANK_CLIENT_SECRET}"
      endpoints:
        account-lookup: "/accounts/{accountNumber}"
        balance-inquiry: "/accounts/{accountNumber}/balance"
        fund-transfer: "/transfers"
\`\`\`

### 2. Implement Partner Bank Client
\`\`\`java
@FeignClient(name = "partner-bank", url = "\${payment-engine.partner-banks[0].api-url}")
public interface PartnerBankClient {
    
    @GetMapping("/accounts/{accountNumber}")
    AccountInfo getAccountInfo(@PathVariable String accountNumber);
    
    @PostMapping("/transfers")
    TransferResponse initiateTransfer(@RequestBody TransferRequest request);
}
\`\`\`
        `;
    }

    generateWebhookIntegration() {
        return `
# Webhook Integration Guide

## Setting Up Webhooks

### 1. Configure Webhook Endpoint
\`\`\`bash
curl -X POST https://api.payment-engine.com/api/v1/webhooks/configure \\
  -H "Authorization: Bearer <token>" \\
  -d '{
    "name": "my-webhook",
    "url": "https://myapp.com/webhooks/payment-engine",
    "events": ["transaction.completed", "transaction.failed"],
    "secret": "my-webhook-secret"
  }'
\`\`\`

### 2. Implement Webhook Handler
\`\`\`javascript
const crypto = require('crypto');

function verifyWebhookSignature(payload, signature, secret) {
  const expectedSignature = crypto
    .createHmac('sha256', secret)
    .update(payload, 'utf8')
    .digest('hex');
  
  return crypto.timingSafeEqual(
    Buffer.from(signature),
    Buffer.from(\`sha256=\${expectedSignature}\`)
  );
}

app.post('/webhooks/payment-engine', (req, res) => {
  const signature = req.headers['x-webhook-signature'];
  const payload = JSON.stringify(req.body);
  
  if (!verifyWebhookSignature(payload, signature, process.env.WEBHOOK_SECRET)) {
    return res.status(401).send('Invalid signature');
  }
  
  // Process webhook event
  handlePaymentEvent(req.body);
  res.status(200).send('OK');
});
\`\`\`
        `;
    }

    /**
     * Generate performance optimization suggestions
     */
    generatePerformanceSuggestions() {
        return `
# Performance Optimization Suggestions

## Database Optimizations
- Add database indexes for frequently queried fields
- Implement connection pooling with optimal settings
- Use read replicas for reporting queries
- Implement database partitioning for large tables

## Caching Strategies
- Cache frequently accessed payment types and configurations
- Implement Redis clustering for high availability
- Use application-level caching for user sessions
- Cache account balances with appropriate TTL

## API Optimizations
- Implement request/response compression
- Use HTTP/2 for improved performance
- Implement proper pagination for large result sets
- Add API response caching for read-only endpoints

## Monitoring Recommendations
- Set up custom business metrics for transaction volumes
- Implement distributed tracing for request flow analysis
- Monitor database query performance
- Track API response times and error rates
        `;
    }

    /**
     * Generate security best practices
     */
    generateSecurityGuide() {
        return `
# Security Best Practices

## Authentication & Authorization
- Implement multi-factor authentication for admin users
- Use short-lived access tokens (1 hour max)
- Implement proper token refresh mechanisms
- Use role-based access control with least privilege principle

## Data Protection
- Encrypt sensitive data at rest using AES-256
- Use TLS 1.3 for all communications
- Implement proper key rotation policies
- Mask PII data in logs and non-production environments

## API Security
- Implement rate limiting per user and IP
- Use API key authentication for service-to-service calls
- Validate all input parameters
- Implement proper CORS policies

## Monitoring & Auditing
- Log all authentication attempts
- Monitor for suspicious transaction patterns
- Implement real-time fraud detection
- Maintain immutable audit trails
        `;
    }

    /**
     * Main execution function
     */
    async execute() {
        console.log('🚀 Starting Payment Engine documentation generation...');
        
        try {
            const apiDocs = await this.generateApiDocumentation();
            const performanceDocs = this.generatePerformanceSuggestions();
            const securityDocs = this.generateSecurityGuide();
            
            // Write generated documentation
            fs.writeFileSync(
                path.join(this.documentationPath, 'GENERATED_API_SPEC.json'),
                JSON.stringify(apiDocs.openApiSpec, null, 2)
            );
            
            fs.writeFileSync(
                path.join(this.documentationPath, 'SDK_EXAMPLES.md'),
                JSON.stringify(apiDocs.sdkExamples, null, 2)
            );
            
            fs.writeFileSync(
                path.join(this.documentationPath, 'PERFORMANCE_GUIDE.md'),
                performanceDocs
            );
            
            fs.writeFileSync(
                path.join(this.documentationPath, 'SECURITY_GUIDE.md'),
                securityDocs
            );
            
            console.log('✅ Documentation generation completed successfully!');
            console.log('📁 Generated files:');
            console.log('   - GENERATED_API_SPEC.json');
            console.log('   - SDK_EXAMPLES.md');
            console.log('   - PERFORMANCE_GUIDE.md');
            console.log('   - SECURITY_GUIDE.md');
            
        } catch (error) {
            console.error('❌ Documentation generation failed:', error.message);
            process.exit(1);
        }
    }
}

// Execute if run directly
if (require.main === module) {
    const generator = new PaymentEngineDocumentationGenerator();
    generator.execute();
}

module.exports = PaymentEngineDocumentationGenerator;