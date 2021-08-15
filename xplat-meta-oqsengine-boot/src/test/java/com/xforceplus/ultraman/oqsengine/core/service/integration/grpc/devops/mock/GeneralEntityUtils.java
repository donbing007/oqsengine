package com.xforceplus.ultraman.oqsengine.core.service.integration.grpc.devops.mock;

/**
 * Created by justin.xu on 08/2021.
 *
 * @since 1.8
 */
public class GeneralEntityUtils {

    /**
     * entityClassHelper
     */
    public static class EntityClassHelper {
        /**
         * 产生父ID
         */
        public static long fatherId(long id) {
            return id + GeneralConstant.MOCK_FATHER_DISTANCE;
        }

        /**
         * 产生祖先ID
         */
        public static long ancId(long id) {
            return id + GeneralConstant.MOCK_ANC_DISTANCE;
        }
    }

    /**
     * entityFieldHelper
     */
    public static class EntityFieldHelper {

        /**
         *  产生id
         */
        public static long id(long id, boolean isProfile) {
            return isProfile ? id + GeneralConstant.MOCK_PROFILE_E_DISTANCE : id;
        }

        /**
         * 产生 name
         */
        public static String name(long id) {
            return id + GeneralConstant.E + GeneralConstant.NAME_SUFFIX;
        }

        /**
         * 产生 cname
         */
        public static String cname(long id) {
            return id + GeneralConstant.E + GeneralConstant.CNAME_SUFFIX;
        }

        /**
         * 产生 dictId
         */
        public static String dictId(long id) {
            return id + GeneralConstant.E + GeneralConstant.DICT_ID_SUFFIX;
        }
    }

    /**
     * relationHelper
     */
    public static class RelationHelper {

        /**
         * 产生belongTo
         */
        public static boolean belongTo(long id) {
            return id % 2 == 0;
        }

        /**
         * 产生code
         */
        public static String code(long id) {
            return id + GeneralConstant.R + GeneralConstant.CODE_SUFFIX;
        }
    }
}
