# ImportOrderSystem_ITSS

Project JavaFX mô phỏng hệ thống đặt hàng nhập khẩu theo tài liệu ITSS.

## Chức năng chính

- Đăng nhập theo vai trò: Bộ phận bán hàng, Bộ phận đặt hàng quốc tế, Site nhập khẩu, Bộ phận quản lý kho.
- Bán hàng: tạo yêu cầu nhập hàng, xem danh sách, tìm kiếm, xem chi tiết, chỉnh sửa, xóa.
- Đặt hàng quốc tế: quản lý Site, thêm Site, kích hoạt/vô hiệu hóa, xem chi tiết.
- Lập phương án nhập hàng theo ưu tiên: tàu, tồn kho lớn, số Site ít nhất.
- Kho: xem đơn đặt hàng, kiểm tra hàng đến, tạo báo cáo kiểm hàng.
- Kiểm thử service bằng `TestRunner` không cần JUnit ngoài.

## Chạy app

```bat
cd /d D:\Code\ImportOrderSystem_ITSS
run.bat
```

## Chạy kiểm thử service

```bat
cd /d D:\Code\ImportOrderSystem_ITSS
test.bat
```

## Tài khoản mẫu

| Tài khoản | Mật khẩu | Vai trò |
| --- | --- | --- |
| sales | 123456 | Bộ phận bán hàng |
| overseas | 123456 | Bộ phận đặt hàng quốc tế |
| site | 123456 | Site nhập khẩu |
| warehouse | 123456 | Bộ phận quản lý kho |

