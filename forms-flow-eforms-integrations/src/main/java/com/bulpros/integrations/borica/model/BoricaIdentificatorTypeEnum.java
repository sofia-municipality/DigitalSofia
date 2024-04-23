package com.bulpros.integrations.borica.model;

public enum BoricaIdentificatorTypeEnum {
    EGN, LNC, PHONE, EMAIL;

    public static BoricaIdentificatorTypeEnum fromKey(String key) {
        for (BoricaIdentificatorTypeEnum item: BoricaIdentificatorTypeEnum.values()) {
            if (item.name().equals(key)) {
                return item;
            }
        }
        return null;
    }
}
