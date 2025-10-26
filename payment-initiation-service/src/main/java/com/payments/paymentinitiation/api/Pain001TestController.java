package com.payments.paymentinitiation.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * Simple pain.001 test controller
 */
@RestController
@RequestMapping("/api/v1/pain001")
public class Pain001TestController {

    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testPain001(@RequestBody String pain001Xml) {
        return ResponseEntity.ok(Map.of(
            "status", "RECEIVED",
            "message", "pain.001 message received successfully",
            "timestamp", Instant.now().toString(),
            "messageId", "PAIN001-" + System.currentTimeMillis(),
            "rawMessage", pain001Xml.substring(0, Math.min(100, pain001Xml.length())) + "..."
        ));
    }

    @GetMapping("/sample")
    public ResponseEntity<String> getSamplePain001() {
        String samplePain001 = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.001.001.09">
                <CstmrCdtTrfInitn>
                    <GrpHdr>
                        <MsgId>PAIN001-20251026-001</MsgId>
                        <CreDtTm>2025-10-26T13:30:00Z</CreDtTm>
                        <NbOfTxs>1</NbOfTxs>
                        <CtrlSum>1000.00</CtrlSum>
                        <InitgPty>
                            <Nm>Test Bank</Nm>
                            <Id>
                                <OrgId>
                                    <Othr>
                                        <Id>TEST-BANK-001</Id>
                                        <SchmeNm>
                                            <Cd>BANK</Cd>
                                        </SchmeNm>
                                    </Othr>
                                </OrgId>
                            </Id>
                        </InitgPty>
                    </GrpHdr>
                    <PmtInf>
                        <PmtInfId>PMT-INF-001</PmtInfId>
                        <PmtMtd>TRF</PmtMtd>
                        <BtchBookg>false</BtchBookg>
                        <NbOfTxs>1</NbOfTxs>
                        <CtrlSum>1000.00</CtrlSum>
                        <PmtTpInf>
                            <SvcLvl>
                                <Cd>SEPA</Cd>
                            </SvcLvl>
                        </PmtTpInf>
                        <ReqdExctnDt>2025-10-26</ReqdExctnDt>
                        <Dbtr>
                            <Nm>Test Debtor</Nm>
                            <PstlAdr>
                                <Ctry>ZA</Ctry>
                            </PstlAdr>
                        </Dbtr>
                        <DbtrAcct>
                            <Id>
                                <IBAN>ZA1234567890123456789012</IBAN>
                            </Id>
                        </DbtrAcct>
                        <DbtrAgt>
                            <FinInstnId>
                                <BICFI>TESTZAJJ</BICFI>
                            </FinInstnId>
                        </DbtrAgt>
                        <CdtTrfTxInf>
                            <PmtId>
                                <TxId>TX-001</TxId>
                            </PmtId>
                            <Amt>
                                <InstdAmt Ccy="ZAR">1000.00</InstdAmt>
                            </Amt>
                            <CdtrAgt>
                                <FinInstnId>
                                    <BICFI>CREDZAJJ</BICFI>
                                </FinInstnId>
                            </CdtrAgt>
                            <Cdtr>
                                <Nm>Test Creditor</Nm>
                            </Cdtr>
                            <CdtrAcct>
                                <Id>
                                    <IBAN>ZA9876543210987654321098</IBAN>
                                </Id>
                            </CdtrAcct>
                            <RmtInf>
                                <Ustrd>Test payment for pain.001</Ustrd>
                            </RmtInf>
                        </CdtTrfTxInf>
                    </PmtInf>
                </CstmrCdtTrfInitn>
            </Document>
            """;
        
        return ResponseEntity.ok()
            .header("Content-Type", "application/xml")
            .body(samplePain001);
    }
}
