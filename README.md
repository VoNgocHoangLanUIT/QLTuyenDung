# 💼 TalentHub - Hệ thống quản lý tuyển dụng

TalentHub là hệ thống quản lý tuyển dụng toàn diện giúp doanh nghiệp tổ chức và giám sát quy trình tuyển dụng một cách hiệu quả. Ứng viên có thể tìm kiếm cơ hội việc làm, ứng tuyển, nộp hồ sơ và theo dõi tiến trình của mình dễ dàng.

---

## 🎯 Mục tiêu dự án

Hệ thống giúp doanh nghiệp quản lý quy trình tuyển dụng một cách hiệu quả từ:

- Đăng tin tuyển dụng
- Theo dõi ứng viên
- Quản lý bài test và kết quả test
- Tổ chức phỏng vấn
- Đánh giá ứng viên
- Gửi offer cho ứng viên

Ứng viên có thể:

- Tìm kiếm việc làm phù hợp
- Ứng tuyển trực tuyến
- Nộp hồ sơ
- Theo dõi tiến trình tuyển dụng dễ dàng

---

## 🔧 Tính năng chính

- ✅ Quản lý tin tuyển dụng
- ✅ Quản lý hồ sơ ứng viên
- ✅ Quản lý bài test và kết quả test
- ✅ Quản lý lịch và kết quả phỏng vấn
- ✅ Theo dõi tiến trình tuyển dụng theo pipeline
- 🔄 Tích hợp LinkedIn để thu thập hồ sơ (*chưa hoàn thành*)
- 🤖 AI gợi ý ứng viên phù hợp (*chưa hoàn thành*)
- 🔐 Phân quyền người dùng:
  - `ADMIN`
  - `RECUITER`
  - `HR_STAFF`
  - `CV_STAFF`
  - `CANDIDATE`

---

## 🧰 Công nghệ sử dụng

| Thành phần     | Công nghệ                         |
|----------------|----------------------------------|
| Backend        | Java Spring Boot                 |
| Frontend       | HTML + CSS + JavaScript          |
| Cơ sở dữ liệu  | Oracle                           |
| Xác thực       | Spring Security                  |
| Gửi email      | Mailgun API                      |
| Tích hợp API   | LinkedIn API (*chưa hoàn thành*) |

---

## 🚀 Cài đặt và chạy hệ thống

### 1. Clone repository
```bash
git clone https://github.com/katoloc-st/QLTuyenDung.git
cd QLTuyenDung
```

### 2. Cấu hình database Oracle
- Tạo schema trong Oracle: `QLTuyenDung`
- Cập nhật thông tin kết nối trong file `application.properties` (Spring Boot):

```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:orcl
spring.datasource.username=QLTuyenDung
spring.datasource.password=your_password
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
```

- Import database bằng file `talenthub_schema.sql` hoặc file dump `.dmp` đã được cung cấp.

### 3. Cài đặt Maven, JDK và Extension
- Cài đặt:
  - [JDK 17+](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
  - [Apache Maven](https://maven.apache.org/download.cgi)
- Cấu hình biến môi trường:
  - Thêm `JAVA_HOME` và `MAVEN_HOME` vào biến môi trường hệ thống
- Nếu dùng VS Code:
  - Cài extension **Java Extension Pack** để hỗ trợ phát triển và chạy Spring Boot

### 4. Chạy backend (Spring Boot)
- Mở file `QlTuyenDungApplication.java`
- Nhấn nút **Run** trong IDE hoặc chạy bằng terminal:

```bash
./mvnw spring-boot:run
```

### 5. Chạy frontend
- Mở trình duyệt
- Truy cập đường dẫn: [http://localhost:8888](http://localhost:8888)

---

## 🗃️ Cấu trúc thư mục

```
QLTuyendung/
├── .mvn/
├── .vscode/
├── src/
│   └── main/
│       ├── java/com/example/QLTuyendung/
│       │   ├── config/
│       │   ├── controller/
│       │   ├── dto/
│       │   ├── model/
│       │   ├── repository/
│       │   ├── service/
│       │   └── QlTuyenDungApplication.java
│       └── resources/
│           ├── static/
│           ├── templates/
│           │   ├── admin/
│           │   ├── nhatuyendung/
│           │   ├── nvhs/
│           │   ├── nvtd/
│           │   ├── ungvien/
│           │   └── user/
│           ├── logon.html
│           └── application.properties
├── test/
├── target/
├── uploads/
├── .gitignore
├── .gitattributes
└── README.md

```

---

## 🧪 Tài khoản mặc định (demo)

| Vai trò     | Email                     | Mật khẩu     |
|-------------|---------------------------|--------------|
| Admin       | admin@talenthub.com       | 123456789    |
| Recruiter   | recruiter@talenthub.com   | 123456789    |
| HR Staff    | hr@talenthub.com          | 123456789    |
| CV Staff    | cv@talenthub.com          | 123456789    |
| Candidate   | candidate@talenthub.com   | 123456789    |

---

## ✍️ Đóng góp

Chúng tôi luôn hoan nghênh mọi đóng góp để cải thiện hệ thống. Bạn có thể:

- Tạo **Issue** nếu phát hiện lỗi hoặc cần đề xuất tính năng
- Tạo **Pull Request** để gửi đóng góp mã nguồn

---

## 📄 Giấy phép

Dự án này sử dụng [MIT License](LICENSE).