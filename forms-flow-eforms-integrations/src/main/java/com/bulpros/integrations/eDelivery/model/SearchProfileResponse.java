package com.bulpros.integrations.eDelivery.model;

import lombok.Data;

@Data
public class SearchProfileResponse {
    private String profileId;
    private String identifier;
    private String name;
    private String email;
    private String phone;
}
