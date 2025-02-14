package com.example.qrcode;

import com.example.qrcode.qr.QrConfiguration;
import com.example.qrcode.qr.QrEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

import static com.example.qrcode.qr.QrEngine.validateConfiguration;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/qrcode")
@Slf4j
public class QrCodeGenerator {

    @Value("${app.cypher.png}")
    private String encodedPngResource;

    @Value("${app.cypher.key}")
    private String key;

    @Value("${app.cypher.algorithm}")
    private String algorithm;

    @Value("${app.cypher.transformation}")
    private String transformation;

    @PostMapping(value = "/generate", produces = {MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<?> barbecueEAN13Barcode(@RequestBody QrRequestDTO requestDTO)
            throws Exception {
        try {
            QrConfiguration qrConfiguration = getQrConfiguration(requestDTO);
            BufferedImage logoImage = reconstructPngFromString(decrypt(encodedPngResource, key, transformation, algorithm));
            return ResponseEntity.ok(QrEngine.buildQrCodeWithLogo(requestDTO.getResourceUrl(),
                    logoImage,
                    qrConfiguration));
        } catch (QrConfiguration.InvalidConfigurationException e) {
            log.error("Invalid configuration", e);
            return ResponseEntity.badRequest().build();
        }
    }

    private static QrConfiguration getQrConfiguration(QrRequestDTO requestDTO) throws QrConfiguration.InvalidConfigurationException {
        QrConfiguration qrConfiguration = QrConfiguration.builder()
                .withSize(requestDTO.getConfig().getSize())
                .withRelativeBorderSize(requestDTO.getConfig().getRelativeBorderSize())
                .withRelativeBorderRound(requestDTO.getConfig().getRelativeBorderRound())
                .withDarkColor(Color.decode(requestDTO.getConfig().getDarkColor()))
                .withLightColor(Color.decode(requestDTO.getConfig().getLightColor()))
                .withPositionalsColor(Color.decode(requestDTO.getConfig().getPositionalsColor()))
                .withCircularPositionals(requestDTO.getConfig().isCircularPositionals())
                .withRelativeLogoSize(requestDTO.getConfig().getRelativeLogoSize())
                .build();
        validateConfiguration(qrConfiguration);
        return qrConfiguration;
    }

    @Bean
    public HttpMessageConverter<BufferedImage> createImageHttpMessageConverter() {
        return new BufferedImageHttpMessageConverter();
    }

    public static String decrypt(String encryptedText, String key, String transformation, String algorithm) throws Exception {
        Cipher cipher = Cipher.getInstance(transformation);
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), algorithm);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decryptedBytes);
    }

    public static BufferedImage reconstructPngFromString(String base64String) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64String)));
    }

}
