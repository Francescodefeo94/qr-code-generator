package com.example.qrcode.qr;

import com.example.qrcode.qr.zxing.QRCodeWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class QrEngine {

	public static void validateConfiguration(QrConfiguration conf) throws QrConfiguration.InvalidConfigurationException {
		if (conf == null) throw new QrConfiguration.InvalidConfigurationException("Configuration is null");
		if (conf.getDarkColor() == null) throw new QrConfiguration.InvalidConfigurationException("Configuration darkColor is null");
		if (conf.getLightColor() == null) throw new QrConfiguration.InvalidConfigurationException("Configuration lightColor is null");
		if (conf.getPositionalsColor() == null) throw new QrConfiguration.InvalidConfigurationException("Configuration positionalsColor is null");
		if (conf.getSize()==0 || conf.getSize()%4 != 0) throw new QrConfiguration.InvalidConfigurationException("Configuration size must be multiple of 4");
		if (conf.getRelativeBorderSize() > .1) throw new QrConfiguration.InvalidConfigurationException("Relative border too big, set it < .1");
		if (conf.getRelativeLogoSize() > .25) throw new QrConfiguration.InvalidConfigurationException("Relative logo too big set it < .25");
		if (conf.getRelativeBorderRound() > .25) throw new QrConfiguration.InvalidConfigurationException("Relative border round too big set it < .25");
	}

	public static BufferedImage buildQrCodeWithLogo(String text, BufferedImage logoImage, QrConfiguration conf) throws WriterException {
		QRCodeWriter.QRCodeWithPositionals qr = encode(text,conf);
		BufferedImage qrImage = baseImage(qr, conf);
        return layer(qrImage,logoImage,conf);
	}

	private static BufferedImage layer(BufferedImage qrImage, BufferedImage logoImage, QrConfiguration conf) {
		BufferedImage image = new BufferedImage(conf.getSize(), conf.getSize(), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		Color white = conf.getLightColor();

		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		final int border = border(conf);

		//Black border
		graphics.setColor(conf.getDarkColor());
		graphics.fillRoundRect(0, 0, conf.getSize(), conf.getSize(),
				(int)(conf.getSize()*conf.getRelativeBorderRound()),
				(int)(conf.getSize()*conf.getRelativeBorderRound()));
		graphics.setColor(white);
		graphics.fillRoundRect(border/2, border/2, conf.getSize()-(border), conf.getSize()-(border),
				(int)((conf.getSize()-border)*conf.getRelativeBorderRound()),
				(int)((conf.getSize()-border)*conf.getRelativeBorderRound()));

		//Qr Data
		graphics.drawImage(qrImage,border,border, null);

		if (logoImage != null) {
			//addLogoToQrCode(graphics, logoImage, conf, border, white);
		}
		return image;

	}

	public static int border(QrConfiguration conf) {
		return (int)Math.floor(conf.getSize()*conf.getRelativeBorderSize());
	}

	public static QRCodeWriter.QRCodeWithPositionals encode(String text, QrConfiguration conf) throws WriterException {
		final int border = border(conf);
		int computedSize = conf.getSize()-(2*border);
		Map<EncodeHintType, Object> hintMap = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
		hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		hintMap.put(EncodeHintType.MARGIN, 0);
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		return qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, computedSize,
				computedSize, hintMap);
	}

	private static BufferedImage baseImage(
			QRCodeWriter.QRCodeWithPositionals encoded,
			QrConfiguration conf) {
		final int border = border(conf);
		int computedSize = conf.getSize()-(2*border);

		BufferedImage image = new BufferedImage(computedSize, computedSize, BufferedImage.TYPE_4BYTE_ABGR_PRE);
		Graphics2D graphics = (Graphics2D) image.getGraphics();

		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);


		Color white = conf.getLightColor();

		//Data Squares
		encoded.dataSquares.forEach(s -> {
			if (s.black) {
				graphics.setColor(conf.getDarkColor());
				graphics.fillRect(s.x, s.y, s.size, s.size);
			}else {
				graphics.setColor(white);
				graphics.fillRect(s.x, s.y, s.size, s.size);
			}
		});

		//Positionals
		encoded.positionals.forEach(p -> {
			int r = p.size;
			int cx = p.left;
			int cy = p.top;
			int wr = r-2*(p.blackBorder);
			int ir = r-2*(p.blackBorder)-2*(p.whiteBorder);


			//White External Circle
			graphics.setColor(white);
			drawPositional(graphics,cx-2, cy-2, r+4, r+4, conf);

			//Black External Circle
			graphics.setColor(conf.getDarkColor());
			drawPositional(graphics,cx, cy, r, r, conf);

			cx = cx +p.blackBorder;
			cy = cy +p.blackBorder;
			//White Internal Circle
			graphics.setColor(white);
			drawPositional(graphics,cx, cy, wr, wr, conf);

			cx = cx +p.whiteBorder;
			cy = cy +p.whiteBorder;
			//Black Internal Circle
			graphics.setColor(conf.getPositionalsColor());
			drawPositional(graphics,cx, cy, ir, ir, conf);
		});

		return image;		
	}

	private static void drawPositional(Graphics2D graphics,int x, int y,int width,int height, QrConfiguration conf) {
		if (conf.isCircularPositionals()) {
			graphics.fillArc(x, y, width, height, 0, 360);
		}else {
			graphics.fillRoundRect(x, y, width, height, 
					(int)(width*conf.getRelativePositionalsRound()), 
					(int)(height*conf.getRelativePositionalsRound()));
		}
	}

}
