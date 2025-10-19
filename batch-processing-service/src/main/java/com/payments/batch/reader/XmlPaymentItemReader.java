package com.payments.batch.reader;

import com.payments.batch.domain.PaymentRecord;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XML payment item reader for ISO 20022 messages.
 *
 * <p>This reader processes XML files containing ISO 20022 payment messages,
 * extracting payment information from structured XML documents. It supports
 * various ISO 20022 message types including pacs.008, pacs.002, and pacs.004.
 *
 * <p><b>Features:</b>
 * <ul>
 *   <li>ISO 20022 message support (pacs.008, pacs.002, pacs.004)
 *   <li>XPath-based data extraction
 *   <li>Multiple payment support per document
 *   <li>Namespace-aware XML parsing
 *   <li>Robust error handling and logging
 * </ul>
 *
 * <p><b>Supported Message Types:</b>
 * <ul>
 *   <li>pacs.008 - Customer Credit Transfer Initiation
 *   <li>pacs.002 - Financial Institution Credit Transfer
 *   <li>pacs.004 - Payment Return
 * </ul>
 *
 * @since PE-402
 */
@Slf4j
public class XmlPaymentItemReader implements PaymentItemReaderInterface {
  
  private final Resource resource;
  private final XPath xpath;
  private final DateTimeFormatter dateFormatter;
  
  private Document document;
  private List<PaymentRecord> paymentRecords;
  private int currentIndex;
  private boolean finished;
  
  /**
   * Creates an XML reader for ISO 20022 messages.
   *
   * @param resource the XML file resource
   */
  public XmlPaymentItemReader(Resource resource) {
    this.resource = resource;
    this.xpath = XPathFactory.newInstance().newXPath();
    this.dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    this.currentIndex = 0;
    this.finished = false;
  }
  
  @Override
  public void open() throws Exception {
    log.info("Opening XML file: {}", resource.getFilename());
    
    try (InputStream inputStream = resource.getInputStream()) {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setValidating(false);
      
      DocumentBuilder builder = factory.newDocumentBuilder();
      this.document = builder.parse(inputStream);
      this.paymentRecords = extractPaymentRecords();
      this.currentIndex = 0;
      this.finished = false;
      
      log.info("XML file opened successfully. Found {} payment records", paymentRecords.size());
      
    } catch (Exception e) {
      log.error("Failed to open XML file: {}", resource.getFilename(), e);
      throw new Exception("Failed to open XML file: " + e.getMessage(), e);
    }
  }
  
  @Override
  public PaymentRecord read() throws Exception {
    if (finished || paymentRecords.isEmpty()) {
      return null;
    }
    
    if (currentIndex >= paymentRecords.size()) {
      finished = true;
      return null;
    }
    
    PaymentRecord record = paymentRecords.get(currentIndex);
    currentIndex++;
    
    log.debug("Read payment record {} of {}: {}", 
        currentIndex, paymentRecords.size(), record.getPaymentId());
    
    return record;
  }
  
  @Override
  public void close() throws Exception {
    log.debug("Closed XML file: {}", resource.getFilename());
  }
  
  /**
   * Extracts all payment records from the XML document.
   *
   * @return list of payment records
   * @throws Exception if extraction fails
   */
  private List<PaymentRecord> extractPaymentRecords() throws Exception {
    List<PaymentRecord> records = new ArrayList<>();
    
    // Try different message types
    records.addAll(extractPacs008Records());
    records.addAll(extractPacs002Records());
    records.addAll(extractPacs004Records());
    
    return records;
  }
  
  /**
   * Extracts payment records from pacs.008 (Customer Credit Transfer) messages.
   *
   * @return list of payment records
   */
  private List<PaymentRecord> extractPacs008Records() {
    List<PaymentRecord> records = new ArrayList<>();
    
    try {
      // XPath for pacs.008 CdtTrfTxInf elements
      String xpathExpression = "//pacs.008:CdtTrfTxInf | //CdtTrfTxInf";
      NodeList nodes = (NodeList) xpath.evaluate(xpathExpression, document, XPathConstants.NODESET);
      
      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        PaymentRecord record = parsePacs008Record(node);
        if (record != null) {
          records.add(record);
        }
      }
      
      log.debug("Extracted {} pacs.008 payment records", records.size());
      
    } catch (XPathExpressionException e) {
      log.warn("Failed to extract pacs.008 records: {}", e.getMessage());
    }
    
