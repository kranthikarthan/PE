package com.paymentengine.paymentprocessing.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.paymentengine.paymentprocessing.dto.SchemeConfigRequest;
import com.paymentengine.paymentprocessing.service.Iso20022FormatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Implementation of ISO 20022 message format service
 */
@Service
public class Iso20022FormatServiceImpl implements Iso20022FormatService {
    
    private static final Logger logger = LoggerFactory.getLogger(Iso20022FormatServiceImpl.class);
    
    private final ObjectMapper jsonMapper;
    private final XmlMapper xmlMapper;
    
    // Message type to class mapping
    private final Map<String, Class<?>> messageTypeClasses;
    
    public Iso20022FormatServiceImpl() {
        this.jsonMapper = new ObjectMapper();
        this.jsonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        this.xmlMapper = new XmlMapper();
        this.xmlMapper.registerModule(new JaxbAnnotationModule());
        this.xmlMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        this.messageTypeClasses = initializeMessageTypeClasses();
    }
    
    @Override
    public Object convertMessage(Object message, SchemeConfigRequest.MessageFormat fromFormat, 
                                SchemeConfigRequest.MessageFormat toFormat, String messageType) {
        
        logger.debug("Converting message from {} to {} for message type: {}", fromFormat, toFormat, messageType);
        
        if (fromFormat == toFormat) {
            return message;
        }
        
        try {
            // First deserialize from source format
            Object deserializedMessage;
            if (fromFormat == SchemeConfigRequest.MessageFormat.JSON) {
                deserializedMessage = deserializeFromJson(jsonMapper.writeValueAsString(message), messageType);
            } else if (fromFormat == SchemeConfigRequest.MessageFormat.XML) {
                deserializedMessage = deserializeFromXml(xmlMapper.writeValueAsString(message), messageType);
            } else {
                throw new IllegalArgumentException("Unsupported source format: " + fromFormat);
            }
            
            // Then serialize to target format
            if (toFormat == SchemeConfigRequest.MessageFormat.JSON) {
                return deserializeFromJson(serializeToJson(deserializedMessage, messageType), messageType);
            } else if (toFormat == SchemeConfigRequest.MessageFormat.XML) {
                return deserializeFromXml(serializeToXml(deserializedMessage, messageType), messageType);
            } else {
                throw new IllegalArgumentException("Unsupported target format: " + toFormat);
            }
            
        } catch (Exception e) {
            logger.error("Error converting message format: {}", e.getMessage());
            throw new RuntimeException("Message format conversion failed", e);
        }
    }
    
