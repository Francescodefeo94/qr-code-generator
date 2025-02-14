package com.example.qrcode;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Builder
public class QrRequestDTO {

    private String resourceUrl;
    private QrConfigurationDTO config;
}
