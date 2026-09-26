package com.ticketing.platform.gatevalidator.application.port.in;

import com.ticketing.platform.gatevalidator.application.dto.GateValidationResultDto;
import com.ticketing.platform.gatevalidator.application.dto.ValidateGateCommand;

public interface ValidateTicketAtGateUseCase {

    GateValidationResultDto validateAtGate(ValidateGateCommand command);
}
