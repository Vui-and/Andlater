# Custom OS Emulator — kiến trúc khung (skeleton)

Đây là **khung kiến trúc thật** (Gradle + NDK/CMake + JNI + CI) cho một app kiểu
Winlator chạy custom OS, theo đúng 3 lớp bạn yêu cầu:

- **UI Layer** — `ui/MainActivity.kt` (chọn file .iso/.img, cấu hình RAM/vCPU/độ phân giải),
  `ui/EmulatorActivity.kt` (SurfaceView hiển thị màn hình VM, dịch chạm → chuột, bàn phím ảo).
- **OS Compatibility / Hardware Emulation Layer** — `core/QemuLauncher.kt` +
  `cpp/hal/*` (C++ supervisor fork/exec QEMU thật, input injector, surface bridge).
- **Packaging** — `.github/workflows/build.yml` tự clone & build **QEMU thật**
  (`qemu/qemu`) và **Box86/Box64 thật** (`ptitSeb/box86`, `ptitSeb/box64`) từ
  source, đóng gói vào `jniLibs/arm64-v8a/`, rồi build APK.

## Việc CHƯA làm được / còn là stub — nói thẳng để bạn không bị bất ngờ

1. **`input_injector.cpp` và `surface_bridge.cpp` là stub.** Chuyển input/khung
   hình thật giữa Kotlin ⇄ QEMU cần chọn 1 transport cụ thể (QMP socket, hoặc
   virtio-input/virtio-gpu trực tiếp) — đây là quyết định kỹ thuật cần làm khi
   bạn đã có binary QEMU thật chạy được, không thể "đoán trước" trong khung sườn.
2. **File `gradle/wrapper/gradle-wrapper.jar` chưa có** (đây là file nhị phân).
   Chạy 1 trong 2 cách sau trên máy có mạng/Android Studio để tạo nó:
   ```
   gradle wrapper --gradle-version 8.7
   ```
   hoặc mở project bằng Android Studio, nó tự tạo.
3. **Lệnh `./configure` của QEMU trong workflow là minh hoạ**, gần như chắc
   chắn cần chỉnh thêm cross-file/flags khi build thật lần đầu (QEMU cross-
   compile cho Android không có toolchain chính thức, cộng đồng Winlator/Limbo
   có patch riêng đáng tham khảo).
4. **`res/drawable/ic_launcher.xml` chỉ là icon placeholder** đơn giản, không
   phải adaptive icon chuẩn — thay bằng icon thật khi cần.

## ⚠️ Lưu ý giấy phép (quan trọng, đọc trước khi phát hành)

- **QEMU là GPLv2.** Nhúng binary QEMU (dù build sẵn) vào APK closed-source
  buộc bạn phải tuân thủ GPLv2 — cung cấp mã nguồn tương ứng (bao gồm app của
  bạn nếu liên kết tĩnh, hoặc ít nhất là source QEMU + patch nếu chạy như
  subprocess riêng biệt, tuỳ cách bạn tích hợp). Nên tự đọc kỹ điều khoản GPLv2
  hoặc hỏi ý kiến pháp lý trước khi phát hành app thương mại.
- **Box86/Box64 là MIT** — dễ nhúng hơn, nhưng vẫn cần giữ thông báo bản quyền.
- **Wine là LGPL** — nếu sau này bạn thêm Wine (không có trong khung này vì
  bạn nói mục tiêu là custom OS, không phải Windows), cùng logic áp dụng.

## Build thật

```bash
# 1. Trên máy có mạng, tạo gradle wrapper jar (một lần):
gradle wrapper --gradle-version 8.7

# 2. Push lên GitHub, chạy workflow (Actions tab → "Build Custom OS
#    Emulator APK" → Run workflow), hoặc để nó tự chạy khi push vào main.

# 3. Tải APK từ artifact "custom-os-emulator-apk" sau khi workflow xong.
```

## Repo mã nguồn mở nên tham khảo thêm

- https://github.com/brunodev85/winlator — kiến trúc gần nhất với app bạn muốn.
- https://github.com/qemu/qemu — lõi máy ảo.
- https://github.com/ptitSeb/box86 và https://github.com/ptitSeb/box64 — DBT x86→ARM.
- https://github.com/utmapp/UTM — QEMU trên di động, cách họ làm surface/input bridge rất đáng học.
- https://github.com/termux/termux-app — cách đóng gói toolchain Linux trong APK qua proot.
