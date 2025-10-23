# React Operations Portal

A comprehensive React-based operations portal for the Payments Engine platform, providing real-time monitoring, management, and control capabilities for all microservices.

## 🚀 Features

- **Real-time Dashboard**: System health monitoring, metrics, and alerts
- **Service Management**: Start/stop/restart services, circuit breaker management, feature flags
- **Payment Repair**: Failed payment identification, retry operations, bulk processing
- **Transaction Enquiries**: Advanced search, filtering, and export capabilities
- **Reconciliation Monitoring**: Batch processing status and performance metrics
- **Channel Onboarding**: Multi-step wizard for channel configuration
- **Clearing System Onboarding**: Support for SAMOS, BankservAfrica, RTC, PayShap, SWIFT

## 🏗️ Architecture

### Frontend Stack
- **React 18** with TypeScript
- **Material-UI v5** with Grid2 components
- **React Router DOM** for navigation
- **Axios** for API communication
- **React Context API** for state management
- **MSW** for API mocking in tests

### Backend Integration
- **22 Microservices** integration
- **JWT Authentication** with token refresh
- **Role-based Access Control (RBAC)**
- **Multi-tenant Support**
- **Real-time Data** with auto-refresh

### Testing
- **Jest** for unit testing
- **React Testing Library** for component testing
- **Cypress** for E2E testing
- **MSW** for API mocking
- **axe-core** for accessibility testing
- **80%+ Coverage** target

### Performance
- **Code Splitting** with lazy loading
- **Virtualization** for large lists
- **Memoization** with React.memo, useMemo, useCallback
- **Bundle Optimization** with tree shaking
- **Caching** strategies

## 📋 Prerequisites

- Node.js 18+ 
- npm 8+ or yarn 1.22+
- Docker (for containerized deployment)
- Kubernetes (for production deployment)

## 🛠️ Installation

### Local Development

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd payment-initiation-service/operations-portal
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Set up environment variables**
   ```bash
   cp .env.example .env.local
   # Edit .env.local with your configuration
   ```

4. **Start development server**
   ```bash
   npm start
   ```

5. **Open in browser**
   ```
   http://localhost:3000
   ```

### Docker Development

1. **Build and run with Docker Compose**
   ```bash
   docker-compose up --build
   ```

2. **Access the application**
   ```
   http://localhost:3000
   ```

## 🧪 Testing

### Run All Tests
```bash
npm test
```

### Run Specific Test Types
```bash
# Unit tests
npm run test:unit

# Component tests
npm run test:component

# Integration tests
npm run test:integration

# E2E tests
npm run test:e2e

# Accessibility tests
npm run test:a11y
```

### Test Coverage
```bash
npm run test:coverage
```

## 🚀 Deployment

### Docker

1. **Build Docker image**
   ```bash
   docker build -t operations-portal:latest .
   ```

2. **Run container**
   ```bash
   docker run -p 8080:8080 operations-portal:latest
   ```

### Kubernetes

1. **Apply Kubernetes manifests**
   ```bash
   kubectl apply -f k8s/
   ```

2. **Check deployment status**
   ```bash
   kubectl get pods -n operations-portal
   kubectl get services -n operations-portal
   kubectl get ingress -n operations-portal
   ```

### Using Deployment Scripts

**Linux/macOS:**
```bash
./scripts/deploy.sh [image-tag] [environment]
```

**Windows:**
```powershell
.\scripts\deploy.ps1 -ImageTag [image-tag] -Environment [environment]
```

## 📁 Project Structure

```
src/
├── components/          # Reusable UI components
│   ├── DataTable.tsx
│   ├── ErrorBoundary.tsx
│   ├── LoadingSpinner.tsx
│   └── ...
├── contexts/           # React Context providers
│   ├── AuthContext.tsx
│   ├── TenantContext.tsx
│   └── NotificationContext.tsx
├── hooks/              # Custom React hooks
│   ├── useApi.ts
│   ├── usePermissions.ts
│   └── ...
├── pages/              # Page components
│   ├── Dashboard.tsx
│   ├── ServiceManagement.tsx
│   └── ...
├── services/           # API service clients
│   ├── authService.ts
│   ├── operationsManagementService.ts
│   └── ...
├── types/              # TypeScript type definitions
│   ├── api.ts
│   ├── auth.ts
│   └── ...
├── utils/              # Utility functions
│   ├── formatters.ts
│   ├── validators.ts
│   └── ...
└── __tests__/          # Test files
    ├── components/
    ├── hooks/
    └── ...
```

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `REACT_APP_API_BASE_URL` | Base URL for API calls | `http://localhost:8080` |
| `REACT_APP_AUTH_SERVICE_URL` | Authentication service URL | `http://localhost:8081` |
| `REACT_APP_OPERATIONS_SERVICE_URL` | Operations service URL | `http://localhost:8082` |
| `REACT_APP_ENABLE_DEBUG` | Enable debug mode | `false` |
| `REACT_APP_ENABLE_MOCK_DATA` | Enable mock data | `false` |

### API Configuration

The application integrates with 22 microservices:

- **PaymentInitiationService** - Payment operations
- **OperationsManagementService** - Service health and management
- **TransactionProcessingService** - Transaction queries
- **ReconciliationService** - Reconciliation monitoring
- **AuthService** - Authentication and authorization
- And 17 more services...

## 🎨 UI Components

### Core Components
- **DataTable** - Sortable, filterable, paginated tables
- **LoadingSpinner** - Consistent loading indicators
- **ErrorBoundary** - Error handling and recovery
- **StatusChip** - Status display with color coding
- **SearchBar** - Advanced search with suggestions
- **DateRangePicker** - Date range selection
- **MetricCard** - Dashboard metrics display

### Page Components
- **Dashboard** - System overview and health
- **ServiceManagement** - Service control and monitoring
- **PaymentRepair** - Failed payment management
- **TransactionEnquiries** - Transaction search and analysis
- **ReconciliationMonitoring** - Reconciliation status
- **ChannelOnboarding** - Channel configuration
- **ClearingSystemOnboarding** - Clearing system setup

## 🔐 Security

- **JWT Authentication** with automatic token refresh
- **Role-based Access Control** with permission checking
- **Input Sanitization** for all user inputs
- **XSS Protection** with Content Security Policy
- **CSRF Protection** with token validation
- **Secure Cookies** with httpOnly and SameSite attributes

## 📊 Monitoring

- **Health Checks** for all services
- **Performance Metrics** with real-time monitoring
- **Error Tracking** with comprehensive logging
- **User Analytics** with privacy compliance
- **System Alerts** with notification management

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow TypeScript best practices
- Write comprehensive tests
- Use meaningful commit messages
- Update documentation as needed
- Follow the existing code style

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:

- Create an issue in the repository
- Contact the development team
- Check the [documentation](docs/) for detailed guides

## 🔄 Changelog

See [CHANGELOG.md](CHANGELOG.md) for a list of changes and version history.

## 📚 Additional Documentation

- [Architecture Guide](docs/ARCHITECTURE.md)
- [Testing Guide](docs/TESTING.md)
- [Deployment Guide](docs/DEPLOYMENT.md)
- [API Documentation](docs/API.md)
- [Security Guide](docs/SECURITY.md)