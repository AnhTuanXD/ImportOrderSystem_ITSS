## 🗂️ HƯỚNG DẪN: Kết nối PostgreSQL cho ImportOrderSystem

### ✅ Các bước thực hiện

#### **BƯỚC 1: Chuẩn bị PostgreSQL Driver (JAR)**

Dự án này dùng `javac` trực tiếp (không Maven), bạn cần download PostgreSQL driver JAR:

1. **Download PostgreSQL JDBC Driver:**
   - Truy cập: https://jdbc.postgresql.org/download/
   - Download phiên bản mới nhất (postgresql-42.x.x.jar)
   - Hoặc download từ: https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.1/postgresql-42.7.1.jar

2. **Đặt file JAR vào dự án:**
   - Tạo folder: `d:\Code\ImportOrderSystem_ITSS V2\lib`
   - Copy file `postgresql-42.7.1.jar` vào folder `lib`

#### **BƯỚC 2: Chạy Script SQL Khởi tạo Database**

1. **Mở psql (PostgreSQL Command Line):**
   ```bash
   psql -U postgres
   ```
   
2. **Tạo database:**
   ```sql
   CREATE DATABASE importorder_db;
   ```
   
3. **Kết nối tới database:**
   ```sql
   \c importorder_db
   ```

4. **Chạy script khởi tạo bảng:**
   ```sql
   \i 'd:/Code/ImportOrderSystem_ITSS V2/database/init.sql'
   ```
   
   Hoặc từ Command Prompt:
   ```bash
   psql -U postgres -d importorder_db -f "d:\Code\ImportOrderSystem_ITSS V2\database\init.sql"
   ```

✅ Kiểm tra các bảng đã tạo:
```sql
\dt
```

#### **BƯỚC 3: Cập nhật run.bat để Compile với PostgreSQL Driver**

Sửa file `run.bat`:

```bat
@echo off
setlocal
set "JAVA_FX=D:\Code\javafx-sdk-21.0.7\lib"
set "LIB=D:\Code\ImportOrderSystem_ITSS V2\lib\postgresql-42.7.1.jar"

if not exist out mkdir out
dir /s /b src\main\java\*.java > sources.txt

REM Compile với PostgreSQL driver
javac --module-path "%JAVA_FX%" --add-modules javafx.controls -cp "%LIB%" -encoding UTF-8 -d out @sources.txt
if errorlevel 1 exit /b 1

REM Copy resources
xcopy /E /I /Y src\main\resources out > nul

REM Run với PostgreSQL driver
java --module-path "%JAVA_FX%" --add-modules javafx.controls -cp "out;%LIB%" com.itss.importorder.MainApp
```

#### **BƯỚC 4: Cập nhật test.bat tương tự**

Sửa file `test.bat`:

```bat
@echo off
setlocal
set "JAVA_FX=D:\Code\javafx-sdk-21.0.7\lib"
set "LIB=D:\Code\ImportOrderSystem_ITSS V2\lib\postgresql-42.7.1.jar"

if not exist out mkdir out
dir /s /b src\main\java\*.java > sources.txt

javac --module-path "%JAVA_FX%" --add-modules javafx.controls -cp "%LIB%" -encoding UTF-8 -d out @sources.txt
if errorlevel 1 exit /b 1

xcopy /E /I /Y src\main\resources out > nul

java --module-path "%JAVA_FX%" --add-modules javafx.controls -cp "out;%LIB%" com.itss.importorder.TestRunner
```

---

### 🔧 Các file đã tạo/cập nhật:

✅ **Các file mới:**
- `src/main/java/com/itss/importorder/database/DatabaseConnection.java` - Kết nối PostgreSQL
- `src/main/java/com/itss/importorder/database/UserRepository.java` - Quản lý User
- `src/main/java/com/itss/importorder/database/ImportRequestRepository.java` - Quản lý ImportRequest
- `src/main/java/com/itss/importorder/database/ImportSiteRepository.java` - Quản lý ImportSite
- `src/main/java/com/itss/importorder/database/StockRecordRepository.java` - Quản lý StockRecord
- `src/main/java/com/itss/importorder/database/ImportPlanRepository.java` - Quản lý ImportPlan
- `src/main/java/com/itss/importorder/database/WarehouseReportRepository.java` - Quản lý WarehouseReport
- `database/init.sql` - Script khởi tạo database

✅ **Các file cập nhật:**
- `src/main/java/com/itss/importorder/repository/DataStore.java` - Thêm database integration
- `src/main/java/com/itss/importorder/repository/SampleDataFactory.java` - Lưu dữ liệu vào DB

---

### 🚀 Chạy ứng dụng:

**Lần đầu:**
1. Cập nhật `run.bat`
2. Chạy: `run.bat`
3. Ứng dụng sẽ tự động tạo dữ liệu mẫu vào database lần đầu

**Lần tiếp theo:**
1. Dữ liệu đã lưu trong database, không mất khi tắt ứng dụng
2. Chạy: `run.bat` lại là thấy dữ liệu cũ

---

### ✨ Tính năng:

- ✅ Dữ liệu **không mất** khi tắt ứng dụng
- ✅ Hỗ trợ CRUD đầy đủ
- ✅ Dữ liệu lưu trữ **bền vững** trong PostgreSQL
- ✅ Có thể backup/restore database dễ dàng

---

### 🐛 Xử lý sự cố:

**Lỗi: "org.postgresql.Driver not found"**
- Kiểm tra file JAR có trong thư mục `lib` không
- Kiểm tra path trong `run.bat` có đúng không

**Lỗi: "Connection refused"**
- Kiểm tra PostgreSQL service đang chạy: `services.msc`
- Kiểm tra mật khẩu postgres (mặc định: `admin`)
- Kiểm tra cổng 5432 không bị firewall chặn

**Lỗi: "Database importorder_db does not exist"**
- Chạy lại bước 2: Chạy script SQL

---

### 📝 Ghi chú:

- Database name: `importorder_db`
- Username: `postgres`
- Password: `admin`
- Host: `localhost`
- Port: `5432`

Nếu có lỗi gì, hãy copy lỗi và gửi cho tôi! 🙂
