package com.bulpros.integrations.evrotrust.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EvrotrustDeliveryReceiptsStatusResponse {
    List<Thread> threads;

    @Getter
    @Setter
    public static class Thread {
        String threadID;
        Boolean threadFound;
        List<ThreadStatus> statuses;
    }
    @Getter
    @Setter
    public static class ThreadStatus {
        String transactionID;
        List<List<Evidence>> evidences;
    }

    @Getter
    @Setter
    public static class Evidence {
        Integer type;
        Integer status;
    }

}
