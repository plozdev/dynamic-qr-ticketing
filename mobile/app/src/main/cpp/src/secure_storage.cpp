#include "secure_storage.h"

namespace ticketing::crypto {

// Khóa salt tĩnh được lưu phân mảnh để chống trích xuất chuỗi thô bằng lệnh `strings`
static const uint8_t OBFUSCATED_SALT[] = {
    0x14, 0x39, 0x2E, 0x1F, 0x31, 0x32, 0x1F, 0x33, 0x21, 0x2C, 0x14, 0x72, 0x70, 0x72, 0x76
};
static const uint8_t SALT_MASK = 0x40;

std::string SecureStorage::deriveTicketSecret(const std::string& ticketId) {
    // 1. Giải mặt nạ salt trong RAM
    std::vector<uint8_t> unmaskedSalt = deobfuscateKey(OBFUSCATED_SALT, sizeof(OBFUSCATED_SALT), SALT_MASK);
    std::string saltStr(unmaskedSalt.begin(), unmaskedSalt.end());

    // 2. Diễn suy ra secret seed kết hợp ticketId và salt
    return "sec_" + ticketId + "_" + saltStr;
}

std::vector<uint8_t> SecureStorage::deobfuscateKey(const uint8_t* maskedData, size_t length, uint8_t maskKey) {
    std::vector<uint8_t> result(length, 0);
    for (size_t i = 0; i < length; ++i) {
        result[i] = maskedData[i] ^ maskKey;
    }
    return result;
}

} // namespace ticketing::crypto
