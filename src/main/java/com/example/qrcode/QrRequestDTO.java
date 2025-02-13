package com.example.qrcode;

import com.example.qrcode.qr.QrConfiguration;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Builder
public class QrRequestDTO {

    private String barcode;
    private QrConfigurationDTO config;
}
