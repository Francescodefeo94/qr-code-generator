package com.example.qrcode;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
@Data
@Builder
public class QrConfigurationDTO {

	// Final image size, multiple of 4 required
	private int size;
	private String darkColor;
	private String lightColor;
	private String positionalsColor;
	private double relativeLogoSize;
	private double relativeBorderSize;
	private double relativeBorderRound;
	private boolean circularPositionals;
	private double relativePositionalsRound;

}
