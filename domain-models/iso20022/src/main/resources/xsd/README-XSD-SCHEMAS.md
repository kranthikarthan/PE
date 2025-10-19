# ISO 20022 XSD Schemas

## ⚠️ IMPORTANT NOTICE

This directory should contain the **official ISO 20022 XSD schemas**. They are **NOT included** in this repository due to licensing restrictions.

## Required Schemas

You must download and place the following files in this directory:

1. **pacs.008.001.08.xsd** - Customer Credit Transfer
2. **pacs.002.001.10.xsd** - Payment Status Report  
3. **pacs.004.001.09.xsd** - Payment Return
4. **camt.054.001.08.xsd** - Debit/Credit Notification

## Where to Download

### Option 1: Official ISO 20022 Repository

```
Website: https://www.iso20022.org/
Path: Catalogue → Payment Clearing & Settlement → Download Schemas
```

### Option 2: SARB/BankservAfrica

Contact the clearing systems for South African-specific schemas:

- **SARB SAMOS**: technical-support@resbank.co.za
- **BankservAfrica**: support@bankservafrica.com  
- **PayShap**: developers@payshap.co.za

## Installation

```bash
# 1. Download schemas
# 2. Copy to this directory
cp /path/to/downloaded/*.xsd ./

# 3. Rebuild the project
cd ../../../../../../
mvn clean install
```

## Verification

After placing the schemas, verify they are correct:

```bash
ls -la *.xsd
```

You should see:
```
pacs.008.001.08.xsd
pacs.002.001.10.xsd
pacs.004.001.09.xsd
camt.054.001.08.xsd
```

## Build Without Schemas (Development Only)

For development without official schemas, simplified versions can be generated:

```bash
# This creates minimal schemas for compilation only
mvn clean install -Dgenerate.simplified.schemas=true
```

⚠️ **DO NOT use simplified schemas in production!**

## Status

Current status: **❌ Official schemas not installed**

After installing official schemas: **✅ Production ready**
