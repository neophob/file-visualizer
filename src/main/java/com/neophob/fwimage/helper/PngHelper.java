package com.neophob.fwimage.helper;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class PngHelper {

	public static void saveit(String name, Layer layer) {
		try {
			BufferedImage image = new BufferedImage( layer.getX(), layer.getY(),BufferedImage.TYPE_INT_RGB);
			image.setRGB(0, 0, layer.getX(), layer.getY(), layer.getBuffer(), 0, layer.getX());

			ImageIO.write(image, "png", new File(name));
		} catch (Exception e) {
			e.printStackTrace();
		}				
	}

}
