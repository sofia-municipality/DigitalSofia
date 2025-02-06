
package com.bulpros.integrations.eDelivery.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;

@Data
public class SendMessageOnBehalfOfRequest extends  SendMessageRequest{
    String senderProfileId;
}
