# ============================================
# STAGE 1: BUILD - Compile code thành JAR file
# ============================================
FROM maven:3.9-eclipse-temurin-21 AS builder
# Giải thích: Dùng image có sẵn Maven + JDK 21
# AS builder: Đặt tên stage này là "builder"

WORKDIR /app
# Tạo thư mục /app trong container

COPY pom.xml .
# Copy file pom.xml vào container
# Tại sao copy pom.xml trước?
# → Docker cache layer này, nếu pom.xml không đổi thì không cần download dependencies lại

RUN mvn dependency:go-offline -B
# Download tất cả dependencies (libraries) vào local Maven repository
# -B: Batch mode (không cần input)

COPY src ./src
# Copy toàn bộ source code vào container

RUN mvn clean package -DskipTests
# Build JAR file
# mvn clean: Xóa build cũ
# mvn package: Compile và đóng gói thành JAR
# -DskipTests: Bỏ qua tests (để build nhanh)

# ============================================
# STAGE 2: RUNTIME - Chỉ chứa JAR và JRE
# ============================================
FROM eclipse-temurin:21-jre-alpine
# Giải thích: Chỉ cần JRE (Java Runtime Environment) để chạy JAR
# Không cần JDK (không compile code nữa)
# alpine: Linux distro rất nhẹ

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
# Tạo user "spring" (không chạy với root - bảo mật tốt hơn)

COPY --from=builder /app/target/*.jar app.jar
# Copy JAR file từ stage "builder" (stage 1)
# Đổi tên thành app.jar (dễ nhớ)

RUN chown spring:spring app.jar
# Đổi owner file cho user spring

# Cài wget cho healthcheck (Alpine không có sẵn)
RUN apk add --no-cache wget

USER spring
# Chuyển sang user spring (không chạy với root)

EXPOSE 8081
# Khai báo port app sẽ chạy (chỉ là documentation)

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/api/actuator/health || exit 1
# Kiểm tra sức khỏe của ứng dụng
# --interval: Kiểm tra mỗi 30 giây
# --timeout: Timeout sau 3 giây
# --start-period: Cho phép 40 giây để app khởi động
# --retries: Thử lại 3 lần trước khi đánh dấu unhealthy

ENTRYPOINT ["java", "-jar", "app.jar"]
# Lệnh chạy khi container start
# java -jar app.jar: Chạy JAR file bằng Java