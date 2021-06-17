package com.xforceplus.ultraman.oqsengine.metadata;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
public class RequestBodyInit {
    public static final StringBuilder stringBuilder = new StringBuilder();

    public static String initToString() {
        return stringBuilder.append("{\n" +
            "  \"entityClasses\": [{\n" +
            "    \"code\": \"component\",\n" +
            "    \"id\": \"1251658380868685825\",\n" +
            "    \"name\": \"组件\",\n" +
            "    \"version\": 2027,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1251658381032263682\",\n" +
            "      \"name\": \"name\",\n" +
            "      \"cname\": \"组件名称\",\n" +
            "      \"fieldType\": \"ENUM\",\n" +
            "      \"dictId\": \"1271016796095225858\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"100\",\n" +
            "        \"displayType\": \"1\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6,\n" +
            "        \"uniqueName\": \"component:IDX_U0:1\"\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1251658381070012417\",\n" +
            "      \"name\": \"category\",\n" +
            "      \"cname\": \"组件分类\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"100\",\n" +
            "        \"displayType\": \"2\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1251658381090983938\",\n" +
            "      \"name\": \"package_json\",\n" +
            "      \"cname\": \"组件信息\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"max\": \"2000\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1251658381107761153\",\n" +
            "      \"name\": \"assets_path\",\n" +
            "      \"cname\": \"静态资源路径\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"max\": \"200\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1251658381128732673\",\n" +
            "      \"name\": \"assets\",\n" +
            "      \"cname\": \"静态资源名称\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"max\": \"2000\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1251658381153898498\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1251658381187452929\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1251658381212618754\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1251658381237784577\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1251658381254561793\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1251658381267144706\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1251658381283921922\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1251658381300699137\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1251658381317476354\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1265859342750269442\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户名称\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1332200165788598274\",\n" +
            "      \"name\": \"org_code\",\n" +
            "      \"cname\": \"组织代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6,\n" +
            "        \"uniqueName\": \"component:IDX_U0:0\"\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1353643383498682370\",\n" +
            "      \"name\": \"type\",\n" +
            "      \"cname\": \"type\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"dictId\": \"1271016796095225858\",\n" +
            "      \"defaultValue\": \"3\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1398211425370079234\",\n" +
            "      \"name\": \"addTest\",\n" +
            "      \"cname\": \"新增测试\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }],\n" +
            "    \"relations\": [{\n" +
            "      \"id\": \"1394233859919036417\",\n" +
            "      \"code\": \"level4\",\n" +
            "      \"rightEntityClassId\": \"1392404671652364290\",\n" +
            "      \"leftEntityClassId\": \"1251658380868685825\",\n" +
            "      \"leftEntityClassCode\": \"component\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1394233859919036417\",\n" +
            "        \"name\": \"level4.id\",\n" +
            "        \"cname\": \"level4\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }, {\n" +
            "      \"id\": \"1394233859931619329\",\n" +
            "      \"code\": \"level4\",\n" +
            "      \"rightEntityClassId\": \"1251658380868685825\",\n" +
            "      \"leftEntityClassId\": \"1392404671652364290\",\n" +
            "      \"leftEntityClassCode\": \"cPerson\",\n" +
            "      \"relationType\": 2\n" +
            "    }],\n" +
            "    \"profiles\": [{\n" +
            "      \"code\": \"SC@vanke\",\n" +
            "      \"entityFieldInfo\": [{\n" +
            "        \"id\": \"1400325783050375170\",\n" +
            "        \"name\": \"wankeTest\",\n" +
            "        \"cname\": \"万科测试\",\n" +
            "        \"fieldType\": \"STRING\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"max\": \"20\",\n" +
            "          \"isRequired\": true,\n" +
            "          \"displayType\": \"0\",\n" +
            "          \"metaFieldSense\": \"NORMAL\",\n" +
            "          \"fuzzyType\": 1,\n" +
            "          \"wildcardMinWidth\": 3,\n" +
            "          \"wildcardMaxWidth\": 6\n" +
            "        },\n" +
            "        \"calculator\": {\n" +
            "          \"calculateType\": 1\n" +
            "        }\n" +
            "      }]\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"person\",\n" +
            "    \"id\": \"1263391796170928130\",\n" +
            "    \"name\": \"人员\",\n" +
            "    \"version\": 2027,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1263391797307584514\",\n" +
            "      \"name\": \"name\",\n" +
            "      \"cname\": \"姓名\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"displayType\": \"1\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1263391797433413634\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1263391797450190849\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1263391797462773762\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户名称\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1263391797475356674\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1263391797697654786\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1263391797739597825\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1263391797752180737\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1263391797777346562\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1263391797789929473\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1263391797802512385\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1297540906451435522\",\n" +
            "      \"name\": \"sex\",\n" +
            "      \"cname\": \"性别\",\n" +
            "      \"fieldType\": \"ENUM\",\n" +
            "      \"dictId\": \"1302848623793127426\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1393962148217425922\",\n" +
            "      \"name\": \"func1\",\n" +
            "      \"cname\": \"公式\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 2,\n" +
            "        \"expression\": \"${name}\",\n" +
            "        \"step\": 10000,\n" +
            "        \"level\": 1,\n" +
            "        \"args\": [\"name\"],\n" +
            "        \"failedPolicy\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1393988108123836417\",\n" +
            "      \"name\": \"lengthtest\",\n" +
            "      \"cname\": \"长度测试\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"5\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1394183420836122626\",\n" +
            "      \"name\": \"amount\",\n" +
            "      \"cname\": \"不含税金额\",\n" +
            "      \"fieldType\": \"DECIMAL\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"precision\": 6,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1394183420840316929\",\n" +
            "      \"name\": \"tax\",\n" +
            "      \"cname\": \"税率\",\n" +
            "      \"fieldType\": \"DECIMAL\",\n" +
            "      \"defaultValue\": \"0.2\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"precision\": 6,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1394183420844511234\",\n" +
            "      \"name\": \"cost\",\n" +
            "      \"cname\": \"基础费用\",\n" +
            "      \"fieldType\": \"DECIMAL\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"precision\": 6,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1394187662833553409\",\n" +
            "      \"name\": \"withTaxAmount\",\n" +
            "      \"cname\": \"含税金额\",\n" +
            "      \"fieldType\": \"DECIMAL\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"precision\": 6,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 2,\n" +
            "        \"expression\": \"${amount} - ${amount}*${tax}\",\n" +
            "        \"step\": 10000,\n" +
            "        \"level\": 1,\n" +
            "        \"args\": [\"amount\", \"amount\", \"tax\"],\n" +
            "        \"failedPolicy\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1394187662841942017\",\n" +
            "      \"name\": \"taxAmount\",\n" +
            "      \"cname\": \"税额\",\n" +
            "      \"fieldType\": \"DECIMAL\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"precision\": 2,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 2,\n" +
            "        \"expression\": \"${amount}*${tax}\",\n" +
            "        \"step\": 10000,\n" +
            "        \"level\": 1,\n" +
            "        \"args\": [\"amount\", \"tax\"],\n" +
            "        \"failedPolicy\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1394218159043317762\",\n" +
            "      \"name\": \"personNo\",\n" +
            "      \"cname\": \"人员编号\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 3,\n" +
            "        \"min\": \"0\",\n" +
            "        \"patten\": \"CN-{yyyy}-{MM}-{dd}-{000000}\",\n" +
            "        \"model\": \"1\",\n" +
            "        \"step\": 1000\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1394558043123978241\",\n" +
            "      \"name\": \"taxAmountDouble\",\n" +
            "      \"cname\": \"税额翻倍\",\n" +
            "      \"fieldType\": \"DECIMAL\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"precision\": 6,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 2,\n" +
            "        \"expression\": \"${taxAmount}*2\",\n" +
            "        \"step\": 10000,\n" +
            "        \"level\": 2,\n" +
            "        \"args\": [\"taxAmount\"],\n" +
            "        \"failedPolicy\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1395566938604040193\",\n" +
            "      \"name\": \"stringsTest\",\n" +
            "      \"cname\": \"多值测试\",\n" +
            "      \"fieldType\": \"STRINGS\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"2000\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1400291231380656130\",\n" +
            "      \"name\": \"errorTest\",\n" +
            "      \"cname\": \"错误字段测试\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 2,\n" +
            "        \"expression\": \"${name}\",\n" +
            "        \"step\": 10000,\n" +
            "        \"level\": 1,\n" +
            "        \"args\": [\"name\"],\n" +
            "        \"failedPolicy\": 2,\n" +
            "        \"failedDefaultValue\": {\n" +
            "          \"@type\": \"type.googleapis.com/google.protobuf.StringValue\",\n" +
            "          \"value\": \"默认客户\"\n" +
            "        }\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1400328115309891586\",\n" +
            "      \"name\": \"numberTest\",\n" +
            "      \"cname\": \"数字错误字段测试\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 2,\n" +
            "        \"expression\": \"1+1\",\n" +
            "        \"step\": 10000,\n" +
            "        \"level\": 1,\n" +
            "        \"failedPolicy\": 2,\n" +
            "        \"failedDefaultValue\": {\n" +
            "          \"@type\": \"type.googleapis.com/google.protobuf.Int64Value\",\n" +
            "          \"value\": \"0\"\n" +
            "        }\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1400328115318280193\",\n" +
            "      \"name\": \"timeTest\",\n" +
            "      \"cname\": \"时间错误字段测试\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 2,\n" +
            "        \"expression\": \"${create_time}\",\n" +
            "        \"step\": 10000,\n" +
            "        \"level\": 1,\n" +
            "        \"args\": [\"create_time\"],\n" +
            "        \"failedPolicy\": 2,\n" +
            "        \"failedDefaultValue\": {\n" +
            "          \"@type\": \"type.googleapis.com/google.protobuf.Int64Value\",\n" +
            "          \"value\": \"1622699125748\"\n" +
            "        }\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1400328115322474498\",\n" +
            "      \"name\": \"floatTest\",\n" +
            "      \"cname\": \"浮点字段错误测试\",\n" +
            "      \"fieldType\": \"DECIMAL\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"precision\": 2,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 2,\n" +
            "        \"expression\": \"${tax}\",\n" +
            "        \"step\": 10000,\n" +
            "        \"level\": 1,\n" +
            "        \"args\": [\"tax\"],\n" +
            "        \"failedPolicy\": 2,\n" +
            "        \"failedDefaultValue\": {\n" +
            "          \"@type\": \"type.googleapis.com/google.protobuf.DoubleValue\",\n" +
            "          \"value\": 1.0\n" +
            "        }\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1400328115326668802\",\n" +
            "      \"name\": \"booleanTest\",\n" +
            "      \"cname\": \"布尔字段错误测试\",\n" +
            "      \"fieldType\": \"BOOLEAN\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 2,\n" +
            "        \"expression\": \"1\\u003d\\u003d1\",\n" +
            "        \"step\": 10000,\n" +
            "        \"level\": 1,\n" +
            "        \"failedPolicy\": 2,\n" +
            "        \"failedDefaultValue\": {\n" +
            "          \"@type\": \"type.googleapis.com/google.protobuf.BoolValue\",\n" +
            "          \"value\": false\n" +
            "        }\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1401732445961650177\",\n" +
            "      \"name\": \"nullTest\",\n" +
            "      \"cname\": \"空值默认值处理\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 2,\n" +
            "        \"expression\": \"${sex}\",\n" +
            "        \"step\": 10000,\n" +
            "        \"level\": 1,\n" +
            "        \"args\": [\"sex\"],\n" +
            "        \"failedPolicy\": 2,\n" +
            "        \"failedDefaultValue\": {\n" +
            "          \"@type\": \"type.googleapis.com/google.protobuf.StringValue\",\n" +
            "          \"value\": \"未知\"\n" +
            "        }\n" +
            "      }\n" +
            "    }],\n" +
            "    \"relations\": [{\n" +
            "      \"id\": \"1263391920565596162\",\n" +
            "      \"code\": \"manager\",\n" +
            "      \"rightEntityClassId\": \"1263391796170928130\",\n" +
            "      \"leftEntityClassId\": \"1263391796170928130\",\n" +
            "      \"leftEntityClassCode\": \"person\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1263391920565596162\",\n" +
            "        \"name\": \"manager.id\",\n" +
            "        \"cname\": \"上级主管\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }, {\n" +
            "      \"id\": \"1263391983153000449\",\n" +
            "      \"code\": \"boss\",\n" +
            "      \"rightEntityClassId\": \"1263391796170928130\",\n" +
            "      \"leftEntityClassId\": \"1263391796170928130\",\n" +
            "      \"leftEntityClassCode\": \"person\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1263391983153000449\",\n" +
            "        \"name\": \"boss.id\",\n" +
            "        \"cname\": \"老板\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }, {\n" +
            "      \"id\": \"1275678844106498050\",\n" +
            "      \"code\": \"departPerson\",\n" +
            "      \"rightEntityClassId\": \"1275678539314814978\",\n" +
            "      \"leftEntityClassId\": \"1263391796170928130\",\n" +
            "      \"leftEntityClassCode\": \"person\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1275678844106498050\",\n" +
            "        \"name\": \"departPerson.id\",\n" +
            "        \"cname\": \"组织人员\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }, {\n" +
            "      \"id\": \"1275679197380141058\",\n" +
            "      \"code\": \"companyPerson\",\n" +
            "      \"rightEntityClassId\": \"1275678608923484162\",\n" +
            "      \"leftEntityClassId\": \"1263391796170928130\",\n" +
            "      \"leftEntityClassCode\": \"person\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1275679197380141058\",\n" +
            "        \"name\": \"companyPerson.id\",\n" +
            "        \"cname\": \"人员公司\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }, {\n" +
            "      \"id\": \"1275680087679885314\",\n" +
            "      \"code\": \"otherCompany\",\n" +
            "      \"rightEntityClassId\": \"1275678608923484162\",\n" +
            "      \"leftEntityClassId\": \"1263391796170928130\",\n" +
            "      \"leftEntityClassCode\": \"person\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1275680087679885314\",\n" +
            "        \"name\": \"otherCompany.id\",\n" +
            "        \"cname\": \"外包公司\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }, {\n" +
            "      \"id\": \"1395571833134510081\",\n" +
            "      \"code\": \"mulitiTest\",\n" +
            "      \"rightEntityClassId\": \"1275678539314814978\",\n" +
            "      \"leftEntityClassId\": \"1263391796170928130\",\n" +
            "      \"leftEntityClassCode\": \"person\",\n" +
            "      \"relationType\": 5,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1395571833134510081\",\n" +
            "        \"name\": \"mulitiTest.id\",\n" +
            "        \"cname\": \"多值测试\",\n" +
            "        \"fieldType\": \"STRINGS\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }, {\n" +
            "      \"id\": \"1275678844093915137\",\n" +
            "      \"code\": \"departPerson\",\n" +
            "      \"rightEntityClassId\": \"1263391796170928130\",\n" +
            "      \"leftEntityClassId\": \"1275678539314814978\",\n" +
            "      \"leftEntityClassCode\": \"depart\",\n" +
            "      \"relationType\": 2\n" +
            "    }],\n" +
            "    \"profiles\": [{\n" +
            "      \"code\": \"CQP\",\n" +
            "      \"entityFieldInfo\": [{\n" +
            "        \"id\": \"1394599234922188801\",\n" +
            "        \"name\": \"cqpAmount\",\n" +
            "        \"cname\": \"医药费\",\n" +
            "        \"fieldType\": \"DECIMAL\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"max\": \"20\",\n" +
            "          \"isRequired\": true,\n" +
            "          \"displayType\": \"0\",\n" +
            "          \"metaFieldSense\": \"NORMAL\",\n" +
            "          \"fuzzyType\": 1,\n" +
            "          \"wildcardMinWidth\": 3,\n" +
            "          \"wildcardMaxWidth\": 6\n" +
            "        },\n" +
            "        \"calculator\": {\n" +
            "          \"calculateType\": 1\n" +
            "        }\n" +
            "      }, {\n" +
            "        \"id\": \"1394599234938966018\",\n" +
            "        \"name\": \"cqpFunc\",\n" +
            "        \"cname\": \"医药公式\",\n" +
            "        \"fieldType\": \"DECIMAL\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"max\": \"20\",\n" +
            "          \"precision\": 6,\n" +
            "          \"isRequired\": true,\n" +
            "          \"displayType\": \"0\",\n" +
            "          \"metaFieldSense\": \"NORMAL\",\n" +
            "          \"fuzzyType\": 1,\n" +
            "          \"wildcardMinWidth\": 3,\n" +
            "          \"wildcardMaxWidth\": 6\n" +
            "        },\n" +
            "        \"calculator\": {\n" +
            "          \"calculateType\": 2,\n" +
            "          \"expression\": \"${cqpAmount}\",\n" +
            "          \"step\": 10000,\n" +
            "          \"level\": 1,\n" +
            "          \"args\": [\"cqpAmount\"],\n" +
            "          \"failedPolicy\": 1\n" +
            "        }\n" +
            "      }]\n" +
            "    }, {\n" +
            "      \"code\": \"pangu\",\n" +
            "      \"entityFieldInfo\": [{\n" +
            "        \"id\": \"1394893312430649346\",\n" +
            "        \"name\": \"ultramanAmount\",\n" +
            "        \"cname\": \"奥特曼金额\",\n" +
            "        \"fieldType\": \"DECIMAL\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"max\": \"20\",\n" +
            "          \"precision\": 6,\n" +
            "          \"isRequired\": true,\n" +
            "          \"displayType\": \"0\",\n" +
            "          \"metaFieldSense\": \"NORMAL\",\n" +
            "          \"fuzzyType\": 1,\n" +
            "          \"wildcardMinWidth\": 3,\n" +
            "          \"wildcardMaxWidth\": 6\n" +
            "        },\n" +
            "        \"calculator\": {\n" +
            "          \"calculateType\": 1\n" +
            "        }\n" +
            "      }, {\n" +
            "        \"id\": \"1394893312447426561\",\n" +
            "        \"name\": \"ultramanTax\",\n" +
            "        \"cname\": \"奥特曼税率\",\n" +
            "        \"fieldType\": \"DECIMAL\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"max\": \"20\",\n" +
            "          \"precision\": 6,\n" +
            "          \"isRequired\": true,\n" +
            "          \"displayType\": \"0\",\n" +
            "          \"metaFieldSense\": \"NORMAL\",\n" +
            "          \"fuzzyType\": 1,\n" +
            "          \"wildcardMinWidth\": 3,\n" +
            "          \"wildcardMaxWidth\": 6\n" +
            "        },\n" +
            "        \"calculator\": {\n" +
            "          \"calculateType\": 1\n" +
            "        }\n" +
            "      }, {\n" +
            "        \"id\": \"1394893312455815169\",\n" +
            "        \"name\": \"ultramanwithTaxAmount\",\n" +
            "        \"cname\": \"奥特曼含税金额\",\n" +
            "        \"fieldType\": \"DECIMAL\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"max\": \"20\",\n" +
            "          \"precision\": 6,\n" +
            "          \"isRequired\": true,\n" +
            "          \"displayType\": \"0\",\n" +
            "          \"metaFieldSense\": \"NORMAL\",\n" +
            "          \"fuzzyType\": 1,\n" +
            "          \"wildcardMinWidth\": 3,\n" +
            "          \"wildcardMaxWidth\": 6\n" +
            "        },\n" +
            "        \"calculator\": {\n" +
            "          \"calculateType\": 2,\n" +
            "          \"expression\": \"${ultramanAmount} - ${ultramanAmount}*${ultramanTax}\",\n" +
            "          \"step\": 10000,\n" +
            "          \"level\": 1,\n" +
            "          \"args\": [\"ultramanAmount\", \"ultramanAmount\", \"ultramanTax\"],\n" +
            "          \"failedPolicy\": 1\n" +
            "        }\n" +
            "      }, {\n" +
            "        \"id\": \"1394893312464203777\",\n" +
            "        \"name\": \"ultramanTaxAmount\",\n" +
            "        \"cname\": \"奥特曼引用税额\",\n" +
            "        \"fieldType\": \"DECIMAL\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"max\": \"20\",\n" +
            "          \"precision\": 6,\n" +
            "          \"isRequired\": true,\n" +
            "          \"displayType\": \"0\",\n" +
            "          \"metaFieldSense\": \"NORMAL\",\n" +
            "          \"fuzzyType\": 1,\n" +
            "          \"wildcardMinWidth\": 3,\n" +
            "          \"wildcardMaxWidth\": 6\n" +
            "        },\n" +
            "        \"calculator\": {\n" +
            "          \"calculateType\": 2,\n" +
            "          \"expression\": \"${taxAmount}\",\n" +
            "          \"step\": 10000,\n" +
            "          \"level\": 1,\n" +
            "          \"args\": [\"taxAmount\"],\n" +
            "          \"failedPolicy\": 1\n" +
            "        }\n" +
            "      }, {\n" +
            "        \"id\": \"1396727088436420609\",\n" +
            "        \"name\": \"amountAndTax\",\n" +
            "        \"cname\": \"父字段测试\",\n" +
            "        \"fieldType\": \"DECIMAL\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"max\": \"20\",\n" +
            "          \"precision\": 6,\n" +
            "          \"isRequired\": true,\n" +
            "          \"displayType\": \"0\",\n" +
            "          \"metaFieldSense\": \"NORMAL\",\n" +
            "          \"fuzzyType\": 1,\n" +
            "          \"wildcardMinWidth\": 3,\n" +
            "          \"wildcardMaxWidth\": 6\n" +
            "        },\n" +
            "        \"calculator\": {\n" +
            "          \"calculateType\": 2,\n" +
            "          \"expression\": \"${ultramanwithTaxAmount} - ${amount} - 123\",\n" +
            "          \"step\": 10000,\n" +
            "          \"level\": 2,\n" +
            "          \"args\": [\"ultramanwithTaxAmount\", \"amount\"],\n" +
            "          \"failedPolicy\": 1\n" +
            "        }\n" +
            "      }]\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"route\",\n" +
            "    \"id\": \"1267402433675583489\",\n" +
            "    \"name\": \"路由配置\",\n" +
            "    \"version\": 2026,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1267402433960796162\",\n" +
            "      \"name\": \"route_name\",\n" +
            "      \"cname\": \"服务名称\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1267402433985961986\",\n" +
            "      \"name\": \"path\",\n" +
            "      \"cname\": \"路由地址\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1267402434002739201\",\n" +
            "      \"name\": \"url\",\n" +
            "      \"cname\": \"服务地址\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"max\": \"20\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1267402434019516417\",\n" +
            "      \"name\": \"billable\",\n" +
            "      \"cname\": \"是否计费\",\n" +
            "      \"fieldType\": \"BOOLEAN\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1267402434032099329\",\n" +
            "      \"name\": \"check_auth\",\n" +
            "      \"cname\": \"校验权限\",\n" +
            "      \"fieldType\": \"BOOLEAN\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1267402434053070849\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1267402434069848066\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1267402434086625282\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1267402434103402497\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1267402434115985410\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1267402434132762625\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1267402434149539841\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1267402434162122754\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1267402434174705666\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1267402434187288578\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }],\n" +
            "    \"relations\": [{\n" +
            "      \"id\": \"1267402905287319553\",\n" +
            "      \"code\": \"routeApp\",\n" +
            "      \"rightEntityClassId\": \"1267402804498194434\",\n" +
            "      \"leftEntityClassId\": \"1267402433675583489\",\n" +
            "      \"leftEntityClassCode\": \"route\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1267402905287319553\",\n" +
            "        \"name\": \"routeApp.id\",\n" +
            "        \"cname\": \"产品线\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"app\",\n" +
            "    \"id\": \"1267402804498194434\",\n" +
            "    \"name\": \"应用\",\n" +
            "    \"version\": 2026,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1267402804741464065\",\n" +
            "      \"name\": \"app_name\",\n" +
            "      \"cname\": \"名称\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"displayType\": \"1\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1267402804766629890\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1267402804783407106\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1267402804795990017\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1267402804812767234\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6,\n" +
            "        \"uniqueName\": \"app:IDX_U0:5\"\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1267402804825350145\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1267402804837933058\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1267402804863098882\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1267402804875681794\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6,\n" +
            "        \"uniqueName\": \"app:IDX_U0:1\"\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1267402804888264705\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6,\n" +
            "        \"uniqueName\": \"app:IDX_U0:4\"\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1267402804905041921\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1351055486036459522\",\n" +
            "      \"name\": \"moren\",\n" +
            "      \"cname\": \"默认值测试\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"defaultValue\": \"world\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6,\n" +
            "        \"uniqueName\": \"app:IDX_U0:2\"\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1394945024284069890\",\n" +
            "      \"name\": \"amount\",\n" +
            "      \"cname\": \"应用金额\",\n" +
            "      \"fieldType\": \"DECIMAL\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"precision\": 6,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6,\n" +
            "        \"uniqueName\": \"app:IDX_U0:1\"\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1394945024288264194\",\n" +
            "      \"name\": \"tax\",\n" +
            "      \"cname\": \"应用税率\",\n" +
            "      \"fieldType\": \"DECIMAL\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"precision\": 6,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6,\n" +
            "        \"uniqueName\": \"app:IDX_U0:2\"\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1394945024296652801\",\n" +
            "      \"name\": \"taxAmount\",\n" +
            "      \"cname\": \"应用税额\",\n" +
            "      \"fieldType\": \"DECIMAL\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"precision\": 6,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6,\n" +
            "        \"uniqueName\": \"app:IDX_U0:3\"\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 2,\n" +
            "        \"expression\": \"${amount}*${tax}\",\n" +
            "        \"step\": 10000,\n" +
            "        \"level\": 1,\n" +
            "        \"args\": [\"amount\", \"tax\"],\n" +
            "        \"failedPolicy\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1394945024300847106\",\n" +
            "      \"name\": \"noTaxAmount\",\n" +
            "      \"cname\": \"应用余额\",\n" +
            "      \"fieldType\": \"DECIMAL\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"precision\": 6,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 2,\n" +
            "        \"expression\": \"${amount}-${taxAmount}\",\n" +
            "        \"step\": 10000,\n" +
            "        \"level\": 2,\n" +
            "        \"args\": [\"amount\", \"taxAmount\"],\n" +
            "        \"failedPolicy\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1394950073320116225\",\n" +
            "      \"name\": \"appNo1\",\n" +
            "      \"cname\": \"应用编号\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6,\n" +
            "        \"uniqueName\": \"app:IDX_U0:0\"\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 3,\n" +
            "        \"min\": \"1\",\n" +
            "        \"patten\": \"APP-{yyyy}-{MM}-{dd}-{000000}\",\n" +
            "        \"model\": \"2\",\n" +
            "        \"step\": 1000\n" +
            "      }\n" +
            "    }],\n" +
            "    \"relations\": [{\n" +
            "      \"id\": \"1337278632578109441\",\n" +
            "      \"code\": \"appOrg\",\n" +
            "      \"rightEntityClassId\": \"1275678539314814978\",\n" +
            "      \"leftEntityClassId\": \"1267402804498194434\",\n" +
            "      \"leftEntityClassCode\": \"app\",\n" +
            "      \"relationType\": 1,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1337278632578109441\",\n" +
            "        \"name\": \"appOrg.id\",\n" +
            "        \"cname\": \"应用组织\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }, {\n" +
            "      \"id\": \"1394233705749004290\",\n" +
            "      \"code\": \"level2\",\n" +
            "      \"rightEntityClassId\": \"1392404401170087937\",\n" +
            "      \"leftEntityClassId\": \"1267402804498194434\",\n" +
            "      \"leftEntityClassCode\": \"app\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1394233705749004290\",\n" +
            "        \"name\": \"level2.id\",\n" +
            "        \"cname\": \"level2\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }, {\n" +
            "      \"id\": \"1394233797209997314\",\n" +
            "      \"code\": \"level3\",\n" +
            "      \"rightEntityClassId\": \"1392404559869968385\",\n" +
            "      \"leftEntityClassId\": \"1267402804498194434\",\n" +
            "      \"leftEntityClassCode\": \"app\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1394233797209997314\",\n" +
            "        \"name\": \"level3.id\",\n" +
            "        \"cname\": \"level3\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }, {\n" +
            "      \"id\": \"1394233957235277826\",\n" +
            "      \"code\": \"level5\",\n" +
            "      \"rightEntityClassId\": \"1392404791726899201\",\n" +
            "      \"leftEntityClassId\": \"1267402804498194434\",\n" +
            "      \"leftEntityClassCode\": \"app\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1394233957235277826\",\n" +
            "        \"name\": \"level5.id\",\n" +
            "        \"cname\": \"level5\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }, {\n" +
            "      \"id\": \"1267402905287319553\",\n" +
            "      \"code\": \"routeApp\",\n" +
            "      \"rightEntityClassId\": \"1267402804498194434\",\n" +
            "      \"leftEntityClassId\": \"1267402433675583489\",\n" +
            "      \"leftEntityClassCode\": \"route\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1267402905287319553\",\n" +
            "        \"name\": \"routeApp.id\",\n" +
            "        \"cname\": \"产品线\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }, {\n" +
            "      \"id\": \"1394233705765781505\",\n" +
            "      \"code\": \"level2\",\n" +
            "      \"rightEntityClassId\": \"1267402804498194434\",\n" +
            "      \"leftEntityClassId\": \"1392404401170087937\",\n" +
            "      \"leftEntityClassCode\": \"aPerson\",\n" +
            "      \"relationType\": 2\n" +
            "    }, {\n" +
            "      \"id\": \"1394233797222580225\",\n" +
            "      \"code\": \"level3\",\n" +
            "      \"rightEntityClassId\": \"1267402804498194434\",\n" +
            "      \"leftEntityClassId\": \"1392404559869968385\",\n" +
            "      \"leftEntityClassCode\": \"bPerson\",\n" +
            "      \"relationType\": 2\n" +
            "    }, {\n" +
            "      \"id\": \"1394233957252055041\",\n" +
            "      \"code\": \"level5\",\n" +
            "      \"rightEntityClassId\": \"1267402804498194434\",\n" +
            "      \"leftEntityClassId\": \"1392404791726899201\",\n" +
            "      \"leftEntityClassCode\": \"dPerson\",\n" +
            "      \"relationType\": 2\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"invoice\",\n" +
            "    \"id\": \"1268111464543596546\",\n" +
            "    \"name\": \"发票\",\n" +
            "    \"version\": 2023,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1268111464870752258\",\n" +
            "      \"name\": \"invoice_type\",\n" +
            "      \"cname\": \"发票类型\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1268111464900112386\",\n" +
            "      \"name\": \"invoice_no\",\n" +
            "      \"cname\": \"发票号码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"50\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1268111464916889601\",\n" +
            "      \"name\": \"buss_no\",\n" +
            "      \"cname\": \"业务单号\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"50\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1268111464933666817\",\n" +
            "      \"name\": \"invoice_status\",\n" +
            "      \"cname\": \"发票状态\",\n" +
            "      \"fieldType\": \"ENUM\",\n" +
            "      \"dictId\": \"1271016796095225858\",\n" +
            "      \"defaultValue\": \"1\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"50\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1268111464950444034\",\n" +
            "      \"name\": \"invoice_code\",\n" +
            "      \"cname\": \"发票代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"50\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1268111464967221250\",\n" +
            "      \"name\": \"invoice_from\",\n" +
            "      \"cname\": \"发票来源\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"50\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1268111464996581378\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1268111465013358593\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1268111465034330114\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1268111465067884545\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1268111465097244674\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1268111465114021889\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1268111465126604802\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1268111465139187714\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1268111465155964930\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1268111465181130754\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }],\n" +
            "    \"relations\": [{\n" +
            "      \"id\": \"1375361355015467009\",\n" +
            "      \"code\": \"ontomany\",\n" +
            "      \"rightEntityClassId\": \"1375361236081782786\",\n" +
            "      \"leftEntityClassId\": \"1268111464543596546\",\n" +
            "      \"leftEntityClassCode\": \"invoice\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1375361355015467009\",\n" +
            "        \"name\": \"ontomany.id\",\n" +
            "        \"cname\": \"一对多\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }, {\n" +
            "      \"id\": \"1336605543354769410\",\n" +
            "      \"code\": \"companyInvoice\",\n" +
            "      \"rightEntityClassId\": \"1268111464543596546\",\n" +
            "      \"leftEntityClassId\": \"1275678608923484162\",\n" +
            "      \"leftEntityClassCode\": \"company\",\n" +
            "      \"relationType\": 1,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1336605543354769410\",\n" +
            "        \"name\": \"companyInvoice.id\",\n" +
            "        \"cname\": \"公司票据\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }, {\n" +
            "      \"id\": \"1375361355044827137\",\n" +
            "      \"code\": \"ontomany\",\n" +
            "      \"rightEntityClassId\": \"1268111464543596546\",\n" +
            "      \"leftEntityClassId\": \"1375361236081782786\",\n" +
            "      \"leftEntityClassCode\": \"mobiletest\",\n" +
            "      \"relationType\": 2\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"importObj\",\n" +
            "    \"id\": \"1270529341674151937\",\n" +
            "    \"name\": \"导入对象\",\n" +
            "    \"version\": 2022,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1270529341946781697\",\n" +
            "      \"name\": \"name\",\n" +
            "      \"cname\": \"名称\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1270529341963558914\",\n" +
            "      \"name\": \"amount\",\n" +
            "      \"cname\": \"金额\",\n" +
            "      \"fieldType\": \"DECIMAL\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"precision\": 2,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1270529341971947522\",\n" +
            "      \"name\": \"is_enabled\",\n" +
            "      \"cname\": \"是否启用\",\n" +
            "      \"fieldType\": \"BOOLEAN\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"1\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1270529341988724737\",\n" +
            "      \"name\": \"date\",\n" +
            "      \"cname\": \"日期\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1270529342001307650\",\n" +
            "      \"name\": \"type\",\n" +
            "      \"cname\": \"类型\",\n" +
            "      \"fieldType\": \"ENUM\",\n" +
            "      \"dictId\": \"1245227711207960577\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"10\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1270529342009696258\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1270529342030667778\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1270529342047444994\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1270529342055833603\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1270529342076805122\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1270529342093582337\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1270529342106165250\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1270529342122942466\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1270529342135525377\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1270529342148108290\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1280756714960400386\",\n" +
            "      \"name\": \"hhhhhh\",\n" +
            "      \"cname\": \"类型\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1280759464280535041\",\n" +
            "      \"name\": \"hhhhh2\",\n" +
            "      \"cname\": \"类型\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"depart\",\n" +
            "    \"id\": \"1275678539314814978\",\n" +
            "    \"name\": \"组织\",\n" +
            "    \"version\": 2016,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1275678539612610562\",\n" +
            "      \"name\": \"name\",\n" +
            "      \"cname\": \"组织名称\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"displayType\": \"1\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1275678539654553602\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1275678539675525121\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1275678539696496641\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1275678539717468162\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1275678539738439682\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1275678539763605505\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1275678539780382721\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1275678539805548545\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1275678539830714369\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1275678539847491585\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1336604531806085122\",\n" +
            "      \"name\": \"type\",\n" +
            "      \"cname\": \"组织类型\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }],\n" +
            "    \"relations\": [{\n" +
            "      \"id\": \"1275678740809179137\",\n" +
            "      \"code\": \"companyDepart\",\n" +
            "      \"rightEntityClassId\": \"1275678608923484162\",\n" +
            "      \"leftEntityClassId\": \"1275678539314814978\",\n" +
            "      \"leftEntityClassCode\": \"depart\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1275678740809179137\",\n" +
            "        \"name\": \"companyDepart.id\",\n" +
            "        \"cname\": \"公司组织\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }, {\n" +
            "      \"id\": \"1275678844093915137\",\n" +
            "      \"code\": \"departPerson\",\n" +
            "      \"rightEntityClassId\": \"1263391796170928130\",\n" +
            "      \"leftEntityClassId\": \"1275678539314814978\",\n" +
            "      \"leftEntityClassCode\": \"depart\",\n" +
            "      \"relationType\": 2\n" +
            "    }, {\n" +
            "      \"id\": \"1275678844106498050\",\n" +
            "      \"code\": \"departPerson\",\n" +
            "      \"rightEntityClassId\": \"1275678539314814978\",\n" +
            "      \"leftEntityClassId\": \"1263391796170928130\",\n" +
            "      \"leftEntityClassCode\": \"person\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1275678844106498050\",\n" +
            "        \"name\": \"departPerson.id\",\n" +
            "        \"cname\": \"组织人员\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }, {\n" +
            "      \"id\": \"1395571833134510081\",\n" +
            "      \"code\": \"mulitiTest\",\n" +
            "      \"rightEntityClassId\": \"1275678539314814978\",\n" +
            "      \"leftEntityClassId\": \"1263391796170928130\",\n" +
            "      \"leftEntityClassCode\": \"person\",\n" +
            "      \"relationType\": 5,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1395571833134510081\",\n" +
            "        \"name\": \"mulitiTest.id\",\n" +
            "        \"cname\": \"多值测试\",\n" +
            "        \"fieldType\": \"STRINGS\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }, {\n" +
            "      \"id\": \"1337278632578109441\",\n" +
            "      \"code\": \"appOrg\",\n" +
            "      \"rightEntityClassId\": \"1275678539314814978\",\n" +
            "      \"leftEntityClassId\": \"1267402804498194434\",\n" +
            "      \"leftEntityClassCode\": \"app\",\n" +
            "      \"relationType\": 1,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1337278632578109441\",\n" +
            "        \"name\": \"appOrg.id\",\n" +
            "        \"cname\": \"应用组织\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }, {\n" +
            "      \"id\": \"1275678740767236097\",\n" +
            "      \"code\": \"companyDepart\",\n" +
            "      \"rightEntityClassId\": \"1275678539314814978\",\n" +
            "      \"leftEntityClassId\": \"1275678608923484162\",\n" +
            "      \"leftEntityClassCode\": \"company\",\n" +
            "      \"relationType\": 2\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"company\",\n" +
            "    \"id\": \"1275678608923484162\",\n" +
            "    \"name\": \"公司\",\n" +
            "    \"version\": 2016,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1275678609447772162\",\n" +
            "      \"name\": \"name\",\n" +
            "      \"cname\": \"公司名称\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"displayType\": \"1\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1275678609468743682\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1275678609489715201\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1275678609506492417\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1275678609535852545\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1275678609573601282\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1275678609590378497\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1275678609611350018\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1275678609628127234\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1275678609653293058\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1275678609670070274\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1302848451101048834\",\n" +
            "      \"name\": \"type\",\n" +
            "      \"cname\": \"公司性质\",\n" +
            "      \"fieldType\": \"ENUM\",\n" +
            "      \"dictId\": \"1245227711207960577\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1302848451327541249\",\n" +
            "      \"name\": \"registration_time\",\n" +
            "      \"cname\": \"注册时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1302848451356901378\",\n" +
            "      \"name\": \"is_listed\",\n" +
            "      \"cname\": \"是否上市公司\",\n" +
            "      \"fieldType\": \"BOOLEAN\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1302848451390455810\",\n" +
            "      \"name\": \"registered_capital\",\n" +
            "      \"cname\": \"注册资金\",\n" +
            "      \"fieldType\": \"DECIMAL\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"precision\": 2,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }],\n" +
            "    \"relations\": [{\n" +
            "      \"id\": \"1275678740767236097\",\n" +
            "      \"code\": \"companyDepart\",\n" +
            "      \"rightEntityClassId\": \"1275678539314814978\",\n" +
            "      \"leftEntityClassId\": \"1275678608923484162\",\n" +
            "      \"leftEntityClassCode\": \"company\",\n" +
            "      \"relationType\": 2\n" +
            "    }, {\n" +
            "      \"id\": \"1336605543354769410\",\n" +
            "      \"code\": \"companyInvoice\",\n" +
            "      \"rightEntityClassId\": \"1268111464543596546\",\n" +
            "      \"leftEntityClassId\": \"1275678608923484162\",\n" +
            "      \"leftEntityClassCode\": \"company\",\n" +
            "      \"relationType\": 1,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1336605543354769410\",\n" +
            "        \"name\": \"companyInvoice.id\",\n" +
            "        \"cname\": \"公司票据\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }, {\n" +
            "      \"id\": \"1385506135078809601\",\n" +
            "      \"code\": \"parent\",\n" +
            "      \"rightEntityClassId\": \"1275678608923484162\",\n" +
            "      \"leftEntityClassId\": \"1275678608923484162\",\n" +
            "      \"leftEntityClassCode\": \"company\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1385506135078809601\",\n" +
            "        \"name\": \"parent.id\",\n" +
            "        \"cname\": \"母公司\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }, {\n" +
            "      \"id\": \"1385506186505170946\",\n" +
            "      \"code\": \"childrenMTO\",\n" +
            "      \"rightEntityClassId\": \"1275678608923484162\",\n" +
            "      \"leftEntityClassId\": \"1275678608923484162\",\n" +
            "      \"leftEntityClassCode\": \"company\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1385506186505170946\",\n" +
            "        \"name\": \"childrenMTO.id\",\n" +
            "        \"cname\": \"子公司\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }, {\n" +
            "      \"id\": \"1385506186538725378\",\n" +
            "      \"code\": \"children\",\n" +
            "      \"rightEntityClassId\": \"1275678608923484162\",\n" +
            "      \"leftEntityClassId\": \"1275678608923484162\",\n" +
            "      \"leftEntityClassCode\": \"company\",\n" +
            "      \"relationType\": 2\n" +
            "    }, {\n" +
            "      \"id\": \"1275679197380141058\",\n" +
            "      \"code\": \"companyPerson\",\n" +
            "      \"rightEntityClassId\": \"1275678608923484162\",\n" +
            "      \"leftEntityClassId\": \"1263391796170928130\",\n" +
            "      \"leftEntityClassCode\": \"person\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1275679197380141058\",\n" +
            "        \"name\": \"companyPerson.id\",\n" +
            "        \"cname\": \"人员公司\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }, {\n" +
            "      \"id\": \"1275680087679885314\",\n" +
            "      \"code\": \"otherCompany\",\n" +
            "      \"rightEntityClassId\": \"1275678608923484162\",\n" +
            "      \"leftEntityClassId\": \"1263391796170928130\",\n" +
            "      \"leftEntityClassCode\": \"person\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1275680087679885314\",\n" +
            "        \"name\": \"otherCompany.id\",\n" +
            "        \"cname\": \"外包公司\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }, {\n" +
            "      \"id\": \"1275678740809179137\",\n" +
            "      \"code\": \"companyDepart\",\n" +
            "      \"rightEntityClassId\": \"1275678608923484162\",\n" +
            "      \"leftEntityClassId\": \"1275678539314814978\",\n" +
            "      \"leftEntityClassCode\": \"depart\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1275678740809179137\",\n" +
            "        \"name\": \"companyDepart.id\",\n" +
            "        \"cname\": \"公司组织\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"childTest\",\n" +
            "    \"id\": \"1304253852310900738\",\n" +
            "    \"name\": \"子类测试\",\n" +
            "    \"father\": \"1275678608923484162\",\n" +
            "    \"level\": 1,\n" +
            "    \"version\": 2001,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1304253852608696321\",\n" +
            "      \"name\": \"fieldstr\",\n" +
            "      \"cname\": \"fieldstr\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1304253852663222274\",\n" +
            "      \"name\": \"fieldbool\",\n" +
            "      \"cname\": \"fieldbool\",\n" +
            "      \"fieldType\": \"BOOLEAN\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1304253852705165313\",\n" +
            "      \"name\": \"fieldlong\",\n" +
            "      \"cname\": \"fieldlong\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1304253852747108354\",\n" +
            "      \"name\": \"fieldserial\",\n" +
            "      \"cname\": \"fieldserial\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1304253852797440002\",\n" +
            "      \"name\": \"fieldenum\",\n" +
            "      \"cname\": \"fieldenum\",\n" +
            "      \"fieldType\": \"ENUM\",\n" +
            "      \"dictId\": \"1302848623793127426\",\n" +
            "      \"defaultValue\": \"m\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1304253852822605826\",\n" +
            "      \"name\": \"fieldtime\",\n" +
            "      \"cname\": \"fieldtime\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1304253852856160258\",\n" +
            "      \"name\": \"fielddouble\",\n" +
            "      \"cname\": \"fielddouble\",\n" +
            "      \"fieldType\": \"DECIMAL\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1304253852885520386\",\n" +
            "      \"name\": \"fieldstrs\",\n" +
            "      \"cname\": \"fieldstrs\",\n" +
            "      \"fieldType\": \"STRINGS\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"testsave7\",\n" +
            "    \"id\": \"1309381492597805057\",\n" +
            "    \"name\": \"testsave7\",\n" +
            "    \"version\": 2000,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1309381493424082946\",\n" +
            "      \"name\": \"dfdsf\",\n" +
            "      \"cname\": \"sdfsdf\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1309381495642869762\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1309381497047961602\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1309381498897649666\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1309381500491485185\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1309381501921742850\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1309381503318446081\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1309381505138774018\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1309381506715832321\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1309381508099952641\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1309381509685399554\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"test9\",\n" +
            "    \"id\": \"1311131393018982401\",\n" +
            "    \"name\": \"test9\",\n" +
            "    \"version\": 2000,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1311131393589407745\",\n" +
            "      \"name\": \"ffff\",\n" +
            "      \"cname\": \"ffff\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311131394709286913\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311131395548147714\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311131396416368641\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311131397225869313\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311131398110867457\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311131398966505473\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311131399813754881\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311131400652615681\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311131401462116354\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311131402321948673\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1338371733870227458\",\n" +
            "      \"name\": \"sdf12\",\n" +
            "      \"cname\": \"sdfsd\",\n" +
            "      \"fieldType\": \"ENUM\",\n" +
            "      \"dictId\": \"1318121107692126209\",\n" +
            "      \"defaultValue\": \"GET\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"testa\",\n" +
            "    \"id\": \"1311134927391191042\",\n" +
            "    \"name\": \"testa\",\n" +
            "    \"version\": 2000,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1311134927865147394\",\n" +
            "      \"name\": \"sdfsd\",\n" +
            "      \"cname\": \"sdfsdfsd\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311134928712396801\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311134929547063297\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311134930373341185\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311134931057012737\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311134931778433026\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311134932562767873\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311134933577789442\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311134934391484417\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311134935150653442\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311134935897239553\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"testb\",\n" +
            "    \"id\": \"1311139676484169729\",\n" +
            "    \"name\": \"testb\",\n" +
            "    \"version\": 2000,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1311139677142675458\",\n" +
            "      \"name\": \"dsfsd\",\n" +
            "      \"cname\": \"fsdfsdf\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311139678392578049\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311139679701200898\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311139680695250945\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311139681735438338\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311139682784014338\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311139684205883394\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311139685128630273\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311139686156234754\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311139687167062018\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311139688148529154\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"testc\",\n" +
            "    \"id\": \"1311141420551270402\",\n" +
            "    \"name\": \"testc\",\n" +
            "    \"version\": 2000,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1311141421050392578\",\n" +
            "      \"name\": \"sdfsd\",\n" +
            "      \"cname\": \"fsdfs\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311141422002499586\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311141422979772417\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311141424015765506\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311141424896569346\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311141425840287745\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311141426771423233\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311141427803222017\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311141428721774593\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311141429728407553\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311141430940561409\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"testd\",\n" +
            "    \"id\": \"1311189520414326785\",\n" +
            "    \"name\": \"testd\",\n" +
            "    \"version\": 2000,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1311189521169301506\",\n" +
            "      \"name\": \"sdadfs\",\n" +
            "      \"cname\": \"sddfs\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311189521676812289\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311189522054299650\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311189522519867394\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311189522914131970\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311189523417448450\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311189523820101633\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311189524289863682\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311189524805763073\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311189525208416258\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311189525594292226\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"teste\",\n" +
            "    \"id\": \"1311202546676850689\",\n" +
            "    \"name\": \"teste\",\n" +
            "    \"version\": 2000,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1311202547771564033\",\n" +
            "      \"name\": \"sdfsdf\",\n" +
            "      \"cname\": \"sdfsdf\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311202548379738114\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311202548908220418\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311202549382176770\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311202549759664130\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311202550271369218\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311202550720159746\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311202551156367361\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311202551542243330\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311202551919730690\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1311202552284635137\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"actionTrail\",\n" +
            "    \"id\": \"1315859164940939266\",\n" +
            "    \"name\": \"行为审计\",\n" +
            "    \"version\": 2000,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1315859165221957633\",\n" +
            "      \"name\": \"userName\",\n" +
            "      \"cname\": \"操作用户\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1315859165251317762\",\n" +
            "      \"name\": \"eventTime\",\n" +
            "      \"cname\": \"操作时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1315859165276483586\",\n" +
            "      \"name\": \"eventType\",\n" +
            "      \"cname\": \"操作\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1315859165305843714\",\n" +
            "      \"name\": \"requestId\",\n" +
            "      \"cname\": \"请求ID\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1315859165331009538\",\n" +
            "      \"name\": \"httpMethod\",\n" +
            "      \"cname\": \"请求方式\",\n" +
            "      \"fieldType\": \"ENUM\",\n" +
            "      \"dictId\": \"1318121107692126209\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1315859165360369666\",\n" +
            "      \"name\": \"uri\",\n" +
            "      \"cname\": \"请求url\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1315859165393924097\",\n" +
            "      \"name\": \"ip\",\n" +
            "      \"cname\": \"请求IP\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1315859165423284225\",\n" +
            "      \"name\": \"userInfo\",\n" +
            "      \"cname\": \"用户信息\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1315859165570084866\",\n" +
            "      \"name\": \"requestBody\",\n" +
            "      \"cname\": \"请求内容\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1315859165616222209\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1315859165649776642\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1315859165679136769\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1315859165704302593\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1315859165746245633\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1315859165775605762\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1315859165809160193\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1315859165838520321\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1315859165867880449\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1315859165893046274\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"objectIndex\",\n" +
            "    \"id\": \"1319299558087929857\",\n" +
            "    \"name\": \"对象索引\",\n" +
            "    \"version\": 1098,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1319299558209564674\",\n" +
            "      \"name\": \"name\",\n" +
            "      \"cname\": \"主键名称\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1319299558285062146\",\n" +
            "      \"name\": \"remark\",\n" +
            "      \"cname\": \"备注\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1319299558385725442\",\n" +
            "      \"name\": \"fieldCodes\",\n" +
            "      \"cname\": \"字段\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1319299558419279873\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1319299558457028610\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1319299558490583042\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1319299558545108994\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1319299558578663426\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1319299558645772290\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1319299558868070402\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1319299558905819138\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1319299558935179265\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1319299558972928002\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"assetchild\",\n" +
            "    \"id\": \"1321300992379789314\",\n" +
            "    \"name\": \"assetchild\",\n" +
            "    \"level\": 1,\n" +
            "    \"version\": 1097,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1321300993088626690\",\n" +
            "      \"name\": \"test_code\",\n" +
            "      \"cname\": \"testCode\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"personCompany\",\n" +
            "    \"id\": \"1336568713741770754\",\n" +
            "    \"name\": \"个人有限公司\",\n" +
            "    \"father\": \"1275678608923484162\",\n" +
            "    \"level\": 1,\n" +
            "    \"version\": 1097,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1336568713804685313\",\n" +
            "      \"name\": \"person\",\n" +
            "      \"cname\": \"法人\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1336604679080681473\",\n" +
            "      \"name\": \"personalName\",\n" +
            "      \"cname\": \"个人名称\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1385505134586310658\",\n" +
            "      \"name\": \"companyType\",\n" +
            "      \"cname\": \"公司性质\",\n" +
            "      \"fieldType\": \"ENUM\",\n" +
            "      \"dictId\": \"1245227711207960577\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"bigPerson\",\n" +
            "    \"id\": \"1351129912773951489\",\n" +
            "    \"name\": \"大人\",\n" +
            "    \"father\": \"1263391796170928130\",\n" +
            "    \"level\": 1,\n" +
            "    \"version\": 1084,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1351129912887197698\",\n" +
            "      \"name\": \"code\",\n" +
            "      \"cname\": \"代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1352537332724350978\",\n" +
            "      \"name\": \"testdel\",\n" +
            "      \"cname\": \"testdel\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"dddtest\",\n" +
            "    \"id\": \"1366227203765637121\",\n" +
            "    \"name\": \"默认值测试\",\n" +
            "    \"version\": 1065,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1366227204088598530\",\n" +
            "      \"name\": \"default_a\",\n" +
            "      \"cname\": \"默认A\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366227204126347265\",\n" +
            "      \"name\": \"default_b\",\n" +
            "      \"cname\": \"默认B\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366227204143124481\",\n" +
            "      \"name\": \"default_c\",\n" +
            "      \"cname\": \"默认C\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"defaultValue\": \"hello\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366227204159901698\",\n" +
            "      \"name\": \"default_d\",\n" +
            "      \"cname\": \"默认D\",\n" +
            "      \"fieldType\": \"ENUM\",\n" +
            "      \"dictId\": \"1302848623793127426\",\n" +
            "      \"defaultValue\": \"m\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366227204172484610\",\n" +
            "      \"name\": \"default_e\",\n" +
            "      \"cname\": \"默认E\",\n" +
            "      \"fieldType\": \"ENUM\",\n" +
            "      \"dictId\": \"1302848623793127426\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366227204185067522\",\n" +
            "      \"name\": \"default_f\",\n" +
            "      \"cname\": \"默认F\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"dictId\": \"1302848623793127426\",\n" +
            "      \"defaultValue\": \"m\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366227204201844738\",\n" +
            "      \"name\": \"default_g\",\n" +
            "      \"cname\": \"默认G\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"defaultValue\": \"0\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366227204214427650\",\n" +
            "      \"name\": \"default_h\",\n" +
            "      \"cname\": \"默认H\",\n" +
            "      \"fieldType\": \"STRINGS\",\n" +
            "      \"defaultValue\": \"sdsd\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366227204231204866\",\n" +
            "      \"name\": \"default_i\",\n" +
            "      \"cname\": \"默认I\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"defaultValue\": \"122\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366227204243787778\",\n" +
            "      \"name\": \"default_j\",\n" +
            "      \"cname\": \"默认J\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"defaultValue\": \"22\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366227204256370690\",\n" +
            "      \"name\": \"default_k\",\n" +
            "      \"cname\": \"默认K\",\n" +
            "      \"fieldType\": \"DECIMAL\",\n" +
            "      \"defaultValue\": \"0.1\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366227204264759297\",\n" +
            "      \"name\": \"default_l\",\n" +
            "      \"cname\": \"默认L\",\n" +
            "      \"fieldType\": \"BOOLEAN\",\n" +
            "      \"defaultValue\": \"true\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366227204277342210\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366227204289925121\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366227204298313729\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366227204310896642\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366227204323479553\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366227204336062466\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366227204348645378\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366227204361228289\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366227204369616898\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366227204386394114\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"invoiceBill\",\n" +
            "    \"id\": \"1366326718728310785\",\n" +
            "    \"name\": \"票据\",\n" +
            "    \"version\": 1064,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1366326718791225346\",\n" +
            "      \"name\": \"billNo\",\n" +
            "      \"cname\": \"单据号\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366326718808002561\",\n" +
            "      \"name\": \"invoiceNo\",\n" +
            "      \"cname\": \"发票号\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366326718828974082\",\n" +
            "      \"name\": \"amount\",\n" +
            "      \"cname\": \"金额\",\n" +
            "      \"fieldType\": \"DECIMAL\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"precision\": 2,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366326718841556993\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366326718854139906\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366326718866722817\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366326718879305729\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366326718887694337\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366326718900277249\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366326718908665858\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366326718921248769\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366326718929637377\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1366326718942220289\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"mobiletest\",\n" +
            "    \"id\": \"1375361236081782786\",\n" +
            "    \"name\": \"移动端测试对象\",\n" +
            "    \"version\": 1062,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1375361236148891650\",\n" +
            "      \"name\": \"name\",\n" +
            "      \"cname\": \"名称\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1375361236165668866\",\n" +
            "      \"name\": \"price\",\n" +
            "      \"cname\": \"价格\",\n" +
            "      \"fieldType\": \"DECIMAL\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1375361236182446081\",\n" +
            "      \"name\": \"inputnumberrange\",\n" +
            "      \"cname\": \"数值区间\",\n" +
            "      \"fieldType\": \"DECIMAL\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1375361236203417602\",\n" +
            "      \"name\": \"time\",\n" +
            "      \"cname\": \"时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1375361236220194817\",\n" +
            "      \"name\": \"timerange\",\n" +
            "      \"cname\": \"时间区间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1375361236232777730\",\n" +
            "      \"name\": \"date\",\n" +
            "      \"cname\": \"日期\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1375361236241166338\",\n" +
            "      \"name\": \"daterange\",\n" +
            "      \"cname\": \"日期区间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1375361236249554946\",\n" +
            "      \"name\": \"month\",\n" +
            "      \"cname\": \"月份\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1375361236262137858\",\n" +
            "      \"name\": \"monthrange\",\n" +
            "      \"cname\": \"月份区间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1375361236270526465\",\n" +
            "      \"name\": \"year\",\n" +
            "      \"cname\": \"年\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1375361236283109378\",\n" +
            "      \"name\": \"select\",\n" +
            "      \"cname\": \"select\",\n" +
            "      \"fieldType\": \"ENUM\",\n" +
            "      \"dictId\": \"1271016796095225858\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1375361236291497985\",\n" +
            "      \"name\": \"radio\",\n" +
            "      \"cname\": \"radio\",\n" +
            "      \"fieldType\": \"BOOLEAN\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1375361236308275201\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1375361236316663810\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1375361236325052418\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1375361236337635330\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1375361236346023937\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1375361236354412545\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1375361236366995458\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1375361236375384065\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1375361236387966978\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1375361236396355586\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }],\n" +
            "    \"relations\": [{\n" +
            "      \"id\": \"1375361355044827137\",\n" +
            "      \"code\": \"ontomany\",\n" +
            "      \"rightEntityClassId\": \"1268111464543596546\",\n" +
            "      \"leftEntityClassId\": \"1375361236081782786\",\n" +
            "      \"leftEntityClassCode\": \"mobiletest\",\n" +
            "      \"relationType\": 2\n" +
            "    }, {\n" +
            "      \"id\": \"1375361355015467009\",\n" +
            "      \"code\": \"ontomany\",\n" +
            "      \"rightEntityClassId\": \"1375361236081782786\",\n" +
            "      \"leftEntityClassId\": \"1268111464543596546\",\n" +
            "      \"leftEntityClassCode\": \"invoice\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1375361355015467009\",\n" +
            "        \"name\": \"ontomany.id\",\n" +
            "        \"cname\": \"一对多\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"formulaEntity\",\n" +
            "    \"id\": \"1391633432916127746\",\n" +
            "    \"name\": \"公式对象\",\n" +
            "    \"version\": 1030,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1391633433058734081\",\n" +
            "      \"name\": \"name\",\n" +
            "      \"cname\": \"名称\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1391633433096482818\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1391633433113260034\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1391633433121648642\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1391633433134231554\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1391633433151008769\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1391633433159397378\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1391633433176174594\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1391633433188757506\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1391633433192951809\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1391633433201340417\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"aPerson\",\n" +
            "    \"id\": \"1392404401170087937\",\n" +
            "    \"name\": \"aPerson\",\n" +
            "    \"father\": \"1263391796170928130\",\n" +
            "    \"level\": 1,\n" +
            "    \"version\": 1011,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1392404401560158210\",\n" +
            "      \"name\": \"test\",\n" +
            "      \"cname\": \"level2\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }],\n" +
            "    \"relations\": [{\n" +
            "      \"id\": \"1394233705765781505\",\n" +
            "      \"code\": \"level2\",\n" +
            "      \"rightEntityClassId\": \"1267402804498194434\",\n" +
            "      \"leftEntityClassId\": \"1392404401170087937\",\n" +
            "      \"leftEntityClassCode\": \"aPerson\",\n" +
            "      \"relationType\": 2\n" +
            "    }, {\n" +
            "      \"id\": \"1394233705749004290\",\n" +
            "      \"code\": \"level2\",\n" +
            "      \"rightEntityClassId\": \"1392404401170087937\",\n" +
            "      \"leftEntityClassId\": \"1267402804498194434\",\n" +
            "      \"leftEntityClassCode\": \"app\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1394233705749004290\",\n" +
            "        \"name\": \"level2.id\",\n" +
            "        \"cname\": \"level2\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"bPerson\",\n" +
            "    \"id\": \"1392404559869968385\",\n" +
            "    \"name\": \"bPerson\",\n" +
            "    \"father\": \"1392404401170087937\",\n" +
            "    \"level\": 2,\n" +
            "    \"version\": 1011,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1392404560184541185\",\n" +
            "      \"name\": \"testb\",\n" +
            "      \"cname\": \"level3\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }],\n" +
            "    \"relations\": [{\n" +
            "      \"id\": \"1394233797222580225\",\n" +
            "      \"code\": \"level3\",\n" +
            "      \"rightEntityClassId\": \"1267402804498194434\",\n" +
            "      \"leftEntityClassId\": \"1392404559869968385\",\n" +
            "      \"leftEntityClassCode\": \"bPerson\",\n" +
            "      \"relationType\": 2\n" +
            "    }, {\n" +
            "      \"id\": \"1394233797209997314\",\n" +
            "      \"code\": \"level3\",\n" +
            "      \"rightEntityClassId\": \"1392404559869968385\",\n" +
            "      \"leftEntityClassId\": \"1267402804498194434\",\n" +
            "      \"leftEntityClassCode\": \"app\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1394233797209997314\",\n" +
            "        \"name\": \"level3.id\",\n" +
            "        \"cname\": \"level3\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"cPerson\",\n" +
            "    \"id\": \"1392404671652364290\",\n" +
            "    \"name\": \"cPerson\",\n" +
            "    \"father\": \"1392404559869968385\",\n" +
            "    \"level\": 3,\n" +
            "    \"version\": 1011,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1392404672004685826\",\n" +
            "      \"name\": \"testc\",\n" +
            "      \"cname\": \"level4\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }],\n" +
            "    \"relations\": [{\n" +
            "      \"id\": \"1394233859931619329\",\n" +
            "      \"code\": \"level4\",\n" +
            "      \"rightEntityClassId\": \"1251658380868685825\",\n" +
            "      \"leftEntityClassId\": \"1392404671652364290\",\n" +
            "      \"leftEntityClassCode\": \"cPerson\",\n" +
            "      \"relationType\": 2\n" +
            "    }, {\n" +
            "      \"id\": \"1394233859919036417\",\n" +
            "      \"code\": \"level4\",\n" +
            "      \"rightEntityClassId\": \"1392404671652364290\",\n" +
            "      \"leftEntityClassId\": \"1251658380868685825\",\n" +
            "      \"leftEntityClassCode\": \"component\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1394233859919036417\",\n" +
            "        \"name\": \"level4.id\",\n" +
            "        \"cname\": \"level4\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"dPerson\",\n" +
            "    \"id\": \"1392404791726899201\",\n" +
            "    \"name\": \"dPerson\",\n" +
            "    \"father\": \"1392404671652364290\",\n" +
            "    \"level\": 4,\n" +
            "    \"version\": 1011,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1392404792020500482\",\n" +
            "      \"name\": \"numd\",\n" +
            "      \"cname\": \"level5\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }],\n" +
            "    \"relations\": [{\n" +
            "      \"id\": \"1394233957252055041\",\n" +
            "      \"code\": \"level5\",\n" +
            "      \"rightEntityClassId\": \"1267402804498194434\",\n" +
            "      \"leftEntityClassId\": \"1392404791726899201\",\n" +
            "      \"leftEntityClassCode\": \"dPerson\",\n" +
            "      \"relationType\": 2\n" +
            "    }, {\n" +
            "      \"id\": \"1394233957235277826\",\n" +
            "      \"code\": \"level5\",\n" +
            "      \"rightEntityClassId\": \"1392404791726899201\",\n" +
            "      \"leftEntityClassId\": \"1267402804498194434\",\n" +
            "      \"leftEntityClassCode\": \"app\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1394233957235277826\",\n" +
            "        \"name\": \"level5.id\",\n" +
            "        \"cname\": \"level5\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }],\n" +
            "    \"profiles\": [{\n" +
            "      \"code\": \"CTC\",\n" +
            "      \"entityFieldInfo\": [{\n" +
            "        \"id\": \"1394616249225416706\",\n" +
            "        \"name\": \"objd\",\n" +
            "        \"cname\": \"objd\",\n" +
            "        \"fieldType\": \"STRING\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"max\": \"20\",\n" +
            "          \"isRequired\": true,\n" +
            "          \"displayType\": \"0\",\n" +
            "          \"metaFieldSense\": \"NORMAL\",\n" +
            "          \"fuzzyType\": 1,\n" +
            "          \"wildcardMinWidth\": 3,\n" +
            "          \"wildcardMaxWidth\": 6\n" +
            "        },\n" +
            "        \"calculator\": {\n" +
            "          \"calculateType\": 1\n" +
            "        }\n" +
            "      }]\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"childTest3\",\n" +
            "    \"id\": \"1394239130636632066\",\n" +
            "    \"name\": \"childTest3\",\n" +
            "    \"father\": \"1304253852310900738\",\n" +
            "    \"level\": 2,\n" +
            "    \"version\": 55,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1394239130888290306\",\n" +
            "      \"name\": \"level3\",\n" +
            "      \"cname\": \"level3\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"childTest4\",\n" +
            "    \"id\": \"1394239218805096450\",\n" +
            "    \"name\": \"childTest4\",\n" +
            "    \"father\": \"1394239130636632066\",\n" +
            "    \"level\": 3,\n" +
            "    \"version\": 55,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1394239219094503426\",\n" +
            "      \"name\": \"level4\",\n" +
            "      \"cname\": \"level4\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"appLevel1\",\n" +
            "    \"id\": \"1394945267914412034\",\n" +
            "    \"name\": \"应用扩展一层\",\n" +
            "    \"father\": \"1267402804498194434\",\n" +
            "    \"level\": 1,\n" +
            "    \"version\": 40,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1394945268195430401\",\n" +
            "      \"name\": \"levelName\",\n" +
            "      \"cname\": \"一层名称\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"appLevel2\",\n" +
            "    \"id\": \"1394946043051151362\",\n" +
            "    \"name\": \"应用扩展二层\",\n" +
            "    \"father\": \"1394945267914412034\",\n" +
            "    \"level\": 2,\n" +
            "    \"version\": 40,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1394946043327975426\",\n" +
            "      \"name\": \"appLevel2Name\",\n" +
            "      \"cname\": \"应用扩展二层名称\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1394951504651522050\",\n" +
            "      \"name\": \"appLevel2Amount1\",\n" +
            "      \"cname\": \"应用 扩展二层金额\",\n" +
            "      \"fieldType\": \"DECIMAL\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"precision\": 6,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 2,\n" +
            "        \"expression\": \"${amount}\",\n" +
            "        \"step\": 10000,\n" +
            "        \"level\": 1,\n" +
            "        \"args\": [\"amount\"],\n" +
            "        \"failedPolicy\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1394956044939464706\",\n" +
            "      \"name\": \"appLevel2TaxAmount\",\n" +
            "      \"cname\": \"应用扩展二层应用税额\",\n" +
            "      \"fieldType\": \"DECIMAL\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"precision\": 6,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 2,\n" +
            "        \"expression\": \"${taxAmount}\",\n" +
            "        \"step\": 10000,\n" +
            "        \"level\": 2,\n" +
            "        \"args\": [\"taxAmount\"],\n" +
            "        \"failedPolicy\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1394956044943659010\",\n" +
            "      \"name\": \"appLevel2AppNo\",\n" +
            "      \"cname\": \"应用扩展二层引用编号\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 2,\n" +
            "        \"expression\": \"${appNo1}\",\n" +
            "        \"step\": 10000,\n" +
            "        \"level\": 1,\n" +
            "        \"args\": [\"appNo1\"],\n" +
            "        \"failedPolicy\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"appLevel3\",\n" +
            "    \"id\": \"1395220921749803010\",\n" +
            "    \"name\": \" 应用扩展三层\",\n" +
            "    \"father\": \"1394946043051151362\",\n" +
            "    \"level\": 3,\n" +
            "    \"version\": 29,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1395220922093735937\",\n" +
            "      \"name\": \"level3Name\",\n" +
            "      \"cname\": \"第三层应用名\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"appLevel4\",\n" +
            "    \"id\": \"1395221667354447873\",\n" +
            "    \"name\": \" 应用扩展四层\",\n" +
            "    \"father\": \"1395220921749803010\",\n" +
            "    \"level\": 4,\n" +
            "    \"version\": 29,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1395221667673214978\",\n" +
            "      \"name\": \"appLevel4Name\",\n" +
            "      \"cname\": \"应用四层名称\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1395221667681603586\",\n" +
            "      \"name\": \"appLevel4Amount\",\n" +
            "      \"cname\": \"应用四层余额应用首层\",\n" +
            "      \"fieldType\": \"DECIMAL\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"precision\": 6,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 2,\n" +
            "        \"expression\": \"${noTaxAmount}\",\n" +
            "        \"step\": 10000,\n" +
            "        \"level\": 3,\n" +
            "        \"args\": [\"noTaxAmount\"],\n" +
            "        \"failedPolicy\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1395221667689992193\",\n" +
            "      \"name\": \"appLevel4T2Amount\",\n" +
            "      \"cname\": \"应用四层余额应用一层\",\n" +
            "      \"fieldType\": \"DECIMAL\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"precision\": 6,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 2,\n" +
            "        \"expression\": \"${appLevel2TaxAmount}\",\n" +
            "        \"step\": 10000,\n" +
            "        \"level\": 3,\n" +
            "        \"args\": [\"appLevel2TaxAmount\"],\n" +
            "        \"failedPolicy\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"air\",\n" +
            "    \"id\": \"1395650019019907074\",\n" +
            "    \"name\": \"飞机票\",\n" +
            "    \"version\": 24,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1395650019292536834\",\n" +
            "      \"name\": \"ddd\",\n" +
            "      \"cname\": \"电子客单编号\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1395650019300925441\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1395650019309314050\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1395650019313508353\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1395650019317702657\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1395650019326091265\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1395650019330285570\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人ID\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1395650019334479874\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人ID\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1395650019338674177\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1395650019347062786\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1395650019351257090\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1400710060085018625\",\n" +
            "      \"name\": \"ccc\",\n" +
            "      \"cname\": \"挺好\",\n" +
            "      \"fieldType\": \"DECIMAL\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }],\n" +
            "    \"relations\": [{\n" +
            "      \"id\": \"1395650272074850305\",\n" +
            "      \"code\": \"relation\",\n" +
            "      \"rightEntityClassId\": \"1395650144349904897\",\n" +
            "      \"leftEntityClassId\": \"1395650019019907074\",\n" +
            "      \"leftEntityClassCode\": \"air\",\n" +
            "      \"relationType\": 2\n" +
            "    }, {\n" +
            "      \"id\": \"1395650272053878786\",\n" +
            "      \"code\": \"relation\",\n" +
            "      \"rightEntityClassId\": \"1395650019019907074\",\n" +
            "      \"leftEntityClassId\": \"1395650144349904897\",\n" +
            "      \"leftEntityClassCode\": \"airdetail\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1395650272053878786\",\n" +
            "        \"name\": \"relation.id\",\n" +
            "        \"cname\": \"飞机票一对多明细\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"airdetail\",\n" +
            "    \"id\": \"1395650144349904897\",\n" +
            "    \"name\": \"飞机票明细\",\n" +
            "    \"version\": 24,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1395650144593174530\",\n" +
            "      \"name\": \"detail\",\n" +
            "      \"cname\": \"明细\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1395650144601563137\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1395650144605757441\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1395650144614146049\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1395650144618340353\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1395650144622534657\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1395650144630923265\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人ID\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1395650144635117570\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人ID\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1395650144639311873\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1395650144647700481\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1395650144651894786\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }],\n" +
            "    \"relations\": [{\n" +
            "      \"id\": \"1395650272053878786\",\n" +
            "      \"code\": \"relation\",\n" +
            "      \"rightEntityClassId\": \"1395650019019907074\",\n" +
            "      \"leftEntityClassId\": \"1395650144349904897\",\n" +
            "      \"leftEntityClassCode\": \"airdetail\",\n" +
            "      \"relationType\": 3,\n" +
            "      \"entityField\": {\n" +
            "        \"id\": \"1395650272053878786\",\n" +
            "        \"name\": \"relation.id\",\n" +
            "        \"cname\": \"飞机票一对多明细\",\n" +
            "        \"fieldType\": \"LONG\",\n" +
            "        \"fieldConfig\": {\n" +
            "          \"searchable\": true,\n" +
            "          \"metaFieldSense\": \"NORMAL\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"belongToOwner\": true\n" +
            "    }, {\n" +
            "      \"id\": \"1395650272074850305\",\n" +
            "      \"code\": \"relation\",\n" +
            "      \"rightEntityClassId\": \"1395650144349904897\",\n" +
            "      \"leftEntityClassId\": \"1395650019019907074\",\n" +
            "      \"leftEntityClassCode\": \"air\",\n" +
            "      \"relationType\": 2\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"inv\",\n" +
            "    \"id\": \"1397389052517261313\",\n" +
            "    \"name\": \"发票信息\",\n" +
            "    \"version\": 14,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1397389052823445505\",\n" +
            "      \"name\": \"invoiceNo\",\n" +
            "      \"cname\": \"发票号\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6,\n" +
            "        \"uniqueName\": \"inv:IDX_U0:0\"\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1397389052836028417\",\n" +
            "      \"name\": \"invoiceType\",\n" +
            "      \"cname\": \"发票类型\",\n" +
            "      \"fieldType\": \"ENUM\",\n" +
            "      \"dictId\": \"1271016796095225858\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1397389052844417026\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1397389052848611330\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1397389052856999938\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1397389052861194242\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1397389052869582850\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1397389052873777154\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人ID\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1397389052882165762\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人ID\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1397389052886360066\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1397389052894748673\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1397389052898942978\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"testdefault\",\n" +
            "    \"id\": \"1399279274276651010\",\n" +
            "    \"name\": \"testdefault\",\n" +
            "    \"version\": 8,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1399279274582835201\",\n" +
            "      \"name\": \"value1\",\n" +
            "      \"cname\": \"shef\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1399279274591223809\",\n" +
            "      \"name\": \"v2\",\n" +
            "      \"cname\": \"formula2\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 2,\n" +
            "        \"expression\": \"${value1}\",\n" +
            "        \"step\": 10000,\n" +
            "        \"level\": 1,\n" +
            "        \"args\": [\"value1\"],\n" +
            "        \"failedPolicy\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1399279274599612418\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1399279274603806721\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1399279274637361154\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1399279274645749761\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1399279274649944066\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1399279274654138370\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人ID\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1399279274662526978\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人ID\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1399279274666721281\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1399279274675109889\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1399279274679304193\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }, {\n" +
            "    \"code\": \"diffAdd\",\n" +
            "    \"id\": \"1400707957463646209\",\n" +
            "    \"name\": \"新增变更\",\n" +
            "    \"version\": 3,\n" +
            "    \"entityFields\": [{\n" +
            "      \"id\": \"1400707957790801921\",\n" +
            "      \"name\": \"addtest\",\n" +
            "      \"cname\": \"addtest\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"max\": \"20\",\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1400707957799190530\",\n" +
            "      \"name\": \"id\",\n" +
            "      \"cname\": \"id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"identifier\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"NORMAL\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1400707957807579137\",\n" +
            "      \"name\": \"tenant_id\",\n" +
            "      \"cname\": \"租户id\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"TENANT_ID\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1400707957811773441\",\n" +
            "      \"name\": \"tenant_code\",\n" +
            "      \"cname\": \"租户代码\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"TENANT_CODE\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1400707957820162050\",\n" +
            "      \"name\": \"create_time\",\n" +
            "      \"cname\": \"创建时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"CREATE_TIME\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1400707957824356354\",\n" +
            "      \"name\": \"update_time\",\n" +
            "      \"cname\": \"修改时间\",\n" +
            "      \"fieldType\": \"DATETIME\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"UPDATE_TIME\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1400707957828550657\",\n" +
            "      \"name\": \"create_user_id\",\n" +
            "      \"cname\": \"创建人ID\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"CREATE_USER_ID\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1400707957836939265\",\n" +
            "      \"name\": \"update_user_id\",\n" +
            "      \"cname\": \"修改人ID\",\n" +
            "      \"fieldType\": \"LONG\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_ID\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1400707957841133570\",\n" +
            "      \"name\": \"create_user_name\",\n" +
            "      \"cname\": \"创建人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"CREATE_USER_NAME\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1400707957849522178\",\n" +
            "      \"name\": \"update_user_name\",\n" +
            "      \"cname\": \"修改人名字\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"UPDATE_USER_NAME\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"1400707957853716481\",\n" +
            "      \"name\": \"delete_flag\",\n" +
            "      \"cname\": \"删除标记\",\n" +
            "      \"fieldType\": \"STRING\",\n" +
            "      \"fieldConfig\": {\n" +
            "        \"searchable\": true,\n" +
            "        \"isRequired\": true,\n" +
            "        \"displayType\": \"0\",\n" +
            "        \"metaFieldSense\": \"DELETE_FLAG\",\n" +
            "        \"fuzzyType\": 1,\n" +
            "        \"wildcardMinWidth\": 3,\n" +
            "        \"wildcardMaxWidth\": 6\n" +
            "      },\n" +
            "      \"calculator\": {\n" +
            "        \"calculateType\": 1\n" +
            "      }\n" +
            "    }]\n" +
            "  }]\n" +
            "}successfully").toString();
    }
}
