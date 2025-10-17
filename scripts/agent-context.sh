#!/bin/bash

# CURSOR AGENT CONTEXT LOADER
# This script helps Cursor agents quickly acquire project context
# Compatible with Git Bash on Windows

echo "=== CURSOR AGENT CONTEXT LOADER ==="
echo "Loading project context for efficient implementation..."
echo ""

# 1. Project Overview
echo "1. 📚 Reading project architecture..."
if [ -f "docs/architecture/FINAL-ARCHITECTURE-OVERVIEW.md" ]; then
    echo "   ✅ Found architecture overview"
else
    echo "   ⚠️  Architecture overview not found"
fi

if [ -f "docs/implementation/feature-breakdown-tree.yaml" ]; then
    echo "   ✅ Found feature breakdown tree"
else
    echo "   ⚠️  Feature breakdown tree not found"
fi

# 2. Domain Models
echo ""
echo "2. 🏗️ Loading domain models..."
if [ -d "domain-models" ]; then
    echo "   ✅ Found domain models directory"
    echo "   📁 Available domains:"
    if command -v ls >/dev/null 2>&1; then
        ls -1 domain-models/ 2>/dev/null | sed 's/^/      - /' || echo "      - (unable to list)"
    else
        echo "      - (ls command not available)"
    fi
else
    echo "   ⚠️  Domain models directory not found"
fi

# 3. Contracts
echo ""
echo "3. 📋 Checking existing contracts..."
if [ -d "contracts" ]; then
    echo "   ✅ Found contracts directory"
    echo "   📁 Available contracts:"
    if command -v find >/dev/null 2>&1; then
        contract_count=$(find contracts/ -name "*.java" 2>/dev/null | wc -l)
        if [ "$contract_count" -gt 0 ]; then
            find contracts/ -name "*.java" 2>/dev/null | head -5 | sed 's/^/      - /'
            if [ "$contract_count" -gt 5 ]; then
                remaining=$((contract_count - 5))
                echo "      ... and $remaining more"
            fi
        else
            echo "      - No Java files found"
        fi
    else
        echo "      - (find command not available)"
    fi
else
    echo "   ⚠️  Contracts directory not found"
fi

# 4. Existing Services
echo ""
echo "4. 🔧 Analyzing existing services..."
services=("payment-initiation-service" "transaction-processing-service" "validation-service" "saga-orchestrator" "routing-service")
for service in "${services[@]}"; do
    if [ -d "$service" ]; then
        echo "   ✅ Found $service"
    fi
done

# 5. Pattern Analysis
echo ""
echo "5. 🔍 Analyzing existing patterns..."
if command -v find >/dev/null 2>&1; then
    service_count=$(find . -name "*Service.java" -path "*/src/main/java/*" 2>/dev/null | wc -l)
    repo_count=$(find . -name "*Repository.java" -path "*/src/main/java/*" 2>/dev/null | wc -l)
    entity_count=$(find . -name "*Entity.java" -path "*/src/main/java/*" 2>/dev/null | wc -l)
    
    echo "   📊 Service patterns: $service_count services found"
    echo "   📊 Repository patterns: $repo_count repositories found"
    echo "   📊 Entity patterns: $entity_count entities found"
else
    echo "   📊 Pattern analysis: (find command not available)"
fi

# 6. Context Summary
echo ""
echo "6. 📋 Context Summary:"
echo "   🎯 Architecture: Microservices payments engine"
echo "   🏗️  Patterns: DDD, CQRS, Saga, Event Sourcing"
echo "   🔧 Tech Stack: Spring Boot 3.x, Java 17, Maven, Docker, Kafka, PostgreSQL"
echo "   📚 Documentation: Organized in docs/ directory"
echo "   🏗️  Domain Models: Available in domain-models/"
echo "   📋 Contracts: Available in contracts/"
echo "   🔧 Services: Multiple microservices available"

# 7. Next Steps
echo ""
echo "7. 🚀 Next Steps:"
echo "   📖 Read CURSOR-AGENT-CONTEXT.md for detailed guidance"
echo "   📋 Follow AGENT-CONTEXT-CHECKLIST.md for validation"
echo "   🏗️  Use IMPLEMENTATION-STRATEGY.md for implementation"
echo "   ⚠️  Check ANTI-PATTERNS-TO-AVOID.md for common mistakes"
echo ""
echo "✅ Context loaded successfully!"
echo "🎯 Ready for efficient implementation!"
