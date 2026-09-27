package com.linuxremediation.Engine.strategy;

import com.linuxremediation.Engine.domain.RemediationReport;
import com.linuxremediation.Engine.dto.AlertPayloadDTO;
import com.linuxremediation.Engine.dto.RemediationContext;

public interface RemediationStrategy {
    boolean supports(AlertPayloadDTO alert);
    RemediationReport service(RemediationContext context);

    default int order() {
        return 100;
    }
}
