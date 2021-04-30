package com.xforceplus.ultraman.oqsengine.cdc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.storage.master.utils.OriginalEntityUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static com.xforceplus.ultraman.oqsengine.storage.master.utils.OriginalEntityUtils.attributesToList;

/**
 * desc :
 * name : JsonTest
 *
 * @author : xujia
 * date : 2021/4/16
 * @since : 1.8
 */
public class JsonTest {
    String str = "{\"F1295238501979242498S\": \"discern_mapping_config\", \"F1295238502528696321S\": \"识别映射配置\", \"F1295238503136870402S\": \"[\n" +
            "  {\n" +
            "    \\\"entityCode\\\": \\\"ticketInvoice\\\",\n" +
            "    \\\"mappingName\\\": \\\"增值税发票\\\",\n" +
            "    \\\"imageType\\\": \\\"2\\\",\n" +
            "    \\\"documentType\\\": \\\"10001001,10001002,10001003,20001001,20001002,30001001,30001002,30001003,40001001,50001017,50001018\\\",\n" +
            "    \\\"items\\\": [\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceCode\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_code\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceNo\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceType\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_type\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceTime\\\",\n" +
            "        \\\"entityField\\\": \\\"paper_drew_date\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"checkCode\\\",\n" +
            "        \\\"entityField\\\": \\\"check_code\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sheetIndex\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_sheet\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"cipherList\\\",\n" +
            "        \\\"entityField\\\": \\\"cipher_text\\\",\n" +
            "        \\\"expression\\\": \\\"#obj.getArray('cipherList') != null?T(org.apache.commons.lang3.StringUtils).join(#obj.getArray('cipherList')):null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"purchaserName\\\",\n" +
            "        \\\"entityField\\\": \\\"purchaser_name\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"purchaserTaxNo\\\",\n" +
            "        \\\"entityField\\\": \\\"purchaser_tax_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"purchaserAddr\\\",\n" +
            "        \\\"entityField\\\": \\\"purchaser_address\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"purchaserTel\\\",\n" +
            "        \\\"entityField\\\": \\\"purchaser_tel\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"purchaserBank\\\",\n" +
            "        \\\"entityField\\\": \\\"purchaser_bank_name\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"purchaserBankNo\\\",\n" +
            "        \\\"entityField\\\": \\\"purchaser_bank_account\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"totalAmountTaxNum\\\",\n" +
            "        \\\"entityField\\\": \\\"amount_with_tax\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"totalTax\\\",\n" +
            "        \\\"entityField\\\": \\\"tax_amount\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"totalAmount\\\",\n" +
            "        \\\"entityField\\\": \\\"amount_without_tax\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"machineCode\\\",\n" +
            "        \\\"entityField\\\": \\\"machine_code\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sellerName\\\",\n" +
            "        \\\"entityField\\\": \\\"seller_name\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sellerTaxNo\\\",\n" +
            "        \\\"entityField\\\": \\\"seller_tax_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sellerAddr\\\",\n" +
            "        \\\"entityField\\\": \\\"seller_address\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sellerTel\\\",\n" +
            "        \\\"entityField\\\": \\\"seller_tel\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sellerBank\\\",\n" +
            "        \\\"entityField\\\": \\\"seller_bank_name\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sellerBankNo\\\",\n" +
            "        \\\"entityField\\\": \\\"seller_bank_account\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"payee\\\",\n" +
            "        \\\"entityField\\\": \\\"payee\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"recheck\\\",\n" +
            "        \\\"entityField\\\": \\\"recheck\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"drawer\\\",\n" +
            "        \\\"entityField\\\": \\\"drawer\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"remark\\\",\n" +
            "        \\\"entityField\\\": \\\"remark\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"is_sales_list\\\",\n" +
            "        \\\"entityField\\\": \\\"is_sales_list\\\",\n" +
            "        \\\"expression\\\": \\\"#obj.getString('invoiceDetails') != null && #obj.getString('invoiceDetails').contains('销货清单') ?'1':'0'\\\",\n" +
            "        \\\"defaultValue\\\": \\\"0\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceTime\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_date\\\",\n" +
            "        \\\"expression\\\": \\\"#obj.getString('invoiceTime') != null && #obj.getString('invoiceTime').matches('^\\\\\\\\d{8}$') ? new java.text.SimpleDateFormat('yyyyMMdd').parse(#obj.getString('invoiceTime')).getTime() : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"stamper\\\",\n" +
            "        \\\"entityField\\\": \\\"is_stamper\\\",\n" +
            "        \\\"expression\\\": \\\"#obj.getString('stamper') != null && #obj.getString('stamper') != '' ?'1':'0'\\\",\n" +
            "        \\\"defaultValue\\\": \\\"0\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"qrcode\\\",\n" +
            "        \\\"entityField\\\": \\\"qrcode\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceCodeP\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_code_p\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceNoP\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_no_p\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceDetails\\\",\n" +
            "        \\\"entityField\\\": \\\"items\\\",\n" +
            "        \\\"mappingEntityCode\\\": \\\"ticketInvoiceDetail\\\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \\\"entityCode\\\": \\\"ticketInvoiceDetail\\\",\n" +
            "    \\\"mappingName\\\": \\\"增值税发票明细\\\",\n" +
            "    \\\"items\\\": [\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"name\\\",\n" +
            "        \\\"entityField\\\": \\\"cargo_name\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"specification\\\",\n" +
            "        \\\"entityField\\\": \\\"item_spec\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"unit\\\",\n" +
            "        \\\"entityField\\\": \\\"quantity_unit\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"quantity\\\",\n" +
            "        \\\"entityField\\\": \\\"quantity\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,27}(\\\\\\\\.\\\\\\\\d{0,15})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"price\\\",\n" +
            "        \\\"entityField\\\": \\\"unit_price\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,27}(\\\\\\\\.\\\\\\\\d{0,15})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"total\\\",\n" +
            "        \\\"entityField\\\": \\\"amount_without_tax\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"taxRate\\\",\n" +
            "        \\\"entityField\\\": \\\"tax_rate\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"tax\\\",\n" +
            "        \\\"entityField\\\": \\\"tax_amount\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \\\"entityCode\\\": \\\"ticketMachine\\\",\n" +
            "    \\\"mappingName\\\": \\\"通用机打发票\\\",\n" +
            "    \\\"imageType\\\": \\\"2\\\",\n" +
            "    \\\"documentType\\\": \\\"50001002\\\",\n" +
            "    \\\"items\\\": [\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceCode\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_code\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceNo\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceType\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_type\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceTime\\\",\n" +
            "        \\\"entityField\\\": \\\"paper_drew_date\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"checkCode\\\",\n" +
            "        \\\"entityField\\\": \\\"check_code\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sheetIndex\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_sheet\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"cipherList\\\",\n" +
            "        \\\"entityField\\\": \\\"cipher_text\\\",\n" +
            "        \\\"expression\\\": \\\"#obj.getArray('cipherList') != null?T(org.apache.commons.lang3.StringUtils).join(#obj.getArray('cipherList')):null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"purchaserName\\\",\n" +
            "        \\\"entityField\\\": \\\"purchaser_name\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"purchaserTaxNo\\\",\n" +
            "        \\\"entityField\\\": \\\"purchaser_tax_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"purchaserAddr\\\",\n" +
            "        \\\"entityField\\\": \\\"purchaser_address\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"purchaserTel\\\",\n" +
            "        \\\"entityField\\\": \\\"purchaser_tel\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"purchaserBank\\\",\n" +
            "        \\\"entityField\\\": \\\"purchaser_bank_name\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"purchaserBankNo\\\",\n" +
            "        \\\"entityField\\\": \\\"purchaser_bank_account\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"totalAmountTaxNum\\\",\n" +
            "        \\\"entityField\\\": \\\"amount_with_tax\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"totalTax\\\",\n" +
            "        \\\"entityField\\\": \\\"tax_amount\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"totalAmount\\\",\n" +
            "        \\\"entityField\\\": \\\"amount_without_tax\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"machineCode\\\",\n" +
            "        \\\"entityField\\\": \\\"machine_code\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sellerName\\\",\n" +
            "        \\\"entityField\\\": \\\"seller_name\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sellerTaxNo\\\",\n" +
            "        \\\"entityField\\\": \\\"seller_tax_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sellerAddr\\\",\n" +
            "        \\\"entityField\\\": \\\"seller_address\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sellerTel\\\",\n" +
            "        \\\"entityField\\\": \\\"seller_tel\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sellerBank\\\",\n" +
            "        \\\"entityField\\\": \\\"seller_bank_name\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sellerBankNo\\\",\n" +
            "        \\\"entityField\\\": \\\"seller_bank_account\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"payee\\\",\n" +
            "        \\\"entityField\\\": \\\"payee\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"recheck\\\",\n" +
            "        \\\"entityField\\\": \\\"recheck\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"drawer\\\",\n" +
            "        \\\"entityField\\\": \\\"drawer\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"remark\\\",\n" +
            "        \\\"entityField\\\": \\\"remark\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"is_sales_list\\\",\n" +
            "        \\\"entityField\\\": \\\"is_sales_list\\\",\n" +
            "        \\\"expression\\\": \\\"#obj.getString('invoiceDetails') != null && #obj.getString('invoiceDetails').contains('销货清单') ?'1':'0'\\\",\n" +
            "        \\\"defaultValue\\\": \\\"0\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceTime\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_date\\\",\n" +
            "        \\\"expression\\\": \\\"#obj.getString('invoiceTime') != null && #obj.getString('invoiceTime').matches('^\\\\\\\\d{8}$') ? new java.text.SimpleDateFormat('yyyyMMdd').parse(#obj.getString('invoiceTime')).getTime() : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceDetails\\\",\n" +
            "        \\\"entityField\\\": \\\"items\\\",\n" +
            "        \\\"mappingEntityCode\\\": \\\"ticketMachineDetail\\\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \\\"entityCode\\\": \\\"ticketMachineDetail\\\",\n" +
            "    \\\"mappingName\\\": \\\"通用机打发票明细\\\",\n" +
            "    \\\"items\\\": [\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"name\\\",\n" +
            "        \\\"entityField\\\": \\\"cargo_name\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"specification\\\",\n" +
            "        \\\"entityField\\\": \\\"item_spec\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"unit\\\",\n" +
            "        \\\"entityField\\\": \\\"quantity_unit\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"quantity\\\",\n" +
            "        \\\"entityField\\\": \\\"quantity\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,27}(\\\\\\\\.\\\\\\\\d{0,15})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"price\\\",\n" +
            "        \\\"entityField\\\": \\\"unit_price\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,27}(\\\\\\\\.\\\\\\\\d{0,15})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"total\\\",\n" +
            "        \\\"entityField\\\": \\\"amount_without_tax\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"taxRate\\\",\n" +
            "        \\\"entityField\\\": \\\"tax_rate\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"tax\\\",\n" +
            "        \\\"entityField\\\": \\\"tax_amount\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \\\"entityCode\\\": \\\"ticketPlane\\\",\n" +
            "    \\\"mappingName\\\": \\\"飞机票\\\",\n" +
            "    \\\"imageType\\\": \\\"2\\\",\n" +
            "    \\\"documentType\\\": \\\"50001009\\\",\n" +
            "    \\\"items\\\": [\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"nameOfPassenger\\\",\n" +
            "        \\\"entityField\\\": \\\"name_of_passenger\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"IDNo\\\",\n" +
            "        \\\"entityField\\\": \\\"id_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"endorsements\\\",\n" +
            "        \\\"entityField\\\": \\\"endorsements\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"serialNo\\\",\n" +
            "        \\\"entityField\\\": \\\"serial_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"eTicketNo\\\",\n" +
            "        \\\"entityField\\\": \\\"e_ticket_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"checkCode\\\",\n" +
            "        \\\"entityField\\\": \\\"check_code\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"information\\\",\n" +
            "        \\\"entityField\\\": \\\"information\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"agentCode\\\",\n" +
            "        \\\"entityField\\\": \\\"agent_code\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"issuedBy\\\",\n" +
            "        \\\"entityField\\\": \\\"issued_by\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"dateOfIssue\\\",\n" +
            "        \\\"entityField\\\": \\\"date_of_issue\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"insurance\\\",\n" +
            "        \\\"entityField\\\": \\\"insurance\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"fare\\\",\n" +
            "        \\\"entityField\\\": \\\"fare\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"caacDevelopmentFund\\\",\n" +
            "        \\\"entityField\\\": \\\"caac_development_fund\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"fuelSurcharge\\\",\n" +
            "        \\\"entityField\\\": \\\"fuel_surcharge\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"tax\\\",\n" +
            "        \\\"entityField\\\": \\\"tax\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"total\\\",\n" +
            "        \\\"entityField\\\": \\\"total\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"flights\\\",\n" +
            "        \\\"entityField\\\": \\\"items\\\",\n" +
            "        \\\"mappingEntityCode\\\": \\\"ticketPlaneDetail\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceType\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_type\\\",\n" +
            "        \\\"defaultValue\\\": \\\"p\\\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \\\"entityCode\\\": \\\"ticketPlaneDetail\\\",\n" +
            "    \\\"mappingName\\\": \\\"飞机票明细\\\",\n" +
            "    \\\"items\\\": [\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"summary\\\",\n" +
            "        \\\"entityField\\\": \\\"summary\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"carrier\\\",\n" +
            "        \\\"entityField\\\": \\\"carrier\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"from\\\",\n" +
            "        \\\"entityField\\\": \\\"plane_from\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"to\\\",\n" +
            "        \\\"entityField\\\": \\\"plane_to\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"flight\\\",\n" +
            "        \\\"entityField\\\": \\\"flight\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"date\\\",\n" +
            "        \\\"entityField\\\": \\\"plane_date\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"time\\\",\n" +
            "        \\\"entityField\\\": \\\"plane_time\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"class\\\",\n" +
            "        \\\"entityField\\\": \\\"seat_class\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"fareBasis\\\",\n" +
            "        \\\"entityField\\\": \\\"fare_basis\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"notValidBefore\\\",\n" +
            "        \\\"entityField\\\": \\\"not_valid_before\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"notValidAfter\\\",\n" +
            "        \\\"entityField\\\": \\\"not_valid_after\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"allow\\\",\n" +
            "        \\\"entityField\\\": \\\"allow\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"extra\\\",\n" +
            "        \\\"entityField\\\": \\\"extra\\\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \\\"entityCode\\\": \\\"ticketTrain\\\",\n" +
            "    \\\"mappingName\\\": \\\"火车票\\\",\n" +
            "    \\\"imageType\\\": \\\"2\\\",\n" +
            "    \\\"documentType\\\": \\\"50001010\\\",\n" +
            "    \\\"items\\\": [\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"trains\\\",\n" +
            "        \\\"entityField\\\": \\\"trains\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"startStation\\\",\n" +
            "        \\\"entityField\\\": \\\"start_station\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"endStation\\\",\n" +
            "        \\\"entityField\\\": \\\"end_station\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"date\\\",\n" +
            "        \\\"entityField\\\": \\\"start_date\\\",\n" +
            "        \\\"expression\\\": \\\"#obj.getString('date') != null && #obj.getString('date').matches('^\\\\\\\\d{8}$') ? new java.text.SimpleDateFormat('yyyyMMdd').parse(#obj.getString('date')).getTime() : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"time\\\",\n" +
            "        \\\"entityField\\\": \\\"start_time\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"seat\\\",\n" +
            "        \\\"entityField\\\": \\\"seat\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"seatType\\\",\n" +
            "        \\\"entityField\\\": \\\"seat_type\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"price\\\",\n" +
            "        \\\"entityField\\\": \\\"amount_with_tax\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"certificateNo\\\",\n" +
            "        \\\"entityField\\\": \\\"certificate_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"name\\\",\n" +
            "        \\\"entityField\\\": \\\"name\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"no\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"qrcode\\\",\n" +
            "        \\\"entityField\\\": \\\"qrcode\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceType\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_type\\\",\n" +
            "        \\\"defaultValue\\\": \\\"t\\\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \\\"entityCode\\\": \\\"ticketTaxi\\\",\n" +
            "    \\\"mappingName\\\": \\\"出租车票\\\",\n" +
            "    \\\"imageType\\\": \\\"2\\\",\n" +
            "    \\\"documentType\\\": \\\"50001011\\\",\n" +
            "    \\\"items\\\": [\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceCode\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_code\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceNo\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"totalAmount\\\",\n" +
            "        \\\"entityField\\\": \\\"amount_with_tax\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"date\\\",\n" +
            "        \\\"entityField\\\": \\\"start_date\\\",\n" +
            "        \\\"expression\\\": \\\"#obj.getString('date') != null && #obj.getString('date').matches('^\\\\\\\\d{8}$') ? new java.text.SimpleDateFormat('yyyyMMdd').parse(#obj.getString('date')).getTime() : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"getOnTime\\\",\n" +
            "        \\\"entityField\\\": \\\"get_on_time\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"mileage\\\",\n" +
            "        \\\"entityField\\\": \\\"mileage\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"getOffTime\\\",\n" +
            "        \\\"entityField\\\": \\\"get_off_time\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"place\\\",\n" +
            "        \\\"entityField\\\": \\\"place\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceType\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_type\\\",\n" +
            "        \\\"defaultValue\\\": \\\"taxi\\\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \\\"entityCode\\\": \\\"ticketQuota\\\",\n" +
            "    \\\"mappingName\\\": \\\"定额发票\\\",\n" +
            "    \\\"imageType\\\": \\\"2\\\",\n" +
            "    \\\"documentType\\\": \\\"50001013\\\",\n" +
            "    \\\"items\\\": [\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceCode\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_code\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceNo\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"totalAmount\\\",\n" +
            "        \\\"entityField\\\": \\\"amount_with_tax\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceType\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_type\\\",\n" +
            "        \\\"defaultValue\\\": \\\"quota\\\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \\\"entityCode\\\": \\\"ticketToll\\\",\n" +
            "    \\\"mappingName\\\": \\\"过路费\\\",\n" +
            "    \\\"imageType\\\": \\\"2\\\",\n" +
            "    \\\"documentType\\\": \\\"50001014\\\",\n" +
            "    \\\"items\\\": [\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceCode\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_code\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceNo\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"totalAmount\\\",\n" +
            "        \\\"entityField\\\": \\\"amount_with_tax\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"date\\\",\n" +
            "        \\\"entityField\\\": \\\"start_date\\\",\n" +
            "        \\\"expression\\\": \\\"#obj.getString('date') != null && #obj.getString('date').matches('^\\\\\\\\d{8}$') ? new java.text.SimpleDateFormat('yyyyMMdd').parse(#obj.getString('date')).getTime() : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"time\\\",\n" +
            "        \\\"entityField\\\": \\\"time\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"entrance\\\",\n" +
            "        \\\"entityField\\\": \\\"entrance_place\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"exit\\\",\n" +
            "        \\\"entityField\\\": \\\"exit_place\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"payment\\\",\n" +
            "        \\\"entityField\\\": \\\"payment\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"vehiclesType\\\",\n" +
            "        \\\"entityField\\\": \\\"vehicles_type\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"vehiclesWeight\\\",\n" +
            "        \\\"entityField\\\": \\\"vehicles_weight\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"limit\\\",\n" +
            "        \\\"entityField\\\": \\\"toll_limit\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceType\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_type\\\",\n" +
            "        \\\"defaultValue\\\": \\\"toll\\\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \\\"entityCode\\\": \\\"ticketBus\\\",\n" +
            "    \\\"mappingName\\\": \\\"公交车票\\\",\n" +
            "    \\\"imageType\\\": \\\"2\\\",\n" +
            "    \\\"documentType\\\": \\\"50001016\\\",\n" +
            "    \\\"items\\\": [\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"companyName\\\",\n" +
            "        \\\"entityField\\\": \\\"purchaser_name\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"companyTaxNo\\\",\n" +
            "        \\\"entityField\\\": \\\"purhcaser_tax_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceCode\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_code\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceNo\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"totalAmount\\\",\n" +
            "        \\\"entityField\\\": \\\"amount_with_tax\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"trains\\\",\n" +
            "        \\\"entityField\\\": \\\"trains\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"startStation\\\",\n" +
            "        \\\"entityField\\\": \\\"start_station\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"endStation\\\",\n" +
            "        \\\"entityField\\\": \\\"end_station\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"date\\\",\n" +
            "        \\\"entityField\\\": \\\"start_date\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"time\\\",\n" +
            "        \\\"entityField\\\": \\\"start_time\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"seat\\\",\n" +
            "        \\\"entityField\\\": \\\"seat\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"name\\\",\n" +
            "        \\\"entityField\\\": \\\"name\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceType\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_type\\\",\n" +
            "        \\\"defaultValue\\\": \\\"bus\\\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \\\"entityCode\\\": \\\"ticketUsedCar\\\",\n" +
            "    \\\"mappingName\\\": \\\"二手车销售统一发票\\\",\n" +
            "    \\\"imageType\\\": \\\"2\\\",\n" +
            "    \\\"documentType\\\": \\\"50001015\\\",\n" +
            "    \\\"items\\\": [\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceNo\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceCode\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_code\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceTime\\\",\n" +
            "        \\\"entityField\\\": \\\"paper_drew_date\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceTime\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_date\\\",\n" +
            "        \\\"expression\\\": \\\"#obj.getString('invoiceTime') != null && #obj.getString('invoiceTime').matches('^\\\\\\\\d{8}$') ? new java.text.SimpleDateFormat('yyyyMMdd').parse(#obj.getString('invoiceTime')).getTime() : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceCodeP\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_code_p\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceNoP\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_no_p\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"qrcode\\\",\n" +
            "        \\\"entityField\\\": \\\"qrcode\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sheetIndex\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_sheet\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"cipherList\\\",\n" +
            "        \\\"entityField\\\": \\\"cipher_text\\\",\n" +
            "        \\\"expression\\\": \\\"#obj.getArray('cipherList') != null?T(org.apache.commons.lang3.StringUtils).join(#obj.getArray('cipherList')):null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"purchaserName\\\",\n" +
            "        \\\"entityField\\\": \\\"purchaser_name\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"purchaserIDCode\\\",\n" +
            "        \\\"entityField\\\": \\\"purchaser_tax_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"purchaserAddr\\\",\n" +
            "        \\\"entityField\\\": \\\"purchaser_address\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"purchaserTel\\\",\n" +
            "        \\\"entityField\\\": \\\"purchaser_tel\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sellerName\\\",\n" +
            "        \\\"entityField\\\": \\\"seller_name\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sellerIDCode\\\",\n" +
            "        \\\"entityField\\\": \\\"seller_tax_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sellerAddr\\\",\n" +
            "        \\\"entityField\\\": \\\"seller_address\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sellerBank\\\",\n" +
            "        \\\"entityField\\\": \\\"seller_bank_name\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sellerBankNo\\\",\n" +
            "        \\\"entityField\\\": \\\"seller_bank_account\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sellerTel\\\",\n" +
            "        \\\"entityField\\\": \\\"seller_tel\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"licensePlateNo\\\",\n" +
            "        \\\"entityField\\\": \\\"car_number\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"registerNo\\\",\n" +
            "        \\\"entityField\\\": \\\"registration_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"vehiclesType\\\",\n" +
            "        \\\"entityField\\\": \\\"vehicle_type\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sku\\\",\n" +
            "        \\\"entityField\\\": \\\"vehicle_brand\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"identificationNumber\\\",\n" +
            "        \\\"entityField\\\": \\\"vehicle_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"vehicleManagementOffice\\\",\n" +
            "        \\\"entityField\\\": \\\"vehicle_place_name\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"totalAmountTaxNum\\\",\n" +
            "        \\\"entityField\\\": \\\"amount_with_tax\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"companyName\\\",\n" +
            "        \\\"entityField\\\": \\\"auctioneers_name\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"companyAddr\\\",\n" +
            "        \\\"entityField\\\": \\\"auctioneers_address\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"companyTaxNo\\\",\n" +
            "        \\\"entityField\\\": \\\"auctioneers_tax_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"companyBank\\\",\n" +
            "        \\\"entityField\\\": \\\"auctioneers_bank\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"companyBankNo\\\",\n" +
            "        \\\"entityField\\\": \\\"auctioneers_bank_info\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"marketName\\\",\n" +
            "        \\\"entityField\\\": \\\"used_car_market_name\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"marketTaxNo\\\",\n" +
            "        \\\"entityField\\\": \\\"used_car_market_tax_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"marketBank\\\",\n" +
            "        \\\"entityField\\\": \\\"used_car_market_bank\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"marketBankNo\\\",\n" +
            "        \\\"entityField\\\": \\\"used_car_market_bank_info\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"drawer\\\",\n" +
            "        \\\"entityField\\\": \\\"drawer\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"remark\\\",\n" +
            "        \\\"entityField\\\": \\\"remark\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceType\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_type\\\",\n" +
            "        \\\"defaultValue\\\": \\\"vs\\\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \\\"entityCode\\\": \\\"ticketVehicle\\\",\n" +
            "    \\\"mappingName\\\": \\\"机动车销售统一发票\\\",\n" +
            "    \\\"imageType\\\": \\\"2\\\",\n" +
            "    \\\"documentType\\\": \\\"50001001\\\",\n" +
            "    \\\"items\\\": [\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceCode\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_code\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceNo\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceType\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_type\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceCodeP\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_code_p\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceNoP\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_no_p\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sheetIndex\\\",\n" +
            "        \\\"entityField\\\": \\\"vehicle_sheet\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceTime\\\",\n" +
            "        \\\"entityField\\\": \\\"paper_drew_date\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"machineCode\\\",\n" +
            "        \\\"entityField\\\": \\\"machine_code\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"checkCode\\\",\n" +
            "        \\\"entityField\\\": \\\"check_code\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"cipherList\\\",\n" +
            "        \\\"entityField\\\": \\\"cipher_text\\\",\n" +
            "        \\\"expression\\\": \\\"#obj.getArray('cipherList') != null?T(org.apache.commons.lang3.StringUtils).join(#obj.getArray('cipherList')):null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"purchaserName\\\",\n" +
            "        \\\"entityField\\\": \\\"purchaser_name\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"purchaserTaxNo\\\",\n" +
            "        \\\"entityField\\\": \\\"purchaser_tax_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"totalAmountTaxNum\\\",\n" +
            "        \\\"entityField\\\": \\\"amount_with_tax\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"totalTax\\\",\n" +
            "        \\\"entityField\\\": \\\"tax_amount\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"totalAmount\\\",\n" +
            "        \\\"entityField\\\": \\\"amount_without_tax\\\",\n" +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sellerName\\\",\n" +
            "        \\\"entityField\\\": \\\"seller_name\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sellerTaxNo\\\",\n" +
            "        \\\"entityField\\\": \\\"seller_tax_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sellerAddr\\\",\n" +
            "        \\\"entityField\\\": \\\"seller_address\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sellerTel\\\",\n" +
            "        \\\"entityField\\\": \\\"seller_tel\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sellerBank\\\",\n" +
            "        \\\"entityField\\\": \\\"seller_bank_name\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sellerBankNo\\\",\n" +
            "        \\\"entityField\\\": \\\"seller_bank_account\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"drawer\\\",\n" +
            "        \\\"entityField\\\": \\\"drawer\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"remark\\\",\n" +
            "        \\\"entityField\\\": \\\"remark\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"invoiceTime\\\",\n" +
            "        \\\"entityField\\\": \\\"invoice_date\\\",\n" +
            "        \\\"expression\\\": \\\"#obj.getString('invoiceTime') != null && #obj.getString('invoiceTime').matches('^\\\\\\\\d{8}$') ? new java.text.SimpleDateFormat('yyyyMMdd').parse(#obj.getString('invoiceTime')).getTime() : null\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"purchaserIDCode\\\",\n" +
            "        \\\"entityField\\\": \\\"purchaser_id_code\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"vehiclesType\\\",\n" +
            "        \\\"entityField\\\": \\\"vehicle_type\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"sku\\\",\n" +
            "        \\\"entityField\\\": \\\"vehicle_brand\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"identificationNumber\\\",\n" +
            "        \\\"entityField\\\": \\\"vehicle_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"origin\\\",\n" +
            "        \\\"entityField\\\": \\\"production_area\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"qualifiedNumber\\\",\n" +
            "        \\\"entityField\\\": \\\"certification_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"bookNo\\\",\n" +
            "        \\\"entityField\\\": \\\"import_certificate_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"checkNo\\\",\n" +
            "        \\\"entityField\\\": \\\"commodity_inspection_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"engineNumber\\\",\n" +
            "        \\\"entityField\\\": \\\"engine_no\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"codeOfTheCompetentTaxAuthority\\\",\n" +
            "        \\\"entityField\\\": \\\"charge_tax_authority_code\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"nameOfTheCompetentTaxAuthority\\\",\n" +
            "        \\\"entityField\\\": \\\"charge_tax_authority_name\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"taxPaidProof\\\",\n" +
            "        \\\"entityField\\\": \\\"tax_paid_proof\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"tonnage\\\",\n" +
            "        \\\"entityField\\\": \\\"tonnage\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"max_capacity\\\",\n" +
            "        \\\"entityField\\\": \\\"max_capacity\\\"\n" +
            "      },\n" +
            "      {\n" +
            "        \\\"srcField\\\": \\\"qrcode\\\",\n" +
            "        \\\"entityField\\\": \\\"qrcode\\\"\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "]\", \"F1295238504688762882S\": \"1\", \"F1295238505305325570S\": \"1\", \"F1295238506429399042L\": 4464073942242932093, \"F1295238507134042114L\": 1618579280792, \"F1295238508010651649L\": 1619769521531, \"F1295238509587709953L\": 4603688623062720515, \"F1295238510393016321L\": 4603688623062720515, \"F1295238511240265729S\": \"柳红彬\", \"F1295238512058155010S\": \"柳红彬\", \"F1295238512855072769S\": \"1\", \"F1295238513526161410S\": \"CQP\"}";
    @Test
    public void test() throws JsonProcessingException {
        List<Object> objects = OriginalEntityUtils.attributesToList(str);
        Assert.assertNotNull(objects);
    }
}
