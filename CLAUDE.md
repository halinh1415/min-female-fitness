# Quy trình khi nhận yêu cầu qua Telegram 
1. nhận được lệnh trả lời xác nhận qua Telegram
2. Chạy `git pull` trước khi sửa. Nếu xung đột: dừng lại, báo qua Telegram.
3. Sửa code theo yêu cầu.
4. Build: `./gradlew assembleDebug`
5. Nếu build lỗi: đọc log, sửa và build lại, tối đa 3 lần.
6. Trả lời qua Telegram: file đã sửa, kết quả build.
   Nếu thất bại, gửi dòng lỗi chính.