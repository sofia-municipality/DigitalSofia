
package com.bulpros.integrations.eDelivery.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;

@Data
public class SendMessageOnBehalfOfRequest {
    ArrayList<Integer> recipientProfileIds;
    String senderProfileId;
    String subject;
    String referencedOrn;
    String additionalIdentifier;

    String templateId;
    @JsonProperty("fields")
    HashMap<String, Object> fields;

}
