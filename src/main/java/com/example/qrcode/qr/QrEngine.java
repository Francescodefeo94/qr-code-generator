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

	private static void validateConfiguration(QrConfiguration conf) throws QrConfiguration.InvalidConfigurationException {
		if (conf == null) throw new QrConfiguration.InvalidConfigurationException("Configuration is null");
		if (conf.getDarkColor() == null) throw new QrConfiguration.InvalidConfigurationException("Configuration darkColor is null");
		if (conf.getLightColor() == null) throw new QrConfiguration.InvalidConfigurationException("Configuration lightColor is null");
		if (conf.getPositionalsColor() == null) throw new QrConfiguration.InvalidConfigurationException("Configuration positionalsColor is null");
		if (conf.getSize()%4 != 0) throw new QrConfiguration.InvalidConfigurationException("Configuration size must be multiple of 4");
		if (conf.getRelativeBorderSize() > .1) throw new QrConfiguration.InvalidConfigurationException("Relative border too big, set it < .1");
		if (conf.getRelativeLogoSize() > .25) throw new QrConfiguration.InvalidConfigurationException("Relative logo too big set it < .25");
		if (conf.getRelativeBorderRound() > .25) throw new QrConfiguration.InvalidConfigurationException("Relative border round too big set it < .25");
		return;
	}

	public static void buildQrCode(String text, File outputFile, QrConfiguration conf) throws WriterException, IOException, QrConfiguration.InvalidConfigurationException {
		BufferedImage img = buildQrCode(text, conf);
		ImageIO.write(img, "png", outputFile);
	}

	public static BufferedImage buildQrCode(String text, QrConfiguration conf) throws WriterException, QrConfiguration.InvalidConfigurationException {
		validateConfiguration(conf);
		QRCodeWriter.QRCodeWithPositionals qr = encode(text,conf);
		BufferedImage qrImage = baseImage(qr, conf);
		BufferedImage layered = layer(qrImage,null,conf);
		return layered;
	}

	public static BufferedImage buildQrCodeWithLogo(String text, File logoImage, QrConfiguration conf) throws WriterException, IOException {
		return buildQrCodeWithLogo(text, ImageIO.read(logoImage), conf);
	}

	public static BufferedImage buildQrCodeWithLogo(String text, BufferedImage logoImage, QrConfiguration conf) throws WriterException {
		QRCodeWriter.QRCodeWithPositionals qr = encode(text,conf);
		BufferedImage qrImage = baseImage(qr, conf);
		BufferedImage layered = layer(qrImage,logoImage,conf);
		return layered;
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

			int computedSize = conf.getSize()-(2*border);
			int centerSize = (int)Math.floor(computedSize*conf.getRelativeLogoSize());
			int logoSize = centerSize-border/2;
			int cx = conf.getSize()/2 -centerSize/2;
			int cy = conf.getSize()/2 -centerSize/2;

			//White center
			graphics.setColor(white);
			graphics.fillArc(cx, cy, centerSize, centerSize, 0, 360);

			int lx = cx+(border/4);
			int ly = cy+(border/4);


			Ellipse2D.Double circle = new Ellipse2D.Double(
					0,0,logoSize,logoSize);
			Area circleArea = new Area(circle);
			BufferedImage cropped = new BufferedImage(logoSize, logoSize, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D croppedGraphics = (Graphics2D) cropped.getGraphics();
			croppedGraphics.setClip(circleArea);
			croppedGraphics.drawImage(logoImage, 0, 0, logoSize, logoSize, null);

			graphics.drawImage(cropped, lx, ly, logoSize, logoSize,null);
			croppedGraphics.dispose();
			graphics.dispose();

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
