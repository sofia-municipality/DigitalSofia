package com.bulpros.integrations.exceptions;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AgentWSExceptionBody {
    private final int code;
    private final String status;
    private final String message;
    private final String developerMessage;
    private final String moreInfoUrl;

}
