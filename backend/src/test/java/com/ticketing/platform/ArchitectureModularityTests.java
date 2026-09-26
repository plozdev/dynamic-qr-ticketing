package com.ticketing.platform;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Architectural verification test powered by Spring Modulith and ArchUnit.
 *
 * Verifies that:
 * 1. Internal module boundaries (eventcatalog, ticketissuance, gatevalidator, auditlog) are strictly respected.
 * 2. No cyclical dependencies exist between bounded contexts.
 * 3. Subpackages (domain, application, infrastructure, api) remain private to their respective module,
 *    preventing architectural decay and tight coupling.
 */
class ArchitectureModularityTests {

    private final ApplicationModules modules = ApplicationModules.of(DynamicQrTicketingApplication.class);

    @Test
    @DisplayName("Verify Spring Modulith boundaries, encapsulation, and acyclic graph")
    void verifyModularity() {
        System.out.println(modules);
        modules.verify();
    }

    @Test
    @DisplayName("Generate C4 architecture and PlantUML documentation")
    void generateArchitectureDocumentation() {
        new Documenter(modules)
                .writeDocumentation();
    }
}