    @Override
    public String serializeToJson(Object message, String messageType) {
        logger.debug("Serializing message to JSON for message type: {}", messageType);
        
        try {
            return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);
        } catch (Exception e) {
            logger.error("Error serializing message to JSON: {}", e.getMessage());
            throw new RuntimeException("JSON serialization failed", e);
        }
    }
    
    @Override
    public String serializeToXml(Object message, String messageType) {
        logger.debug("Serializing message to XML for message type: {}", messageType);
        
        try {
            // For XML, we need to handle ISO 20022 specific formatting
            String xml = xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);
            return formatIso20022Xml(xml, messageType);
        } catch (Exception e) {
            logger.error("Error serializing message to XML: {}", e.getMessage());
            throw new RuntimeException("XML serialization failed", e);
        }
    }
    
    @Override
    public Object deserializeFromJson(String jsonMessage, String messageType) {
        logger.debug("Deserializing message from JSON for message type: {}", messageType);
        
        try {
            Class<?> messageClass = getMessageClass(messageType);
            return jsonMapper.readValue(jsonMessage, messageClass);
        } catch (Exception e) {
            logger.error("Error deserializing message from JSON: {}", e.getMessage());
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }
    
    @Override
    public Object deserializeFromXml(String xmlMessage, String messageType) {
        logger.debug("Deserializing message from XML for message type: {}", messageType);
        
        try {
            Class<?> messageClass = getMessageClass(messageType);
            return xmlMapper.readValue(xmlMessage, messageClass);
        } catch (Exception e) {
            logger.error("Error deserializing message from XML: {}", e.getMessage());
            throw new RuntimeException("XML deserialization failed", e);
        }
    }
    
    @Override
    public Map<String, Object> validateMessageFormat(Object message, SchemeConfigRequest.MessageFormat format, String messageType) {
        logger.debug("Validating message format: {} for message type: {}", format, messageType);
        
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        try {
            // Basic validation
            if (message == null) {
                errors.add("Message cannot be null");
                return createValidationResult(false, errors, warnings);
            }
            
            // Format-specific validation
            if (format == SchemeConfigRequest.MessageFormat.JSON) {
                validateJsonFormat(message, messageType, errors, warnings);
            } else if (format == SchemeConfigRequest.MessageFormat.XML) {
                validateXmlFormat(message, messageType, errors, warnings);
            }
            
            // Message type specific validation
            validateMessageTypeSpecific(message, messageType, errors, warnings);
            
            return createValidationResult(errors.isEmpty(), errors, warnings);
            
        } catch (Exception e) {
            logger.error("Error validating message format: {}", e.getMessage());
            errors.add("Validation error: " + e.getMessage());
            return createValidationResult(false, errors, warnings);
        }
    }
    
    @Override
    public String getMessageSchema(String messageType, SchemeConfigRequest.MessageFormat format) {
        logger.debug("Getting message schema for message type: {} and format: {}", messageType, format);
        
        // Return mock schema - in production, load from actual schema files
        if (format == SchemeConfigRequest.MessageFormat.JSON) {
            return getJsonSchema(messageType);
        } else if (format == SchemeConfigRequest.MessageFormat.XML) {
            return getXmlSchema(messageType);
        }
        
        throw new IllegalArgumentException("Unsupported format: " + format);
    }
    
    @Override
    public Object transformMessageVersion(Object message, String fromVersion, String toVersion, String messageType) {
        logger.debug("Transforming message from version {} to {} for message type: {}", fromVersion, toVersion, messageType);
        
        // Mock version transformation - in production, implement actual transformation logic
        try {
            // For now, just return the message as-is
            // In production, this would contain actual transformation logic
            return message;
        } catch (Exception e) {
            logger.error("Error transforming message version: {}", e.getMessage());
            throw new RuntimeException("Message version transformation failed", e);
        }
    }
    
    @Override
    public Map<String, Object> getSupportedMessageTypes() {
        logger.debug("Getting supported message types");
        
        return Map.of(
                "messageTypes", Arrays.asList(
                        Map.of(
                                "type", "pain001",
                                "name", "Customer Credit Transfer Initiation",
                                "description", "Initiates a credit transfer from customer to beneficiary",
                                "supportedFormats", Arrays.asList("JSON", "XML"),
                                "versions", Arrays.asList("2019", "2021")
                        ),
                        Map.of(
                                "type", "pain002",
                                "name", "Customer Payment Status Report",
                                "description", "Reports the status of a customer payment",
                                "supportedFormats", Arrays.asList("JSON", "XML"),
                                "versions", Arrays.asList("2019", "2021")
                        ),
                        Map.of(
                                "type", "camt055",
                                "name", "Customer Payment Cancellation Request",
                                "description", "Requests cancellation of a customer payment",
                                "supportedFormats", Arrays.asList("JSON", "XML"),
                                "versions", Arrays.asList("2019", "2021")
                        ),
                        Map.of(
                                "type", "camt053",
                                "name", "Bank to Customer Statement",
                                "description", "Provides account statement information",
                                "supportedFormats", Arrays.asList("JSON", "XML"),
                                "versions", Arrays.asList("2019", "2021")
                        ),
                        Map.of(
                                "type", "pacs008",
                                "name", "FI to FI Customer Credit Transfer",
                                "description", "Financial institution to financial institution credit transfer",
                                "supportedFormats", Arrays.asList("JSON", "XML"),
                                "versions", Arrays.asList("2019", "2021")
                        )
                ),
                "totalTypes", 5,
                "supportedFormats", Arrays.asList("JSON", "XML"),
                "supportedVersions", Arrays.asList("2019", "2021")
        );
    }
    
    @Override
    public Object getMessageTemplate(String messageType, SchemeConfigRequest.MessageFormat format) {
        logger.debug("Getting message template for message type: {} and format: {}", messageType, format);
        
        // Return mock template - in production, load from actual template files
        return createMessageTemplate(messageType, format);
    }
    
    private Map<String, Class<?>> initializeMessageTypeClasses() {
        Map<String, Class<?>> classes = new HashMap<>();
        
        // In production, these would be actual ISO 20022 message classes
        // For now, we'll use Map to represent the message structure
        classes.put("pain001", Map.class);
        classes.put("pain002", Map.class);
        classes.put("camt055", Map.class);
        classes.put("camt053", Map.class);
        classes.put("pacs008", Map.class);
        
        return classes;
    }
    
    private Class<?> getMessageClass(String messageType) {
        Class<?> messageClass = messageTypeClasses.get(messageType);
        if (messageClass == null) {
            throw new IllegalArgumentException("Unsupported message type: " + messageType);
        }
        return messageClass;
    }
    
    private String formatIso20022Xml(String xml, String messageType) {
        // Add ISO 20022 specific XML formatting
        StringBuilder formatted = new StringBuilder();
        formatted.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        formatted.append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:").append(messageType).append("\">\n");
        formatted.append(xml);
        formatted.append("\n</Document>");
        
        return formatted.toString();
    }
    
    private void validateJsonFormat(Object message, String messageType, List<String> errors, List<String> warnings) {
        try {
            String json = jsonMapper.writeValueAsString(message);
            jsonMapper.readValue(json, getMessageClass(messageType));
        } catch (Exception e) {
            errors.add("Invalid JSON format: " + e.getMessage());
        }
    }
    
    private void validateXmlFormat(Object message, String messageType, List<String> errors, List<String> warnings) {
        try {
            String xml = xmlMapper.writeValueAsString(message);
            xmlMapper.readValue(xml, getMessageClass(messageType));
        } catch (Exception e) {
            errors.add("Invalid XML format: " + e.getMessage());
        }
    }
    
    private void validateMessageTypeSpecific(Object message, String messageType, List<String> errors, List<String> warnings) {
        // Message type specific validation logic
        if (message instanceof Map) {
            Map<String, Object> messageMap = (Map<String, Object>) message;
            
            switch (messageType) {
                case "pain001":
                    validatePain001(messageMap, errors, warnings);
                    break;
                case "pain002":
                    validatePain002(messageMap, errors, warnings);
                    break;
                case "camt055":
                    validateCamt055(messageMap, errors, warnings);
                    break;
                default:
                    warnings.add("No specific validation rules for message type: " + messageType);
            }
        }
    }
    
    private void validatePain001(Map<String, Object> message, List<String> errors, List<String> warnings) {
        if (!message.containsKey("CstmrCdtTrfInitn")) {
            errors.add("PAIN.001 message must contain CstmrCdtTrfInitn element");
        }
        
        // Add more specific validation rules for PAIN.001
    }
    
    private void validatePain002(Map<String, Object> message, List<String> errors, List<String> warnings) {
        if (!message.containsKey("CstmrPmtStsRpt")) {
            errors.add("PAIN.002 message must contain CstmrPmtStsRpt element");
        }
        
        // Add more specific validation rules for PAIN.002
    }
    
    private void validateCamt055(Map<String, Object> message, List<String> errors, List<String> warnings) {
        if (!message.containsKey("CstmrPmtCxlReq")) {
            errors.add("CAMT.055 message must contain CstmrPmtCxlReq element");
        }
        
        // Add more specific validation rules for CAMT.055
    }
    
    private Map<String, Object> createValidationResult(boolean valid, List<String> errors, List<String> warnings) {
        return Map.of(
                "valid", valid,
                "errors", errors,
                "warnings", warnings,
                "timestamp", new Date().toString()
        );
    }
    
    private String getJsonSchema(String messageType) {
        // Return mock JSON schema - in production, load from actual schema files
        return String.format("""
                {
                  "$schema": "http://json-schema.org/draft-07/schema#",
                  "type": "object",
                  "title": "ISO 20022 %s Message Schema",
                  "description": "JSON schema for ISO 20022 %s message",
                  "properties": {
                    "messageId": {
                      "type": "string",
                      "description": "Unique message identifier"
                    },
                    "timestamp": {
                      "type": "string",
                      "format": "date-time",
                      "description": "Message creation timestamp"
                    }
                  },
                  "required": ["messageId", "timestamp"]
                }
                """, messageType.toUpperCase(), messageType.toUpperCase());
    }
    
    private String getXmlSchema(String messageType) {
        // Return mock XML schema - in production, load from actual schema files
        return String.format("""
                <?xml version="1.0" encoding="UTF-8"?>
                <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
                           targetNamespace="urn:iso:std:iso:20022:tech:xsd:%s"
                           xmlns="urn:iso:std:iso:20022:tech:xsd:%s"
                           elementFormDefault="qualified">
                  <xs:element name="Document" type="Document"/>
                  <xs:complexType name="Document">
                    <xs:sequence>
                      <xs:element name="messageId" type="xs:string"/>
                      <xs:element name="timestamp" type="xs:dateTime"/>
                    </xs:sequence>
                  </xs:complexType>
                </xs:schema>
                """, messageType, messageType);
    }
    
    private Object createMessageTemplate(String messageType, SchemeConfigRequest.MessageFormat format) {
        // Return mock template - in production, load from actual template files
        Map<String, Object> template = new HashMap<>();
        
        switch (messageType) {
            case "pain001":
                template = createPain001Template();
                break;
            case "pain002":
                template = createPain002Template();
                break;
            case "camt055":
                template = createCamt055Template();
                break;
            default:
                template = createGenericTemplate(messageType);
        }
        
        if (format == SchemeConfigRequest.MessageFormat.XML) {
            return formatIso20022Xml(xmlMapper.writeValueAsString(template), messageType);
        } else {
            return template;
        }
    }
    
    private Map<String, Object> createPain001Template() {
        Map<String, Object> template = new HashMap<>();
        Map<String, Object> cstmrCdtTrfInitn = new HashMap<>();
        Map<String, Object> grpHdr = new HashMap<>();
        Map<String, Object> pmtInf = new HashMap<>();
        
        grpHdr.put("MsgId", "MSG-${messageId}");
        grpHdr.put("CreDtTm", "${timestamp}");
        grpHdr.put("NbOfTxs", "1");
        grpHdr.put("CtrlSum", "${amount}");
        
        pmtInf.put("PmtInfId", "PMT-${paymentId}");
        pmtInf.put("PmtMtd", "TRF");
        pmtInf.put("ReqdExctnDt", "${executionDate}");
        
        cstmrCdtTrfInitn.put("GrpHdr", grpHdr);
        cstmrCdtTrfInitn.put("PmtInf", pmtInf);
        
        template.put("CstmrCdtTrfInitn", cstmrCdtTrfInitn);
        
        return template;
    }
    
    private Map<String, Object> createPain002Template() {
        Map<String, Object> template = new HashMap<>();
        Map<String, Object> cstmrPmtStsRpt = new HashMap<>();
        Map<String, Object> grpHdr = new HashMap<>();
        
        grpHdr.put("MsgId", "MSG-${messageId}");
        grpHdr.put("CreDtTm", "${timestamp}");
        
        cstmrPmtStsRpt.put("GrpHdr", grpHdr);
        template.put("CstmrPmtStsRpt", cstmrPmtStsRpt);
        
        return template;
    }
    
    private Map<String, Object> createCamt055Template() {
        Map<String, Object> template = new HashMap<>();
        Map<String, Object> cstmrPmtCxlReq = new HashMap<>();
        Map<String, Object> grpHdr = new HashMap<>();
        
        grpHdr.put("MsgId", "MSG-${messageId}");
        grpHdr.put("CreDtTm", "${timestamp}");
        grpHdr.put("NbOfTxs", "1");
        
        cstmrPmtCxlReq.put("GrpHdr", grpHdr);
        template.put("CstmrPmtCxlReq", cstmrPmtCxlReq);
        
        return template;
    }
    
    private Map<String, Object> createGenericTemplate(String messageType) {
        Map<String, Object> template = new HashMap<>();
        template.put("messageType", messageType);
        template.put("messageId", "MSG-${messageId}");
        template.put("timestamp", "${timestamp}");
        template.put("data", "${data}");
        
        return template;
    }
}