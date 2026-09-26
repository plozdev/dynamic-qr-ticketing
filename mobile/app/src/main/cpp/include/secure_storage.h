#ifndef DYNAMIC_QR_SECURE_STORAGE_H
#define DYNAMIC_QR_SECURE_STORAGE_H

#include <string>
#include <vector>

namespace ticketing::crypto {

/**
 * Native secure storage & secret derivation.
 * Uses compile-time XOR obfuscation / salt embedding to prevent plain-text secret extraction.
 */
class SecureStorage {
public:
    /**
     * Retrieve derived HMAC secret for a specific ticket or master seed.
     */
    static std::string deriveTicketSecret(const std::string& ticketId);

    /**
     * Obfuscation helper to de-mask embedded native keys in RAM during execution.
     */
    static std::vector<uint8_t> deobfuscateKey(const uint8_t* maskedData, size_t length, uint8_t maskKey);
};

} // namespace ticketing::crypto

#endif // DYNAMIC_QR_SECURE_STORAGE_H
