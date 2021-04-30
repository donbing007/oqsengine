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
//    String str = "{" +
//            "\t\"F1295238501979242498S\": \"ticket_compare_config\"," +
//            "\t\"F1295238502528696321S\": \"单证字段比较配置\"," +
//            "\t\"F1295238503136870402S\": \"[{\\\t\\\"entityCode\\\": \\\"ticketInvoice\\\",\\\t\\\"checkType\\\": \\\"1\\\",\\\t\\\"items\\\": [\\\"invoice_type\\\", \\\"invoice_sheet\\\", \\\"invoice_code\\\", \\\"invoice_no\\\", \\\"invoice_date\\\", \\\"purchaser_name\\\", \\\"purchaser_tax_no\\\", \\\"seller_name\\\", \\\"seller_tax_no\\\", \\\"amount_without_tax\\\", \\\"tax_amount\\\", \\\"amount_with_tax\\\", \\\"check_code\\\", \\\"machine_code\\\", \\\"cipher_text\\\"]\}, {\\\t\\\"entityCode\\\": \\\"ticketQuota\\\",\\\t\\\"checkType\\\": \\\"1\\\",\\\t\\\"items\\\": [\\\"invoice_no\\\", \\\"invoice_code\\\", \\\"amount_with_tax\\\"]\}, {\\\t\\\"entityCode\\\": \\\"ticketMachine\\\",\\\t\\\"checkType\\\": \\\"1\\\",\\\t\\\"items\\\": [\\\"invoice_no\\\", \\\"invoice_code\\\", \\\"invoice_sheet\\\", \\\"invoice_date\\\", \\\"amount_with_tax\\\", \\\"tax_amount\\\", \\\"amount_without_tax\\\", \\\"purchaser_name\\\", \\\"purchaser_tax_no\\\", \\\"seller_name\\\", \\\"seller_tax_no\\\", \\\"check_code\\\", \\\"machine_code\\\", \\\"cipher_text\\\"]\}, {\\\t\\\"entityCode\\\": \\\"ticketPlane\\\",\\\t\\\"checkType\\\": \\\"1\\\",\\\t\\\"items\\\": [\\\"name_of_passenger\\\", \\\"id_no\\\", \\\"e_ticket_no\\\", \\\"check_code\\\", \\\"issued_by\\\", \\\"date_of_issue\\\", \\\"insurance\\\", \\\"fare\\\", \\\"caac_development_fund\\\", \\\"fuel_surcharge\\\", \\\"total\\\", \\\"tax\\\"]\}, {\\\t\\\"entityCode\\\": \\\"ticketTrain\\\",\\\t\\\"checkType\\\": \\\"1\\\",\\\t\\\"items\\\": [\\\"name\\\", \\\"trains\\\", \\\"start_station\\\", \\\"end_station\\\", \\\"start_date\\\", \\\"start_time\\\", \\\"seat\\\", \\\"seat_type\\\", \\\"certificate_no\\\", \\\"qrcode\\\", \\\"amount_with_tax\\\"]\}, {\\\t\\\"entityCode\\\": \\\"ticketTaxi\\\",\\\t\\\"checkType\\\": \\\"1\\\",\\\t\\\"items\\\": [\\\"invoice_no\\\", \\\"invoice_code\\\", \\\"start_date\\\", \\\"get_on_time\\\", \\\"mileage\\\", \\\"get_off_time\\\", \\\"name\\\", \\\"amount_with_tax\\\"]\}, {\\\t\\\"entityCode\\\": \\\"ticketUsedCar\\\",\\\t\\\"checkType\\\": \\\"1\\\",\\\t\\\"items\\\": [\\\"invoice_no\\\", \\\"invoice_code\\\", \\\"invoice_date\\\", \\\"amount_with_tax\\\", \\\"registration_no\\\", \\\"vehicle_no\\\", \\\"vehicle_brand\\\", \\\"seller_name\\\", \\\"seller_tax_no\\\", \\\"purchaser_name\\\", \\\"purchaser_tax_no\\\", \\\"auctioneers_name\\\", \\\"auctioneers_address\\\", \\\"auctioneers_tax_no\\\", \\\"auctioneers_bank_info\\\", \\\"auctioneers_tel\\\", \\\"used_car_market_name\\\", \\\"used_car_market_address\\\", \\\"used_car_market_tax_no\\\", \\\"used_car_market_bank_info\\\", \\\"used_car_market_tel\\\", \\\"car_number\\\", \\\"dq_code\\\", \\\"dp_name\\\"]\}, {\\\t\\\"entityCode\\\": \\\"ticketBus\\\",\\\t\\\"checkType\\\": \\\"1\\\",\\\t\\\"items\\\": [\\\"invoice_no\\\", \\\"invoice_code\\\", \\\"trains\\\", \\\"start_station\\\", \\\"end_station\\\", \\\"start_date\\\", \\\"start_time\\\", \\\"seat\\\", \\\"carrier\\\", \\\"name\\\", \\\"amount_with_tax\\\", \\\"purchaser_name\\\", \\\"purchaser_tax_no\\\"]\}, {\\\t\\\"entityCode\\\": \\\"ticketToll\\\",\\\t\\\"checkType\\\": \\\"1\\\",\\\t\\\"items\\\": [\\\"invoice_no\\\", \\\"invoice_code\\\", \\\"exit_place\\\", \\\"entrance_place\\\", \\\"start_date\\\", \\\"payment\\\", \\\"vehicles_type\\\", \\\"vehicles_weight\\\", \\\"toll_limit\\\", \\\"amount_with_tax\\\"]\}, {\\\t\\\"entityCode\\\": \\\"ticketVehicle\\\",\\\t\\\"checkType\\\": \\\"1\\\",\\\t\\\"items\\\": [\\\"vehicle_sheet\\\", \\\"invoice_no\\\", \\\"invoice_code\\\", \\\"invoice_date\\\", \\\"seller_name\\\", \\\"seller_tax_no\\\", \\\"amount_without_tax\\\", \\\"tax_amount\\\", \\\"amount_with_tax\\\", \\\"vehicle_type\\\", \\\"vehicle_brand\\\", \\\"production_area\\\", \\\"engine_no\\\", \\\"commodity_inspection_no\\\", \\\"certification_no\\\", \\\"vehicle_no\\\", \\\"import_certificate_no\\\", \\\"charge_tax_authority_code\\\", \\\"charge_tax_authority_name\\\", \\\"tax_paid_proof\\\", \\\"tonnage\\\", \\\"max_capacity\\\", \\\"dq_code\\\", \\\"dq_name\\\"]\}]\"," +
//            "\t\"F1295238504688762882S\": \"1\"," +
//            "\t\"F1295238505305325570S\": \"1\"," +
//            "\t\"F1295238506429399042L\": 4464073942242932093," +
//            "\t\"F1295238507134042114L\": 1618573382435," +
//            "\t\"F1295238508010651649L\": 1618573382435," +
//            "\t\"F1295238509587709953L\": 4603688623062720515," +
//            "\t\"F1295238510393016321L\": 4603688623062720515," +
//            "\t\"F1295238511240265729S\": \"柳红彬\"," +
//            "\t\"F1295238512058155010S\": \"柳红彬\"," +
//            "\t\"F1295238512855072769S\": \"1\"," +
//            "\t\"F1295238513526161410S\": \"CQP\"" +
//            "}";
//
//    String str2 = "{\"tenant_id\":\"4567581588943585293\",\"create_user_id\":\"4612820548148789257\",\"dict_desc\":\"小组角色关系\",\"create_user_name\":\"柳红彬\",\"create_time\":\"1598350807168\",\"update_user_name\":\"柳红彬\",\"is_default\":\"1\",\"dict_code\":\"team_relation\",\"update_time\":\"1598350807168\",\"update_user_id\":\"4612820548148789257\",\"enable\":\"1\",\"dict_value\":\"[\  {\    \\\"itemCode\\\": \\\"checkScan\\\",\    \\\"itemValue\\\": {\      \\\"4418255883950761701\\\": \\\"4418255815205528906\\\",\      \\\"4418255815205528921\\\": \\\"4418255815205528921\\\"\    }\  },\  {\    \\\"itemCode\\\": \\\"leaderScan\\\",\    \\\"itemValue\\\": {\      \\\"4418255815205528921\\\": \\\"4418255883950761701,4418255815205528906\\\"\    }\  }\]\",\"id\":\"1298203494372605953\",\"delete_flag\":\"1\",\"tenant\":null,\"tenant_code\":\"CQP\"}  ";

    String str3 = "{\"F1295238501979242498S\": \"discern_mapping_config\", \"F1295238502528696321S\": \"识别映射配置\", \"F1295238503136870402S\": \"[" +
            "  {" +
            "    \\\"entityCode\\\": \\\"ticketInvoice\\\"," +
            "    \\\"mappingName\\\": \\\"增值税发票\\\"," +
            "    \\\"imageType\\\": \\\"2\\\"," +
            "    \\\"documentType\\\": \\\"10001001,10001002,10001003,20001001,20001002,30001001,30001002,30001003,40001001,50001017,50001018\\\"," +
            "    \\\"items\\\": [" +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceCode\\\"," +
            "        \\\"entityField\\\": \\\"invoice_code\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceNo\\\"," +
            "        \\\"entityField\\\": \\\"invoice_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceType\\\"," +
            "        \\\"entityField\\\": \\\"invoice_type\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceTime\\\"," +
            "        \\\"entityField\\\": \\\"paper_drew_date\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"checkCode\\\"," +
            "        \\\"entityField\\\": \\\"check_code\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sheetIndex\\\"," +
            "        \\\"entityField\\\": \\\"invoice_sheet\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"cipherList\\\"," +
            "        \\\"entityField\\\": \\\"cipher_text\\\"," +
            "        \\\"expression\\\": \\\"#obj.getArray('cipherList') != null?T(org.apache.commons.lang3.StringUtils).join(#obj.getArray('cipherList')):null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"purchaserName\\\"," +
            "        \\\"entityField\\\": \\\"purchaser_name\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"purchaserTaxNo\\\"," +
            "        \\\"entityField\\\": \\\"purchaser_tax_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"purchaserAddr\\\"," +
            "        \\\"entityField\\\": \\\"purchaser_address\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"purchaserTel\\\"," +
            "        \\\"entityField\\\": \\\"purchaser_tel\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"purchaserBank\\\"," +
            "        \\\"entityField\\\": \\\"purchaser_bank_name\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"purchaserBankNo\\\"," +
            "        \\\"entityField\\\": \\\"purchaser_bank_account\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"totalAmountTaxNum\\\"," +
            "        \\\"entityField\\\": \\\"amount_with_tax\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"totalTax\\\"," +
            "        \\\"entityField\\\": \\\"tax_amount\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"totalAmount\\\"," +
            "        \\\"entityField\\\": \\\"amount_without_tax\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"machineCode\\\"," +
            "        \\\"entityField\\\": \\\"machine_code\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sellerName\\\"," +
            "        \\\"entityField\\\": \\\"seller_name\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sellerTaxNo\\\"," +
            "        \\\"entityField\\\": \\\"seller_tax_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sellerAddr\\\"," +
            "        \\\"entityField\\\": \\\"seller_address\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sellerTel\\\"," +
            "        \\\"entityField\\\": \\\"seller_tel\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sellerBank\\\"," +
            "        \\\"entityField\\\": \\\"seller_bank_name\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sellerBankNo\\\"," +
            "        \\\"entityField\\\": \\\"seller_bank_account\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"payee\\\"," +
            "        \\\"entityField\\\": \\\"payee\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"recheck\\\"," +
            "        \\\"entityField\\\": \\\"recheck\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"drawer\\\"," +
            "        \\\"entityField\\\": \\\"drawer\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"remark\\\"," +
            "        \\\"entityField\\\": \\\"remark\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"is_sales_list\\\"," +
            "        \\\"entityField\\\": \\\"is_sales_list\\\"," +
            "        \\\"expression\\\": \\\"#obj.getString('invoiceDetails') != null && #obj.getString('invoiceDetails').contains('销货清单') ?'1':'0'\\\"," +
            "        \\\"defaultValue\\\": \\\"0\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceTime\\\"," +
            "        \\\"entityField\\\": \\\"invoice_date\\\"," +
            "        \\\"expression\\\": \\\"#obj.getString('invoiceTime') != null && #obj.getString('invoiceTime').matches('^\\\\\\\\d{8}$') ? new java.text.SimpleDateFormat('yyyyMMdd').parse(#obj.getString('invoiceTime')).getTime() : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"stamper\\\"," +
            "        \\\"entityField\\\": \\\"is_stamper\\\"," +
            "        \\\"expression\\\": \\\"#obj.getString('stamper') != null && #obj.getString('stamper') != '' ?'1':'0'\\\"," +
            "        \\\"defaultValue\\\": \\\"0\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"qrcode\\\"," +
            "        \\\"entityField\\\": \\\"qrcode\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceCodeP\\\"," +
            "        \\\"entityField\\\": \\\"invoice_code_p\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceNoP\\\"," +
            "        \\\"entityField\\\": \\\"invoice_no_p\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceDetails\\\"," +
            "        \\\"entityField\\\": \\\"items\\\"," +
            "        \\\"mappingEntityCode\\\": \\\"ticketInvoiceDetail\\\"" +
            "      }" +
            "    ]" +
            "  }," +
            "  {" +
            "    \\\"entityCode\\\": \\\"ticketInvoiceDetail\\\"," +
            "    \\\"mappingName\\\": \\\"增值税发票明细\\\"," +
            "    \\\"items\\\": [" +
            "      {" +
            "        \\\"srcField\\\": \\\"name\\\"," +
            "        \\\"entityField\\\": \\\"cargo_name\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"specification\\\"," +
            "        \\\"entityField\\\": \\\"item_spec\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"unit\\\"," +
            "        \\\"entityField\\\": \\\"quantity_unit\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"quantity\\\"," +
            "        \\\"entityField\\\": \\\"quantity\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,27}(\\\\\\\\.\\\\\\\\d{0,15})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"price\\\"," +
            "        \\\"entityField\\\": \\\"unit_price\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,27}(\\\\\\\\.\\\\\\\\d{0,15})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"total\\\"," +
            "        \\\"entityField\\\": \\\"amount_without_tax\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"taxRate\\\"," +
            "        \\\"entityField\\\": \\\"tax_rate\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"tax\\\"," +
            "        \\\"entityField\\\": \\\"tax_amount\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"" +
            "      }" +
            "    ]" +
            "  }," +
            "  {" +
            "    \\\"entityCode\\\": \\\"ticketMachine\\\"," +
            "    \\\"mappingName\\\": \\\"通用机打发票\\\"," +
            "    \\\"imageType\\\": \\\"2\\\"," +
            "    \\\"documentType\\\": \\\"50001002\\\"," +
            "    \\\"items\\\": [" +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceCode\\\"," +
            "        \\\"entityField\\\": \\\"invoice_code\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceNo\\\"," +
            "        \\\"entityField\\\": \\\"invoice_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceType\\\"," +
            "        \\\"entityField\\\": \\\"invoice_type\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceTime\\\"," +
            "        \\\"entityField\\\": \\\"paper_drew_date\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"checkCode\\\"," +
            "        \\\"entityField\\\": \\\"check_code\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sheetIndex\\\"," +
            "        \\\"entityField\\\": \\\"invoice_sheet\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"cipherList\\\"," +
            "        \\\"entityField\\\": \\\"cipher_text\\\"," +
            "        \\\"expression\\\": \\\"#obj.getArray('cipherList') != null?T(org.apache.commons.lang3.StringUtils).join(#obj.getArray('cipherList')):null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"purchaserName\\\"," +
            "        \\\"entityField\\\": \\\"purchaser_name\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"purchaserTaxNo\\\"," +
            "        \\\"entityField\\\": \\\"purchaser_tax_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"purchaserAddr\\\"," +
            "        \\\"entityField\\\": \\\"purchaser_address\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"purchaserTel\\\"," +
            "        \\\"entityField\\\": \\\"purchaser_tel\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"purchaserBank\\\"," +
            "        \\\"entityField\\\": \\\"purchaser_bank_name\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"purchaserBankNo\\\"," +
            "        \\\"entityField\\\": \\\"purchaser_bank_account\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"totalAmountTaxNum\\\"," +
            "        \\\"entityField\\\": \\\"amount_with_tax\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"totalTax\\\"," +
            "        \\\"entityField\\\": \\\"tax_amount\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"totalAmount\\\"," +
            "        \\\"entityField\\\": \\\"amount_without_tax\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"machineCode\\\"," +
            "        \\\"entityField\\\": \\\"machine_code\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sellerName\\\"," +
            "        \\\"entityField\\\": \\\"seller_name\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sellerTaxNo\\\"," +
            "        \\\"entityField\\\": \\\"seller_tax_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sellerAddr\\\"," +
            "        \\\"entityField\\\": \\\"seller_address\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sellerTel\\\"," +
            "        \\\"entityField\\\": \\\"seller_tel\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sellerBank\\\"," +
            "        \\\"entityField\\\": \\\"seller_bank_name\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sellerBankNo\\\"," +
            "        \\\"entityField\\\": \\\"seller_bank_account\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"payee\\\"," +
            "        \\\"entityField\\\": \\\"payee\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"recheck\\\"," +
            "        \\\"entityField\\\": \\\"recheck\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"drawer\\\"," +
            "        \\\"entityField\\\": \\\"drawer\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"remark\\\"," +
            "        \\\"entityField\\\": \\\"remark\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"is_sales_list\\\"," +
            "        \\\"entityField\\\": \\\"is_sales_list\\\"," +
            "        \\\"expression\\\": \\\"#obj.getString('invoiceDetails') != null && #obj.getString('invoiceDetails').contains('销货清单') ?'1':'0'\\\"," +
            "        \\\"defaultValue\\\": \\\"0\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceTime\\\"," +
            "        \\\"entityField\\\": \\\"invoice_date\\\"," +
            "        \\\"expression\\\": \\\"#obj.getString('invoiceTime') != null && #obj.getString('invoiceTime').matches('^\\\\\\\\d{8}$') ? new java.text.SimpleDateFormat('yyyyMMdd').parse(#obj.getString('invoiceTime')).getTime() : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceDetails\\\"," +
            "        \\\"entityField\\\": \\\"items\\\"," +
            "        \\\"mappingEntityCode\\\": \\\"ticketMachineDetail\\\"" +
            "      }" +
            "    ]" +
            "  }," +
            "  {" +
            "    \\\"entityCode\\\": \\\"ticketMachineDetail\\\"," +
            "    \\\"mappingName\\\": \\\"通用机打发票明细\\\"," +
            "    \\\"items\\\": [" +
            "      {" +
            "        \\\"srcField\\\": \\\"name\\\"," +
            "        \\\"entityField\\\": \\\"cargo_name\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"specification\\\"," +
            "        \\\"entityField\\\": \\\"item_spec\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"unit\\\"," +
            "        \\\"entityField\\\": \\\"quantity_unit\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"quantity\\\"," +
            "        \\\"entityField\\\": \\\"quantity\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,27}(\\\\\\\\.\\\\\\\\d{0,15})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"price\\\"," +
            "        \\\"entityField\\\": \\\"unit_price\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,27}(\\\\\\\\.\\\\\\\\d{0,15})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"total\\\"," +
            "        \\\"entityField\\\": \\\"amount_without_tax\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"taxRate\\\"," +
            "        \\\"entityField\\\": \\\"tax_rate\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"tax\\\"," +
            "        \\\"entityField\\\": \\\"tax_amount\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"" +
            "      }" +
            "    ]" +
            "  }," +
            "  {" +
            "    \\\"entityCode\\\": \\\"ticketPlane\\\"," +
            "    \\\"mappingName\\\": \\\"飞机票\\\"," +
            "    \\\"imageType\\\": \\\"2\\\"," +
            "    \\\"documentType\\\": \\\"50001009\\\"," +
            "    \\\"items\\\": [" +
            "      {" +
            "        \\\"srcField\\\": \\\"nameOfPassenger\\\"," +
            "        \\\"entityField\\\": \\\"name_of_passenger\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"IDNo\\\"," +
            "        \\\"entityField\\\": \\\"id_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"endorsements\\\"," +
            "        \\\"entityField\\\": \\\"endorsements\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"serialNo\\\"," +
            "        \\\"entityField\\\": \\\"serial_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"eTicketNo\\\"," +
            "        \\\"entityField\\\": \\\"e_ticket_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"checkCode\\\"," +
            "        \\\"entityField\\\": \\\"check_code\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"information\\\"," +
            "        \\\"entityField\\\": \\\"information\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"agentCode\\\"," +
            "        \\\"entityField\\\": \\\"agent_code\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"issuedBy\\\"," +
            "        \\\"entityField\\\": \\\"issued_by\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"dateOfIssue\\\"," +
            "        \\\"entityField\\\": \\\"date_of_issue\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"insurance\\\"," +
            "        \\\"entityField\\\": \\\"insurance\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"fare\\\"," +
            "        \\\"entityField\\\": \\\"fare\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"caacDevelopmentFund\\\"," +
            "        \\\"entityField\\\": \\\"caac_development_fund\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"fuelSurcharge\\\"," +
            "        \\\"entityField\\\": \\\"fuel_surcharge\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"tax\\\"," +
            "        \\\"entityField\\\": \\\"tax\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"total\\\"," +
            "        \\\"entityField\\\": \\\"total\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"flights\\\"," +
            "        \\\"entityField\\\": \\\"items\\\"," +
            "        \\\"mappingEntityCode\\\": \\\"ticketPlaneDetail\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceType\\\"," +
            "        \\\"entityField\\\": \\\"invoice_type\\\"," +
            "        \\\"defaultValue\\\": \\\"p\\\"" +
            "      }" +
            "    ]" +
            "  }," +
            "  {" +
            "    \\\"entityCode\\\": \\\"ticketPlaneDetail\\\"," +
            "    \\\"mappingName\\\": \\\"飞机票明细\\\"," +
            "    \\\"items\\\": [" +
            "      {" +
            "        \\\"srcField\\\": \\\"summary\\\"," +
            "        \\\"entityField\\\": \\\"summary\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"carrier\\\"," +
            "        \\\"entityField\\\": \\\"carrier\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"from\\\"," +
            "        \\\"entityField\\\": \\\"plane_from\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"to\\\"," +
            "        \\\"entityField\\\": \\\"plane_to\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"flight\\\"," +
            "        \\\"entityField\\\": \\\"flight\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"date\\\"," +
            "        \\\"entityField\\\": \\\"plane_date\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"time\\\"," +
            "        \\\"entityField\\\": \\\"plane_time\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"class\\\"," +
            "        \\\"entityField\\\": \\\"seat_class\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"fareBasis\\\"," +
            "        \\\"entityField\\\": \\\"fare_basis\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"notValidBefore\\\"," +
            "        \\\"entityField\\\": \\\"not_valid_before\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"notValidAfter\\\"," +
            "        \\\"entityField\\\": \\\"not_valid_after\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"allow\\\"," +
            "        \\\"entityField\\\": \\\"allow\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"extra\\\"," +
            "        \\\"entityField\\\": \\\"extra\\\"" +
            "      }" +
            "    ]" +
            "  }," +
            "  {" +
            "    \\\"entityCode\\\": \\\"ticketTrain\\\"," +
            "    \\\"mappingName\\\": \\\"火车票\\\"," +
            "    \\\"imageType\\\": \\\"2\\\"," +
            "    \\\"documentType\\\": \\\"50001010\\\"," +
            "    \\\"items\\\": [" +
            "      {" +
            "        \\\"srcField\\\": \\\"trains\\\"," +
            "        \\\"entityField\\\": \\\"trains\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"startStation\\\"," +
            "        \\\"entityField\\\": \\\"start_station\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"endStation\\\"," +
            "        \\\"entityField\\\": \\\"end_station\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"date\\\"," +
            "        \\\"entityField\\\": \\\"start_date\\\"," +
            "        \\\"expression\\\": \\\"#obj.getString('date') != null && #obj.getString('date').matches('^\\\\\\\\d{8}$') ? new java.text.SimpleDateFormat('yyyyMMdd').parse(#obj.getString('date')).getTime() : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"time\\\"," +
            "        \\\"entityField\\\": \\\"start_time\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"seat\\\"," +
            "        \\\"entityField\\\": \\\"seat\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"seatType\\\"," +
            "        \\\"entityField\\\": \\\"seat_type\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"price\\\"," +
            "        \\\"entityField\\\": \\\"amount_with_tax\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"certificateNo\\\"," +
            "        \\\"entityField\\\": \\\"certificate_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"name\\\"," +
            "        \\\"entityField\\\": \\\"name\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"no\\\"," +
            "        \\\"entityField\\\": \\\"invoice_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"qrcode\\\"," +
            "        \\\"entityField\\\": \\\"qrcode\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceType\\\"," +
            "        \\\"entityField\\\": \\\"invoice_type\\\"," +
            "        \\\"defaultValue\\\": \\\"t\\\"" +
            "      }" +
            "    ]" +
            "  }," +
            "  {" +
            "    \\\"entityCode\\\": \\\"ticketTaxi\\\"," +
            "    \\\"mappingName\\\": \\\"出租车票\\\"," +
            "    \\\"imageType\\\": \\\"2\\\"," +
            "    \\\"documentType\\\": \\\"50001011\\\"," +
            "    \\\"items\\\": [" +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceCode\\\"," +
            "        \\\"entityField\\\": \\\"invoice_code\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceNo\\\"," +
            "        \\\"entityField\\\": \\\"invoice_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"totalAmount\\\"," +
            "        \\\"entityField\\\": \\\"amount_with_tax\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"date\\\"," +
            "        \\\"entityField\\\": \\\"start_date\\\"," +
            "        \\\"expression\\\": \\\"#obj.getString('date') != null && #obj.getString('date').matches('^\\\\\\\\d{8}$') ? new java.text.SimpleDateFormat('yyyyMMdd').parse(#obj.getString('date')).getTime() : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"getOnTime\\\"," +
            "        \\\"entityField\\\": \\\"get_on_time\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"mileage\\\"," +
            "        \\\"entityField\\\": \\\"mileage\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"getOffTime\\\"," +
            "        \\\"entityField\\\": \\\"get_off_time\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"place\\\"," +
            "        \\\"entityField\\\": \\\"place\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceType\\\"," +
            "        \\\"entityField\\\": \\\"invoice_type\\\"," +
            "        \\\"defaultValue\\\": \\\"taxi\\\"" +
            "      }" +
            "    ]" +
            "  }," +
            "  {" +
            "    \\\"entityCode\\\": \\\"ticketQuota\\\"," +
            "    \\\"mappingName\\\": \\\"定额发票\\\"," +
            "    \\\"imageType\\\": \\\"2\\\"," +
            "    \\\"documentType\\\": \\\"50001013\\\"," +
            "    \\\"items\\\": [" +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceCode\\\"," +
            "        \\\"entityField\\\": \\\"invoice_code\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceNo\\\"," +
            "        \\\"entityField\\\": \\\"invoice_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"totalAmount\\\"," +
            "        \\\"entityField\\\": \\\"amount_with_tax\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceType\\\"," +
            "        \\\"entityField\\\": \\\"invoice_type\\\"," +
            "        \\\"defaultValue\\\": \\\"quota\\\"" +
            "      }" +
            "    ]" +
            "  }," +
            "  {" +
            "    \\\"entityCode\\\": \\\"ticketToll\\\"," +
            "    \\\"mappingName\\\": \\\"过路费\\\"," +
            "    \\\"imageType\\\": \\\"2\\\"," +
            "    \\\"documentType\\\": \\\"50001014\\\"," +
            "    \\\"items\\\": [" +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceCode\\\"," +
            "        \\\"entityField\\\": \\\"invoice_code\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceNo\\\"," +
            "        \\\"entityField\\\": \\\"invoice_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"totalAmount\\\"," +
            "        \\\"entityField\\\": \\\"amount_with_tax\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"date\\\"," +
            "        \\\"entityField\\\": \\\"start_date\\\"," +
            "        \\\"expression\\\": \\\"#obj.getString('date') != null && #obj.getString('date').matches('^\\\\\\\\d{8}$') ? new java.text.SimpleDateFormat('yyyyMMdd').parse(#obj.getString('date')).getTime() : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"time\\\"," +
            "        \\\"entityField\\\": \\\"time\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"entrance\\\"," +
            "        \\\"entityField\\\": \\\"entrance_place\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"exit\\\"," +
            "        \\\"entityField\\\": \\\"exit_place\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"payment\\\"," +
            "        \\\"entityField\\\": \\\"payment\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"vehiclesType\\\"," +
            "        \\\"entityField\\\": \\\"vehicles_type\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"vehiclesWeight\\\"," +
            "        \\\"entityField\\\": \\\"vehicles_weight\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"limit\\\"," +
            "        \\\"entityField\\\": \\\"toll_limit\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceType\\\"," +
            "        \\\"entityField\\\": \\\"invoice_type\\\"," +
            "        \\\"defaultValue\\\": \\\"toll\\\"" +
            "      }" +
            "    ]" +
            "  }," +
            "  {" +
            "    \\\"entityCode\\\": \\\"ticketBus\\\"," +
            "    \\\"mappingName\\\": \\\"公交车票\\\"," +
            "    \\\"imageType\\\": \\\"2\\\"," +
            "    \\\"documentType\\\": \\\"50001016\\\"," +
            "    \\\"items\\\": [" +
            "      {" +
            "        \\\"srcField\\\": \\\"companyName\\\"," +
            "        \\\"entityField\\\": \\\"purchaser_name\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"companyTaxNo\\\"," +
            "        \\\"entityField\\\": \\\"purhcaser_tax_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceCode\\\"," +
            "        \\\"entityField\\\": \\\"invoice_code\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceNo\\\"," +
            "        \\\"entityField\\\": \\\"invoice_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"totalAmount\\\"," +
            "        \\\"entityField\\\": \\\"amount_with_tax\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"trains\\\"," +
            "        \\\"entityField\\\": \\\"trains\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"startStation\\\"," +
            "        \\\"entityField\\\": \\\"start_station\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"endStation\\\"," +
            "        \\\"entityField\\\": \\\"end_station\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"date\\\"," +
            "        \\\"entityField\\\": \\\"start_date\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"time\\\"," +
            "        \\\"entityField\\\": \\\"start_time\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"seat\\\"," +
            "        \\\"entityField\\\": \\\"seat\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"name\\\"," +
            "        \\\"entityField\\\": \\\"name\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceType\\\"," +
            "        \\\"entityField\\\": \\\"invoice_type\\\"," +
            "        \\\"defaultValue\\\": \\\"bus\\\"" +
            "      }" +
            "    ]" +
            "  }," +
            "  {" +
            "    \\\"entityCode\\\": \\\"ticketUsedCar\\\"," +
            "    \\\"mappingName\\\": \\\"二手车销售统一发票\\\"," +
            "    \\\"imageType\\\": \\\"2\\\"," +
            "    \\\"documentType\\\": \\\"50001015\\\"," +
            "    \\\"items\\\": [" +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceNo\\\"," +
            "        \\\"entityField\\\": \\\"invoice_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceCode\\\"," +
            "        \\\"entityField\\\": \\\"invoice_code\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceTime\\\"," +
            "        \\\"entityField\\\": \\\"paper_drew_date\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceTime\\\"," +
            "        \\\"entityField\\\": \\\"invoice_date\\\"," +
            "        \\\"expression\\\": \\\"#obj.getString('invoiceTime') != null && #obj.getString('invoiceTime').matches('^\\\\\\\\d{8}$') ? new java.text.SimpleDateFormat('yyyyMMdd').parse(#obj.getString('invoiceTime')).getTime() : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceCodeP\\\"," +
            "        \\\"entityField\\\": \\\"invoice_code_p\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceNoP\\\"," +
            "        \\\"entityField\\\": \\\"invoice_no_p\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"qrcode\\\"," +
            "        \\\"entityField\\\": \\\"qrcode\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sheetIndex\\\"," +
            "        \\\"entityField\\\": \\\"invoice_sheet\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"cipherList\\\"," +
            "        \\\"entityField\\\": \\\"cipher_text\\\"," +
            "        \\\"expression\\\": \\\"#obj.getArray('cipherList') != null?T(org.apache.commons.lang3.StringUtils).join(#obj.getArray('cipherList')):null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"purchaserName\\\"," +
            "        \\\"entityField\\\": \\\"purchaser_name\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"purchaserIDCode\\\"," +
            "        \\\"entityField\\\": \\\"purchaser_tax_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"purchaserAddr\\\"," +
            "        \\\"entityField\\\": \\\"purchaser_address\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"purchaserTel\\\"," +
            "        \\\"entityField\\\": \\\"purchaser_tel\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sellerName\\\"," +
            "        \\\"entityField\\\": \\\"seller_name\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sellerIDCode\\\"," +
            "        \\\"entityField\\\": \\\"seller_tax_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sellerAddr\\\"," +
            "        \\\"entityField\\\": \\\"seller_address\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sellerBank\\\"," +
            "        \\\"entityField\\\": \\\"seller_bank_name\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sellerBankNo\\\"," +
            "        \\\"entityField\\\": \\\"seller_bank_account\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sellerTel\\\"," +
            "        \\\"entityField\\\": \\\"seller_tel\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"licensePlateNo\\\"," +
            "        \\\"entityField\\\": \\\"car_number\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"registerNo\\\"," +
            "        \\\"entityField\\\": \\\"registration_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"vehiclesType\\\"," +
            "        \\\"entityField\\\": \\\"vehicle_type\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sku\\\"," +
            "        \\\"entityField\\\": \\\"vehicle_brand\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"identificationNumber\\\"," +
            "        \\\"entityField\\\": \\\"vehicle_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"vehicleManagementOffice\\\"," +
            "        \\\"entityField\\\": \\\"vehicle_place_name\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"totalAmountTaxNum\\\"," +
            "        \\\"entityField\\\": \\\"amount_with_tax\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"companyName\\\"," +
            "        \\\"entityField\\\": \\\"auctioneers_name\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"companyAddr\\\"," +
            "        \\\"entityField\\\": \\\"auctioneers_address\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"companyTaxNo\\\"," +
            "        \\\"entityField\\\": \\\"auctioneers_tax_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"companyBank\\\"," +
            "        \\\"entityField\\\": \\\"auctioneers_bank\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"companyBankNo\\\"," +
            "        \\\"entityField\\\": \\\"auctioneers_bank_info\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"marketName\\\"," +
            "        \\\"entityField\\\": \\\"used_car_market_name\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"marketTaxNo\\\"," +
            "        \\\"entityField\\\": \\\"used_car_market_tax_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"marketBank\\\"," +
            "        \\\"entityField\\\": \\\"used_car_market_bank\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"marketBankNo\\\"," +
            "        \\\"entityField\\\": \\\"used_car_market_bank_info\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"drawer\\\"," +
            "        \\\"entityField\\\": \\\"drawer\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"remark\\\"," +
            "        \\\"entityField\\\": \\\"remark\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceType\\\"," +
            "        \\\"entityField\\\": \\\"invoice_type\\\"," +
            "        \\\"defaultValue\\\": \\\"vs\\\"" +
            "      }" +
            "    ]" +
            "  }," +
            "  {" +
            "    \\\"entityCode\\\": \\\"ticketVehicle\\\"," +
            "    \\\"mappingName\\\": \\\"机动车销售统一发票\\\"," +
            "    \\\"imageType\\\": \\\"2\\\"," +
            "    \\\"documentType\\\": \\\"50001001\\\"," +
            "    \\\"items\\\": [" +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceCode\\\"," +
            "        \\\"entityField\\\": \\\"invoice_code\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceNo\\\"," +
            "        \\\"entityField\\\": \\\"invoice_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceType\\\"," +
            "        \\\"entityField\\\": \\\"invoice_type\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceCodeP\\\"," +
            "        \\\"entityField\\\": \\\"invoice_code_p\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceNoP\\\"," +
            "        \\\"entityField\\\": \\\"invoice_no_p\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sheetIndex\\\"," +
            "        \\\"entityField\\\": \\\"vehicle_sheet\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceTime\\\"," +
            "        \\\"entityField\\\": \\\"paper_drew_date\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"machineCode\\\"," +
            "        \\\"entityField\\\": \\\"machine_code\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"checkCode\\\"," +
            "        \\\"entityField\\\": \\\"check_code\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"cipherList\\\"," +
            "        \\\"entityField\\\": \\\"cipher_text\\\"," +
            "        \\\"expression\\\": \\\"#obj.getArray('cipherList') != null?T(org.apache.commons.lang3.StringUtils).join(#obj.getArray('cipherList')):null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"purchaserName\\\"," +
            "        \\\"entityField\\\": \\\"purchaser_name\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"purchaserTaxNo\\\"," +
            "        \\\"entityField\\\": \\\"purchaser_tax_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"totalAmountTaxNum\\\"," +
            "        \\\"entityField\\\": \\\"amount_with_tax\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"totalTax\\\"," +
            "        \\\"entityField\\\": \\\"tax_amount\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"totalAmount\\\"," +
            "        \\\"entityField\\\": \\\"amount_without_tax\\\"," +
            "        \\\"expression\\\": \\\"#value != null && #value.matches('^-?\\\\\\\\d{1,18}(\\\\\\\\.\\\\\\\\d{0,6})?$') ? #value : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sellerName\\\"," +
            "        \\\"entityField\\\": \\\"seller_name\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sellerTaxNo\\\"," +
            "        \\\"entityField\\\": \\\"seller_tax_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sellerAddr\\\"," +
            "        \\\"entityField\\\": \\\"seller_address\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sellerTel\\\"," +
            "        \\\"entityField\\\": \\\"seller_tel\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sellerBank\\\"," +
            "        \\\"entityField\\\": \\\"seller_bank_name\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sellerBankNo\\\"," +
            "        \\\"entityField\\\": \\\"seller_bank_account\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"drawer\\\"," +
            "        \\\"entityField\\\": \\\"drawer\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"remark\\\"," +
            "        \\\"entityField\\\": \\\"remark\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"invoiceTime\\\"," +
            "        \\\"entityField\\\": \\\"invoice_date\\\"," +
            "        \\\"expression\\\": \\\"#obj.getString('invoiceTime') != null && #obj.getString('invoiceTime').matches('^\\\\\\\\d{8}$') ? new java.text.SimpleDateFormat('yyyyMMdd').parse(#obj.getString('invoiceTime')).getTime() : null\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"purchaserIDCode\\\"," +
            "        \\\"entityField\\\": \\\"purchaser_id_code\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"vehiclesType\\\"," +
            "        \\\"entityField\\\": \\\"vehicle_type\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"sku\\\"," +
            "        \\\"entityField\\\": \\\"vehicle_brand\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"identificationNumber\\\"," +
            "        \\\"entityField\\\": \\\"vehicle_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"origin\\\"," +
            "        \\\"entityField\\\": \\\"production_area\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"qualifiedNumber\\\"," +
            "        \\\"entityField\\\": \\\"certification_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"bookNo\\\"," +
            "        \\\"entityField\\\": \\\"import_certificate_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"checkNo\\\"," +
            "        \\\"entityField\\\": \\\"commodity_inspection_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"engineNumber\\\"," +
            "        \\\"entityField\\\": \\\"engine_no\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"codeOfTheCompetentTaxAuthority\\\"," +
            "        \\\"entityField\\\": \\\"charge_tax_authority_code\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"nameOfTheCompetentTaxAuthority\\\"," +
            "        \\\"entityField\\\": \\\"charge_tax_authority_name\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"taxPaidProof\\\"," +
            "        \\\"entityField\\\": \\\"tax_paid_proof\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"tonnage\\\"," +
            "        \\\"entityField\\\": \\\"tonnage\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"max_capacity\\\"," +
            "        \\\"entityField\\\": \\\"max_capacity\\\"" +
            "      }," +
            "      {" +
            "        \\\"srcField\\\": \\\"qrcode\\\"," +
            "        \\\"entityField\\\": \\\"qrcode\\\"" +
            "      }" +
            "    ]" +
            "  }" +
            "]\", \"F1295238504688762882S\": \"1\", \"F1295238505305325570S\": \"1\", \"F1295238506429399042L\": 4464073942242932093, \"F1295238507134042114L\": 1618579280792, \"F1295238508010651649L\": 1619769521531, \"F1295238509587709953L\": 4603688623062720515, \"F1295238510393016321L\": 4603688623062720515, \"F1295238511240265729S\": \"柳红彬\", \"F1295238512058155010S\": \"柳红彬\", \"F1295238512855072769S\": \"1\", \"F1295238513526161410S\": \"CQP\"}";
    @Test
    public void test() throws JsonProcessingException {
        List<Object> objects = OriginalEntityUtils.attributesToList(str3);
        Assert.assertNotNull(objects);
    }
}
