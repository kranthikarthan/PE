# Operations Portal Service

A React-based frontend service for payment operations management, providing a web interface for monitoring, managing, and troubleshooting payment operations.

## Architecture

This service is designed as a **pure frontend microservice** that communicates with backend services via REST APIs. It follows microservices principles:

- **Independent Deployment**: Can be deployed separately from backend services
- **API-Driven Communication**: Communicates only via HTTP/REST APIs
- **No Direct Database Access**: All data access goes through backend APIs
- **Stateless**: No server-side state management

## Features

- **Dashboard**: Real-time payment metrics and system health
- **Transaction Enquiries**: Search and view payment transactions
- **Payment Repair**: Manual intervention and repair operations
- **Service Management**: Monitor and manage payment services
- **Reconciliation Monitoring**: Track reconciliation processes
- **Channel Onboarding**: Manage payment channels
- **Clearing System Onboarding**: Configure clearing systems

## Technology Stack

- **Frontend**: React 18, TypeScript
- **State Management**: React Context API
- **HTTP Client**: Axios with interceptors
- **Testing**: Jest, React Testing Library, Cypress
- **Build**: Create React App with custom configuration
- **Deployment**: Docker, Kubernetes

## Development

### Prerequisites

- Node.js 18+
- npm 9+

### Setup

```bash
# Install dependencies
npm install

# Start development server
npm start

# Run tests
npm test

# Build for production
npm run build
```

### Environment Configuration

The service uses environment variables for configuration:

- `REACT_APP_API_BASE_URL`: Backend API base URL
- `REACT_APP_AUTH_SERVICE_URL`: Authentication service URL
- `REACT_APP_PAYMENT_SERVICE_URL`: Payment initiation service URL
- `REACT_APP_ROUTING_SERVICE_URL`: Routing service URL

## API Integration

The service integrates with multiple backend services:

1. **Payment Initiation Service**: Payment processing operations
2. **Routing Service**: Payment routing decisions
3. **Authentication Service**: User authentication and authorization
4. **Operations Management Service**: System operations and monitoring

## Security

- **Authentication**: JWT-based authentication
- **Authorization**: Role-based access control
- **Input Validation**: Client-side validation with server-side verification
- **Secure Storage**: Encrypted local storage for sensitive data
- **HTTPS**: All API communications over HTTPS

## Deployment

### Docker

```bash
# Build image
docker build -t operations-portal-service .

# Run container
docker run -p 3000:80 operations-portal-service
```

### Kubernetes

```bash
# Apply Kubernetes manifests
kubectl apply -f k8s/
```

## Monitoring

- **Health Checks**: Built-in health check endpoints
- **Metrics**: Performance and usage metrics
- **Logging**: Structured logging with correlation IDs
- **Error Tracking**: Comprehensive error handling and reporting

## Testing

- **Unit Tests**: Component and utility testing
- **Integration Tests**: API integration testing
- **E2E Tests**: End-to-end user journey testing
- **Performance Tests**: Load and performance testing

## Contributing

1. Follow TypeScript best practices
2. Write comprehensive tests
3. Update documentation
4. Follow semantic versioning
5. Use conventional commits

## License

Proprietary - Internal use only