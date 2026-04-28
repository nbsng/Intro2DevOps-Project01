package com.yas.media;

import com.yas.commonlibrary.config.CorsConfig;
import com.yas.media.config.YasConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {"com.yas.media", "com.yas.commonlibrary"})
@EnableConfigurationProperties({YasConfig.class, CorsConfig.class})
public class MediaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediaApplication.class, args);
    }
    // Hàm cố tình viết sai để test SonarQube
    public void testSonarQubeFails() {
        // 1. Lỗi bảo mật: Hardcode mật khẩu (Security Hotspot/Vulnerability)
        String dbPassword = "my_super_secret_password_123!";

        // 2. Lỗi Bug: Null Pointer Exception xảy ra
        String text = null;
        if (text.equals("hello")) {
            System.out.println("Lỗi rồi!");
        }

        // 3. Lỗi Code Smell: Try-catch
        try {
            int a = 10 / 0;
        } catch (Exception e) {
            // Cố tình để trống, SonarQube cực ghét điều này (test)
        }
    }

}
