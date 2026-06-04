## 🚀 SETUP NHANH: PostgreSQL cho ImportOrderSystem

### 📋 Các bước (5 phút):

#### **1️⃣ Download PostgreSQL Driver**
- Link: https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.1/postgresql-42.7.1.jar
- Hoặc tìm `postgresql-42.7.1.jar` trên Google
- Lưu vào: `D:\Code\ImportOrderSystem_ITSS V2\lib\postgresql-42.7.1.jar`
- *(Tạo thư mục `lib` nếu chưa có)*

#### **2️⃣ Khởi tạo Database (chỉ 1 lần)**

**Cách 1: Dùng pgAdmin (nếu đã cài)**
- Mở pgAdmin → Databases → Tạo database mới tên: `importorder_db`
- Mở Query Tool → Chạy script `database\init.sql`

**Cách 2: Dùng Command Prompt**
```bash
cd D:\Code\ImportOrderSystem_ITSS V2
psql -U postgres -d importorder_db -f database\init.sql
```

#### **3️⃣ Cập nhật run.bat**

Thay thế nội dung `run.bat` bằng nội dung trong `run.bat.new`

```bash
copy run.bat.new run.bat
```

Hoặc copy-paste thủ công:
- Xóa nội dung `run.bat` cũ
- Copy nội dung `run.bat.new` vào

#### **4️⃣ Cập nhật test.bat (tùy chọn)**

```bash
copy test.bat.new test.bat
```

#### **5️⃣ Chạy ứng dụng**

```bash
run.bat
```

✅ Done! Dữ liệu giờ sẽ lưu vào PostgreSQL

---

### ✨ Kiểm tra kết nối:

Sau khi chạy lần đầu, bạn sẽ thấy:
```
✓ Kết nối PostgreSQL thành công!
✓ Dữ liệu mẫu đã được lưu vào database!
```

### 🔐 Thông tin kết nối:
- **Host:** localhost
- **Port:** 5432
- **Database:** importorder_db
- **User:** postgres
- **Password:** admin

### 🐛 Lỗi thường gặp:

| Lỗi | Nguyên nhân | Cách sửa |
|-----|-----------|---------|
| `org.postgresql.Driver not found` | JAR file chưa thêm | Kiểm tra `lib` folder có JAR không |
| `Connection refused` | PostgreSQL chưa chạy | Khởi động PostgreSQL service |
| `Database does not exist` | Chưa chạy init.sql | Chạy script `database\init.sql` |
| Mật khẩu sai | Cấu hình PostgreSQL khác | Sửa password trong `DatabaseConnection.java` |

---

### 📚 Tài liệu chi tiết:

Xem `HUONG_DAN_POSTGRESQL.md` để hiểu chi tiết hơn

### ❓ Cần giúp?

Nếu có lỗi:
1. Kiểm tra lại các bước trên
2. Kiểm tra PostgreSQL service đang chạy (`services.msc`)
3. Kiểm tra database `importorder_db` đã tạo
4. Kiểm tra file JAR trong `lib` folder