    return records;
  }
  
  /**
   * Extracts payment records from pacs.002 (Financial Institution Credit Transfer) messages.
   *
   * @return list of payment records
   */
  private List<PaymentRecord> extractPacs002Records() {
    List<PaymentRecord> records = new ArrayList<>();
    
    try {
      // XPath for pacs.002 CdtTrfTxInf elements
      String xpathExpression = "//pacs.002:CdtTrfTxInf | //CdtTrfTxInf";
      NodeList nodes = (NodeList) xpath.evaluate(xpathExpression, document, XPathConstants.NODESET);
      
      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        PaymentRecord record = parsePacs002Record(node);
        if (record != null) {
          records.add(record);
        }
      }
      
      log.debug("Extracted {} pacs.002 payment records", records.size());
      
    } catch (XPathExpressionException e) {
      log.warn("Failed to extract pacs.002 records: {}", e.getMessage());
    }
    
    return records;
  }
  
  /**
   * Extracts payment records from pacs.004 (Payment Return) messages.
   *
   * @return list of payment records
   */
  private List<PaymentRecord> extractPacs004Records() {
    List<PaymentRecord> records = new ArrayList<>();
    
    try {
      // XPath for pacs.004 PmtRtr elements
      String xpathExpression = "//pacs.004:PmtRtr | //PmtRtr";
      NodeList nodes = (NodeList) xpath.evaluate(xpathExpression, document, XPathConstants.NODESET);
      
      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        PaymentRecord record = parsePacs004Record(node);
        if (record != null) {
          records.add(record);
        }
      }
      
      log.debug("Extracted {} pacs.004 payment records", records.size());
      
    } catch (XPathExpressionException e) {
      log.warn("Failed to extract pacs.004 records: {}", e.getMessage());
    }
    
    return records;
  }
  
  /**
   * Parses a pacs.008 payment record from XML node.
   *
   * @param node the XML node
   * @return payment record or null if parsing fails
   */
  private PaymentRecord parsePacs008Record(Node node) {
    try {
      String paymentId = getNodeValue(node, ".//PmtId//TxId");
      String debtorAccount = getNodeValue(node, ".//DbtrAcct//Id//IBAN | .//DbtrAcct//Id//Othr//Id");
      String creditorAccount = getNodeValue(node, ".//CdtrAcct//Id//IBAN | .//CdtrAcct//Id//Othr//Id");
      String amount = getNodeValue(node, ".//InstrAmt//Amt");
      String currency = getNodeValue(node, ".//InstrAmt//Ccy");
      String valueDate = getNodeValue(node, ".//ReqdExctnDt");
      String paymentReference = getNodeValue(node, ".//RmtInf//Ustrd");
      String debtorName = getNodeValue(node, ".//Dbtr//Nm");
      String creditorName = getNodeValue(node, ".//Cdtr//Nm");
      String debtorBankCode = getNodeValue(node, ".//DbtrAgt//FinInstnId//BICFI");
      String creditorBankCode = getNodeValue(node, ".//CdtrAgt//FinInstnId//BICFI");
      String paymentType = "ISO20022";
      
      return PaymentRecord.builder()
          .paymentId(paymentId)
          .debtorAccount(debtorAccount)
          .creditorAccount(creditorAccount)
          .amount(parseBigDecimal(amount))
          .currency(currency)
          .valueDate(parseDate(valueDate))
          .paymentReference(paymentReference)
          .debtorName(debtorName)
          .creditorName(creditorName)
          .debtorBankCode(debtorBankCode)
          .creditorBankCode(creditorBankCode)
          .paymentType(paymentType)
          .lineNumber(currentIndex)
          .build();
          
    } catch (Exception e) {
      log.warn("Failed to parse pacs.008 record: {}", e.getMessage());
      return null;
    }
  }
  
  /**
   * Parses a pacs.002 payment record from XML node.
   *
   * @param node the XML node
   * @return payment record or null if parsing fails
   */
  private PaymentRecord parsePacs002Record(Node node) {
    try {
      String paymentId = getNodeValue(node, ".//PmtId//TxId");
      String debtorAccount = getNodeValue(node, ".//DbtrAcct//Id//IBAN | .//DbtrAcct//Id//Othr//Id");
      String creditorAccount = getNodeValue(node, ".//CdtrAcct//Id//IBAN | .//CdtrAcct//Id//Othr//Id");
      String amount = getNodeValue(node, ".//InstrAmt//Amt");
      String currency = getNodeValue(node, ".//InstrAmt//Ccy");
      String valueDate = getNodeValue(node, ".//ReqdExctnDt");
      String paymentReference = getNodeValue(node, ".//RmtInf//Ustrd");
      String debtorName = getNodeValue(node, ".//Dbtr//Nm");
      String creditorName = getNodeValue(node, ".//Cdtr//Nm");
      String debtorBankCode = getNodeValue(node, ".//DbtrAgt//FinInstnId//BICFI");
      String creditorBankCode = getNodeValue(node, ".//CdtrAgt//FinInstnId//BICFI");
      String paymentType = "ISO20022";
      
      return PaymentRecord.builder()
          .paymentId(paymentId)
          .debtorAccount(debtorAccount)
          .creditorAccount(creditorAccount)
          .amount(parseBigDecimal(amount))
          .currency(currency)
          .valueDate(parseDate(valueDate))
          .paymentReference(paymentReference)
          .debtorName(debtorName)
          .creditorName(creditorName)
          .debtorBankCode(debtorBankCode)
          .creditorBankCode(creditorBankCode)
          .paymentType(paymentType)
          .lineNumber(currentIndex)
          .build();
          
    } catch (Exception e) {
      log.warn("Failed to parse pacs.002 record: {}", e.getMessage());
      return null;
    }
  }
  
  /**
   * Parses a pacs.004 payment record from XML node.
   *
   * @param node the XML node
   * @return payment record or null if parsing fails
   */
  private PaymentRecord parsePacs004Record(Node node) {
    try {
      String paymentId = getNodeValue(node, ".//PmtId//TxId");
      String debtorAccount = getNodeValue(node, ".//DbtrAcct//Id//IBAN | .//DbtrAcct//Id//Othr//Id");
      String creditorAccount = getNodeValue(node, ".//CdtrAcct//Id//IBAN | .//CdtrAcct//Id//Othr//Id");
      String amount = getNodeValue(node, ".//InstrAmt//Amt");
      String currency = getNodeValue(node, ".//InstrAmt//Ccy");
      String valueDate = getNodeValue(node, ".//ReqdExctnDt");
      String paymentReference = getNodeValue(node, ".//RmtInf//Ustrd");
      String debtorName = getNodeValue(node, ".//Dbtr//Nm");
      String creditorName = getNodeValue(node, ".//Cdtr//Nm");
      String debtorBankCode = getNodeValue(node, ".//DbtrAgt//FinInstnId//BICFI");
      String creditorBankCode = getNodeValue(node, ".//CdtrAgt//FinInstnId//BICFI");
      String paymentType = "ISO20022";
      
      return PaymentRecord.builder()
          .paymentId(paymentId)
          .debtorAccount(debtorAccount)
          .creditorAccount(creditorAccount)
          .amount(parseBigDecimal(amount))
          .currency(currency)
          .valueDate(parseDate(valueDate))
          .paymentReference(paymentReference)
          .debtorName(debtorName)
          .creditorName(creditorName)
          .debtorBankCode(debtorBankCode)
          .creditorBankCode(creditorBankCode)
          .paymentType(paymentType)
          .lineNumber(currentIndex)
          .build();
          
    } catch (Exception e) {
      log.warn("Failed to parse pacs.004 record: {}", e.getMessage());
      return null;
    }
  }
  
  /**
   * Gets a node value using XPath expression.
   *
   * @param node the parent node
   * @param xpathExpression the XPath expression
   * @return node value or null if not found
   */
  private String getNodeValue(Node node, String xpathExpression) {
    try {
      Node result = (Node) xpath.evaluate(xpathExpression, node, XPathConstants.NODE);
      if (result != null && result.getNodeType() == Node.TEXT_NODE) {
        return result.getTextContent().trim();
      } else if (result != null) {
        return result.getTextContent().trim();
      }
    } catch (XPathExpressionException e) {
      log.debug("XPath expression failed: {}", xpathExpression, e);
    }
    return null;
  }
  
  /**
   * Parses a string field, handling null and empty values.
   *
   * @param value the string value
   * @return parsed string or null if empty
   */
  private String parseString(String value) {
    if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value)) {
      return null;
    }
    return value.trim();
  }
  
  /**
   * Parses a decimal amount field.
   *
   * @param value the amount string
   * @return parsed amount
   */
  private BigDecimal parseBigDecimal(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    
    String cleanValue = value.trim();
    if (cleanValue.isEmpty()) {
      return null;
    }
    
    try {
      return new BigDecimal(cleanValue);
    } catch (NumberFormatException e) {
      log.warn("Could not parse amount '{}': {}", cleanValue, e.getMessage());
      return null;
    }
  }
  
  /**
   * Parses a date field.
   *
   * @param value the date string
   * @return parsed date or null if empty
   */
  private LocalDate parseDate(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    
    String cleanValue = value.trim();
    
    try {
      return LocalDate.parse(cleanValue, dateFormatter);
    } catch (Exception e) {
      try {
        return LocalDate.parse(cleanValue);
      } catch (Exception e2) {
        log.warn("Could not parse date '{}': {}", cleanValue, e2.getMessage());
        return null;
      }
    }
  }
  
  @Override
  public int getCurrentLineNumber() {
    return currentIndex;
  }
  
  @Override
  public boolean isFinished() {
    return finished;
  }
  
  @Override
  public String getResourceName() {
    return resource.getFilename();
  }
}
