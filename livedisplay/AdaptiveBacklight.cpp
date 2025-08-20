/*
 * SPDX-FileCopyrightText: 2019-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

#define LOG_TAG "AdaptiveBacklightService"

#include <android-base/file.h>
#include <android-base/logging.h>
#include <android-base/strings.h>

#include "AdaptiveBacklight.h"

namespace aidl {
namespace vendor {
namespace lineage {
namespace livedisplay {

static constexpr const char* kCabcStatusPath = "/sys/devices/platform/soc/soc:qcom,dsi-display/cabc";

ndk::ScopedAStatus AdaptiveBacklight::getEnabled(bool* _aidl_return) {
    std::string buf;
    if (!android::base::ReadFileToString(kCabcStatusPath, &buf)) {
        LOG(ERROR) << "Failed to read " << kCabcStatusPath;
        return ndk::ScopedAStatus::fromExceptionCode(EX_UNSUPPORTED_OPERATION);
    }
    *_aidl_return = std::stoi(android::base::Trim(buf)) == 1;
    return ndk::ScopedAStatus::ok();
}

ndk::ScopedAStatus AdaptiveBacklight::setEnabled(bool enabled) {
    if (!android::base::WriteStringToFile((enabled ? "1" : "0"), kCabcStatusPath)) {
        LOG(ERROR) << "Failed to write " << kCabcStatusPath;
        return ndk::ScopedAStatus::fromExceptionCode(EX_UNSUPPORTED_OPERATION);
    }
    return ndk::ScopedAStatus::ok();
}

}  // namespace livedisplay
}  // namespace lineage
}  // namespace vendor
}  // namespace aidl
