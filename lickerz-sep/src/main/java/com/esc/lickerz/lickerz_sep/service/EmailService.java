package com.esc.lickerz.lickerz_sep.service;

// import org.joda.time.LocalDateTime;
// import org.joda.time.format.DateTimeFormat;
// import org.joda.time.format.DateTimeFormatter;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
// import org.springframework.mail.javamail.MimeMessageHelper;
// import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// import org.thymeleaf.context.Context;
// import org.thymeleaf.spring6.SpringTemplateEngine;

// import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendVerificationCode(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Lickerz 비밀번호 재설정 인증 코드");
        message.setText("귀하의 인증 코드는 " + code + " 입니다. 이 코드는 5분간 유효합니다.");
        javaMailSender.send(message);
        log.info("인증 코드 이메일 전송 완료: {}", email);
    }
}