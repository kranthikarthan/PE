import re

# Read the POM file
with open('pom.xml', 'r') as f:
    content = f.read()

# Find and replace the routing service dependency section
pattern = r'<!-- Routing Service - Temporarily disabled for build -->\s*<!--\s*<dependency>\s*<groupId>com\.payments</groupId>\s*<artifactId>routing-service</artifactId>\s*<version>\${project\.version}</version>\s*</dependency>\s*-->'
replacement = '<!-- Routing Service -->\n        <dependency>\n            <groupId>com.payments</groupId>\n            <artifactId>routing-service</artifactId>\n            <version>${project.version}</version>\n        </dependency>'

# Replace the pattern
new_content = re.sub(pattern, replacement, content, flags=re.MULTILINE | re.DOTALL)

# Write the fixed content back
with open('pom.xml', 'w') as f:
    f.write(new_content)

print("POM file fixed successfully")
