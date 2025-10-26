#!/bin/bash

# Read the POM file
content=$(cat pom.xml)

# Replace the routing service dependency section
new_content=$(echo "$content" | sed 's/<!-- Routing Service - Temporarily disabled for build -->/<!-- Routing Service -->/' | sed '/<!-- Routing Service -->/,/-->/s/<!--//' | sed '/<!-- Routing Service -->/,/-->/s/-->//')

# Write the fixed content back
echo "$new_content" > pom.xml

echo "POM file fixed successfully"
