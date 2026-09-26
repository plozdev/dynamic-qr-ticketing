#ifndef DYNAMIC_QR_CRYPTO_ENGINE_H
#define DYNAMIC_QR_CRYPTO_ENGINE_H

#include <string>
#include <cstdint>

namespace ticketing::crypto {

/**
 * Core cryptographic engine for Dynamic QR generation & verification.
 * Implements HMAC-SHA256 and Base64Url token encoding.
 */
class CryptoEngine {
public:
    CryptoEngine() = default;
    ~CryptoEngine() = default;

    /**
     * Generate dynamic token using HMAC-SHA256 on message "<ticketId>:<timeWindow>".
     * @param ticketId Unique ticket ID.
     * @param secretKey Provisioned shared secret key.
     * @param epochSeconds Current Unix epoch in seconds.
     * @param intervalSec Rotation period (default 30 seconds).
     * @return Base64Url encoded dynamic token string.
     */
    std::string generateTotpToken(
        const std::string& ticketId,
        const std::string& secretKey,
        int64_t epochSeconds,
        int32_t intervalSec
    );

    /**
     * Verify dynamic token with allowable sliding time window drift.
     */
    bool verifyTotpToken(
        const std::string& ticketId,
        const std::string& secretKey,
        const std::string& token,
        int64_t epochSeconds,
        int32_t intervalSec,
        int32_t allowedDriftSteps
    );

    std::string getVersion() const;
};

} // namespace ticketing::crypto

#endif // DYNAMIC_QR_CRYPTO_ENGINE_H
