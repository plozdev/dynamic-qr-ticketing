package com.ticketing.platform.ticketissuance.application.port.in;

import com.ticketing.platform.ticketissuance.application.dto.IssueTicketCommand;

import java.util.UUID;

public interface IssueTicketUseCase {

    UUID issueTicket(IssueTicketCommand command);
}
