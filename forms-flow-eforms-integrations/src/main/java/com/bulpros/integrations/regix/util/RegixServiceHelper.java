package com.bulpros.integrations.regix.util;

import com.bulpros.integrations.exceptions.IntegrationServiceErrorException;
import com.bulpros.integrations.regix.model.client.DataContainer;
import com.bulpros.integrations.regix.model.client.ResponseContainer;
import com.bulpros.integrations.regix.model.client.ServiceResultData;
import java.util.Objects;

public class RegixServiceHelper {
    public static synchronized ResponseContainer getResponseData(ServiceResultData response){
        if(Objects.isNull(response)) {
            throw new IntegrationServiceErrorException("Regix Result Data Response is null!");
        }
        if(response.isHasError()) {
            throw new IntegrationServiceErrorException("Regix error message: " + response.getError());
        }
        if(!response.isIsReady()) {
            throw new IntegrationServiceErrorException("Regix IsReady flag is false!");
        }
        DataContainer dataContainer = response.getData();
        if(Objects.isNull(dataContainer)) {
            throw new IntegrationServiceErrorException("Data Container is null!");
        }
        ResponseContainer responseContainer = dataContainer.getResponse();
        if(Objects.isNull(responseContainer)) {
            throw new IntegrationServiceErrorException("Response Container is null!");
        }
        return responseContainer;
    }
}