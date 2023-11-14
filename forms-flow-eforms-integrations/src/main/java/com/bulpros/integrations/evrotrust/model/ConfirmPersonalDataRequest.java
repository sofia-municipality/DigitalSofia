package com.bulpros.integrations.evrotrust.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ConfirmPersonalDataRequest extends BaseModelConfirmPersonalDataRequest {

    private Document document;

    @Getter
    @Setter
    public static class Document {
        private Date dateExpire;
    }

}
