#include "crypto_engine.h"
#include <sstream>
#include <vector>
#include <cstring>
#include <iomanip>

namespace ticketing::crypto {

namespace {

// Bảng ký tự Base64Url theo RFC 4648 §5
static const char BASE64URL_CHARS[] =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    "abcdefghijklmnopqrstuvwxyz"
    "0123456789-_";

// =========================================================================
// THUẬT TOÁN BĂM SHA-256 THUẦN C++ (FIPS 180-4 / RFC 6234)
// Không phụ thuộc thư viện ngoài, chạy cực nhanh và an toàn trên C++ NDK
// =========================================================================

inline uint32_t rotr(uint32_t n, unsigned int c) {
    return (n >> c) | (n << (32 - c));
}

inline uint32_t ch(uint32_t x, uint32_t y, uint32_t z) { return (x & y) ^ (~x & z); }
inline uint32_t maj(uint32_t x, uint32_t y, uint32_t z) { return (x & y) ^ (x & z) ^ (y & z); }
inline uint32_t sig0(uint32_t x) { return rotr(x, 2) ^ rotr(x, 13) ^ rotr(x, 22); }
inline uint32_t sig1(uint32_t x) { return rotr(x, 6) ^ rotr(x, 11) ^ rotr(x, 25); }
inline uint32_t theta0(uint32_t x) { return rotr(x, 7) ^ rotr(x, 18) ^ (x >> 3); }
inline uint32_t theta1(uint32_t x) { return rotr(x, 17) ^ rotr(x, 19) ^ (x >> 10); }

static const uint32_t SHA256_K[64] = {
    0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
    0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
    0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
    0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
    0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
    0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
    0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
    0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
};

std::vector<uint8_t> sha256(const uint8_t* data, size_t length) {
    uint32_t h0 = 0x6a09e667;
    uint32_t h1 = 0xbb67ae85;
    uint32_t h2 = 0x3c6ef372;
    uint32_t h3 = 0xa54ff53a;
    uint32_t h4 = 0x510e527f;
    uint32_t h5 = 0x9b05688c;
    uint32_t h6 = 0x1f83d9ab;
    uint32_t h7 = 0x5be0cd19;

    // Chuẩn bị đệm Padding: append 0x80, thêm zeroes, rồi append 64-bit length
    std::vector<uint8_t> padded(data, data + length);
    padded.push_back(0x80);
    while ((padded.size() % 64) != 56) {
        padded.push_back(0x00);
    }
    uint64_t totalBits = static_cast<uint64_t>(length) * 8;
    for (int i = 7; i >= 0; --i) {
        padded.push_back(static_cast<uint8_t>((totalBits >> (i * 8)) & 0xFF));
    }

    // Xử lý từng khối 64 bytes (512 bits)
    for (size_t chunk = 0; chunk < padded.size(); chunk += 64) {
        uint32_t w[64];
        for (int t = 0; t < 16; ++t) {
            w[t] = (static_cast<uint32_t>(padded[chunk + t * 4]) << 24) |
                   (static_cast<uint32_t>(padded[chunk + t * 4 + 1]) << 16) |
                   (static_cast<uint32_t>(padded[chunk + t * 4 + 2]) << 8) |
                   (static_cast<uint32_t>(padded[chunk + t * 4 + 3]));
        }
        for (int t = 16; t < 64; ++t) {
            w[t] = theta1(w[t - 2]) + w[t - 7] + theta0(w[t - 15]) + w[t - 16];
        }

        uint32_t a = h0, b = h1, c = h2, d = h3, e = h4, f = h5, g = h6, h = h7;
        for (int t = 0; t < 64; ++t) {
            uint32_t t1 = h + sig1(e) + ch(e, f, g) + SHA256_K[t] + w[t];
            uint32_t t2 = sig0(a) + maj(a, b, c);
            h = g;
            g = f;
            f = e;
            e = d + t1;
            d = c;
            c = b;
            b = a;
            a = t1 + t2;
        }

        h0 += a; h1 += b; h2 += c; h3 += d;
        h4 += e; h5 += f; h6 += g; h7 += h;
    }

    std::vector<uint8_t> digest(32);
    uint32_t state[8] = {h0, h1, h2, h3, h4, h5, h6, h7};
    for (int i = 0; i < 8; ++i) {
        digest[i * 4]     = static_cast<uint8_t>((state[i] >> 24) & 0xFF);
        digest[i * 4 + 1] = static_cast<uint8_t>((state[i] >> 16) & 0xFF);
        digest[i * 4 + 2] = static_cast<uint8_t>((state[i] >> 8) & 0xFF);
        digest[i * 4 + 3] = static_cast<uint8_t>(state[i] & 0xFF);
    }
    return digest;
}

// =========================================================================
// THUẬT TOÁN HMAC-SHA256 (RFC 2104)
// Tương thích 100% với javax.crypto.Mac.getInstance("HmacSHA256") của Backend
// =========================================================================
std::vector<uint8_t> computeHmacSha256(const std::string& key, const std::string& message) {
    const size_t BLOCK_SIZE = 64;
    std::vector<uint8_t> k0(BLOCK_SIZE, 0x00);

    if (key.size() > BLOCK_SIZE) {
        std::vector<uint8_t> hashedKey = sha256(
            reinterpret_cast<const uint8_t*>(key.data()), key.size()
        );
        std::memcpy(k0.data(), hashedKey.data(), hashedKey.size());
    } else {
        std::memcpy(k0.data(), key.data(), key.size());
    }

    std::vector<uint8_t> ipad(BLOCK_SIZE);
    std::vector<uint8_t> opad(BLOCK_SIZE);
    for (size_t i = 0; i < BLOCK_SIZE; ++i) {
        ipad[i] = k0[i] ^ 0x36;
        opad[i] = k0[i] ^ 0x5C;
    }

    // Vòng băm trong: H(ipad || message)
    std::vector<uint8_t> innerMsg = ipad;
    innerMsg.insert(innerMsg.end(), message.begin(), message.end());
    std::vector<uint8_t> innerHash = sha256(innerMsg.data(), innerMsg.size());

    // Vòng băm ngoài: H(opad || innerHash)
    std::vector<uint8_t> outerMsg = opad;
    outerMsg.insert(outerMsg.end(), innerHash.begin(), innerHash.end());
    std::vector<uint8_t> hmacResult = sha256(outerMsg.data(), outerMsg.size());

    return hmacResult;
}

} // anonymous namespace

/**
 * @brief Mã hóa mảng byte nhị phân sang chuỗi Base64Url (RFC 4648 Section 5).
 */
std::string toBase64Url(const std::vector<uint8_t>& data) {
    std::string result;
    size_t length = data.size();
    if (length == 0) return result;

    result.reserve(((length + 2) / 3) * 4);

    size_t i = 0;
    while (i + 2 < length) {
        uint32_t octet_a = data[i++];
        uint32_t octet_b = data[i++];
        uint32_t octet_c = data[i++];

        uint32_t triple = (octet_a << 16) | (octet_b << 8) | octet_c;

        result.push_back(BASE64URL_CHARS[(triple >> 18) & 0x3F]);
        result.push_back(BASE64URL_CHARS[(triple >> 12) & 0x3F]);
        result.push_back(BASE64URL_CHARS[(triple >> 6) & 0x3F]);
        result.push_back(BASE64URL_CHARS[triple & 0x3F]);
    }

    if (i < length) {
        uint32_t octet_a = data[i++];
        uint32_t octet_b = (i < length) ? data[i++] : 0;

        uint32_t triple = (octet_a << 16) | (octet_b << 8);

        result.push_back(BASE64URL_CHARS[(triple >> 18) & 0x3F]);
        result.push_back(BASE64URL_CHARS[(triple >> 12) & 0x3F]);

        if (i == length + 1) { // Còn dư 2 bytes
            result.push_back(BASE64URL_CHARS[(triple >> 6) & 0x3F]);
        }
    }

    return result;
}

/**
 * @brief Sinh Dynamic TOTP Token bằng HMAC-SHA256 trên thông điệp "<ticketId>:<timeWindow>".
 */
std::string CryptoEngine::generateTotpToken(
    const std::string& ticketId,
    const std::string& secretKey,
    int64_t epochSeconds,
    int32_t intervalSec
) {
    if (intervalSec <= 0) intervalSec = 30;

    int64_t timeWindow = epochSeconds / intervalSec;

    std::ostringstream msgStream;
    msgStream << ticketId << ":" << timeWindow;
    std::string message = msgStream.str();

    // 1. Tính toán HMAC-SHA256
    std::vector<uint8_t> hmacDigest = computeHmacSha256(secretKey, message);

    // 2. Chuyển đổi sang chuỗi Base64Url
    return toBase64Url(hmacDigest);
}

/**
 * @brief Xác thực Dynamic TOTP Token với cơ chế trượt thời gian (+/- allowedDriftSteps).
 */
bool CryptoEngine::verifyTotpToken(
    const std::string& ticketId,
    const std::string& secretKey,
    const std::string& token,
    int64_t epochSeconds,
    int32_t intervalSec,
    int32_t allowedDriftSteps
) {
    if (intervalSec <= 0) intervalSec = 30;
    int64_t currentWindow = epochSeconds / intervalSec;

    // Duyệt qua cửa sổ trượt: window - allowedDriftSteps ... window + allowedDriftSteps
    for (int32_t drift = -allowedDriftSteps; drift <= allowedDriftSteps; ++drift) {
        int64_t checkTimeSeconds = (currentWindow + drift) * intervalSec;
        std::string expectedToken = generateTotpToken(ticketId, secretKey, checkTimeSeconds, intervalSec);
        if (expectedToken == token) {
            return true;
        }
    }
    return false;
}

std::string CryptoEngine::getVersion() const {
    return "2.0.0-hmac-sha256-base64url";
}

} // namespace ticketing::crypto
