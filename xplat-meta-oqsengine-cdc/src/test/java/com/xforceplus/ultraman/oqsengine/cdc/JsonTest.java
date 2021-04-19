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
    String str = "{\n" +
            "\t\"F1295238501979242498S\": \"ticket_compare_config\",\n" +
            "\t\"F1295238502528696321S\": \"单证字段比较配置\",\n" +
            "\t\"F1295238503136870402S\": \"[{\\n\\t\\\"entityCode\\\": \\\"ticketInvoice\\\",\\n\\t\\\"checkType\\\": \\\"1\\\",\\n\\t\\\"items\\\": [\\\"invoice_type\\\", \\\"invoice_sheet\\\", \\\"invoice_code\\\", \\\"invoice_no\\\", \\\"invoice_date\\\", \\\"purchaser_name\\\", \\\"purchaser_tax_no\\\", \\\"seller_name\\\", \\\"seller_tax_no\\\", \\\"amount_without_tax\\\", \\\"tax_amount\\\", \\\"amount_with_tax\\\", \\\"check_code\\\", \\\"machine_code\\\", \\\"cipher_text\\\"]\\n}, {\\n\\t\\\"entityCode\\\": \\\"ticketQuota\\\",\\n\\t\\\"checkType\\\": \\\"1\\\",\\n\\t\\\"items\\\": [\\\"invoice_no\\\", \\\"invoice_code\\\", \\\"amount_with_tax\\\"]\\n}, {\\n\\t\\\"entityCode\\\": \\\"ticketMachine\\\",\\n\\t\\\"checkType\\\": \\\"1\\\",\\n\\t\\\"items\\\": [\\\"invoice_no\\\", \\\"invoice_code\\\", \\\"invoice_sheet\\\", \\\"invoice_date\\\", \\\"amount_with_tax\\\", \\\"tax_amount\\\", \\\"amount_without_tax\\\", \\\"purchaser_name\\\", \\\"purchaser_tax_no\\\", \\\"seller_name\\\", \\\"seller_tax_no\\\", \\\"check_code\\\", \\\"machine_code\\\", \\\"cipher_text\\\"]\\n}, {\\n\\t\\\"entityCode\\\": \\\"ticketPlane\\\",\\n\\t\\\"checkType\\\": \\\"1\\\",\\n\\t\\\"items\\\": [\\\"name_of_passenger\\\", \\\"id_no\\\", \\\"e_ticket_no\\\", \\\"check_code\\\", \\\"issued_by\\\", \\\"date_of_issue\\\", \\\"insurance\\\", \\\"fare\\\", \\\"caac_development_fund\\\", \\\"fuel_surcharge\\\", \\\"total\\\", \\\"tax\\\"]\\n}, {\\n\\t\\\"entityCode\\\": \\\"ticketTrain\\\",\\n\\t\\\"checkType\\\": \\\"1\\\",\\n\\t\\\"items\\\": [\\\"name\\\", \\\"trains\\\", \\\"start_station\\\", \\\"end_station\\\", \\\"start_date\\\", \\\"start_time\\\", \\\"seat\\\", \\\"seat_type\\\", \\\"certificate_no\\\", \\\"qrcode\\\", \\\"amount_with_tax\\\"]\\n}, {\\n\\t\\\"entityCode\\\": \\\"ticketTaxi\\\",\\n\\t\\\"checkType\\\": \\\"1\\\",\\n\\t\\\"items\\\": [\\\"invoice_no\\\", \\\"invoice_code\\\", \\\"start_date\\\", \\\"get_on_time\\\", \\\"mileage\\\", \\\"get_off_time\\\", \\\"name\\\", \\\"amount_with_tax\\\"]\\n}, {\\n\\t\\\"entityCode\\\": \\\"ticketUsedCar\\\",\\n\\t\\\"checkType\\\": \\\"1\\\",\\n\\t\\\"items\\\": [\\\"invoice_no\\\", \\\"invoice_code\\\", \\\"invoice_date\\\", \\\"amount_with_tax\\\", \\\"registration_no\\\", \\\"vehicle_no\\\", \\\"vehicle_brand\\\", \\\"seller_name\\\", \\\"seller_tax_no\\\", \\\"purchaser_name\\\", \\\"purchaser_tax_no\\\", \\\"auctioneers_name\\\", \\\"auctioneers_address\\\", \\\"auctioneers_tax_no\\\", \\\"auctioneers_bank_info\\\", \\\"auctioneers_tel\\\", \\\"used_car_market_name\\\", \\\"used_car_market_address\\\", \\\"used_car_market_tax_no\\\", \\\"used_car_market_bank_info\\\", \\\"used_car_market_tel\\\", \\\"car_number\\\", \\\"dq_code\\\", \\\"dp_name\\\"]\\n}, {\\n\\t\\\"entityCode\\\": \\\"ticketBus\\\",\\n\\t\\\"checkType\\\": \\\"1\\\",\\n\\t\\\"items\\\": [\\\"invoice_no\\\", \\\"invoice_code\\\", \\\"trains\\\", \\\"start_station\\\", \\\"end_station\\\", \\\"start_date\\\", \\\"start_time\\\", \\\"seat\\\", \\\"carrier\\\", \\\"name\\\", \\\"amount_with_tax\\\", \\\"purchaser_name\\\", \\\"purchaser_tax_no\\\"]\\n}, {\\n\\t\\\"entityCode\\\": \\\"ticketToll\\\",\\n\\t\\\"checkType\\\": \\\"1\\\",\\n\\t\\\"items\\\": [\\\"invoice_no\\\", \\\"invoice_code\\\", \\\"exit_place\\\", \\\"entrance_place\\\", \\\"start_date\\\", \\\"payment\\\", \\\"vehicles_type\\\", \\\"vehicles_weight\\\", \\\"toll_limit\\\", \\\"amount_with_tax\\\"]\\n}, {\\n\\t\\\"entityCode\\\": \\\"ticketVehicle\\\",\\n\\t\\\"checkType\\\": \\\"1\\\",\\n\\t\\\"items\\\": [\\\"vehicle_sheet\\\", \\\"invoice_no\\\", \\\"invoice_code\\\", \\\"invoice_date\\\", \\\"seller_name\\\", \\\"seller_tax_no\\\", \\\"amount_without_tax\\\", \\\"tax_amount\\\", \\\"amount_with_tax\\\", \\\"vehicle_type\\\", \\\"vehicle_brand\\\", \\\"production_area\\\", \\\"engine_no\\\", \\\"commodity_inspection_no\\\", \\\"certification_no\\\", \\\"vehicle_no\\\", \\\"import_certificate_no\\\", \\\"charge_tax_authority_code\\\", \\\"charge_tax_authority_name\\\", \\\"tax_paid_proof\\\", \\\"tonnage\\\", \\\"max_capacity\\\", \\\"dq_code\\\", \\\"dq_name\\\"]\\n}]\",\n" +
            "\t\"F1295238504688762882S\": \"1\",\n" +
            "\t\"F1295238505305325570S\": \"1\",\n" +
            "\t\"F1295238506429399042L\": 4464073942242932093,\n" +
            "\t\"F1295238507134042114L\": 1618573382435,\n" +
            "\t\"F1295238508010651649L\": 1618573382435,\n" +
            "\t\"F1295238509587709953L\": 4603688623062720515,\n" +
            "\t\"F1295238510393016321L\": 4603688623062720515,\n" +
            "\t\"F1295238511240265729S\": \"柳红彬\",\n" +
            "\t\"F1295238512058155010S\": \"柳红彬\",\n" +
            "\t\"F1295238512855072769S\": \"1\",\n" +
            "\t\"F1295238513526161410S\": \"CQP\"\n" +
            "}";

    String str2 = "{\"tenant_id\":\"4567581588943585293\",\"create_user_id\":\"4612820548148789257\",\"dict_desc\":\"小组角色关系\",\"create_user_name\":\"柳红彬\",\"create_time\":\"1598350807168\",\"update_user_name\":\"柳红彬\",\"is_default\":\"1\",\"dict_code\":\"team_relation\",\"update_time\":\"1598350807168\",\"update_user_id\":\"4612820548148789257\",\"enable\":\"1\",\"dict_value\":\"[\\n  {\\n    \\\"itemCode\\\": \\\"checkScan\\\",\\n    \\\"itemValue\\\": {\\n      \\\"4418255883950761701\\\": \\\"4418255815205528906\\\",\\n      \\\"4418255815205528921\\\": \\\"4418255815205528921\\\"\\n    }\\n  },\\n  {\\n    \\\"itemCode\\\": \\\"leaderScan\\\",\\n    \\\"itemValue\\\": {\\n      \\\"4418255815205528921\\\": \\\"4418255883950761701,4418255815205528906\\\"\\n    }\\n  }\\n]\",\"id\":\"1298203494372605953\",\"delete_flag\":\"1\",\"tenant\":null,\"tenant_code\":\"CQP\"}  ";

    @Test
    public void test() throws JsonProcessingException {
        List<Object> objects = OriginalEntityUtils.attributesToList(str2);
        Assert.assertNotNull(objects);
    }
}
