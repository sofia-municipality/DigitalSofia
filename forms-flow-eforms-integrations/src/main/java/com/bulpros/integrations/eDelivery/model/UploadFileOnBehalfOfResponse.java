


package com.bulpros.integrations.eDelivery.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UploadFileOnBehalfOfResponse {
    String name;
    String size;
    String hashAlgorithm;
    String hash;
    Long blobId;
    String malwareScanStatus;
    String signatureStatus;
    String errorStatus;
}
