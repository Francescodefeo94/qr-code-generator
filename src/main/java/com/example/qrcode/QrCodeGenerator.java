package com.example.qrcode;

import com.example.qrcode.qr.QrConfiguration;
import com.example.qrcode.qr.QrEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/qrcode")
@Slf4j
public class QrCodeGenerator {

    @Value("classpath:images/logo.png")
    private Resource logoPngResource;

    @PostMapping(value = "/generate", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<BufferedImage> barbecueEAN13Barcode(@RequestBody String barcode)
            throws Exception {
        return ResponseEntity.ok(QrEngine.buildQrCodeWithLogo(barcode,
                new File(logoPngResource.getURI()),
                QrConfiguration.builder()
                        .withSize(1000)
                        .withRelativeBorderSize(.05)
                        .withRelativeBorderRound(.2)
                        .withDarkColor(new Color(44, 219, 236))
                        .withLightColor(Color.white)
                        .withPositionalsColor(new Color(0, 0, 0))
                        .withCircularPositionals(true)
                        .withRelativeLogoSize(.249)
                        .build()));
    }

    @Bean
    public HttpMessageConverter<BufferedImage> createImageHttpMessageConverter() {
        return new BufferedImageHttpMessageConverter();
    }

}
