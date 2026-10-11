# Quy trình khi nhận yêu cầu qua Telegram 
1. nhận được lệnh trả lời xác nhận qua Telegram
2. đọc nhật ký thay đổi. Sửa code theo yêu cầu. 
3. Build: `./gradlew assembleDebug`
4. Nếu build lỗi: đọc log, sửa và build lại, tối đa 3 lần.
5. viết nhật ký thay đổi
6. Trả lời qua Telegram: file đã sửa, kết quả build.
7. Ưu tiên test trên máy thật, test xong tắt màn hình
8. Nếu máy thật không cắm thì bật máy ảo, test xong tắt máy ảo
   Nếu thất bại, gửi dòng lỗi chính.