package ca.tylerwest.golf.imagetoascii;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.imageio.ImageIO;

public class Iteration1 {
	public static void main(String[] args) throws Exception {
		char[] chars = new char[] { '@', '#', '&', '$', '%', '?', '*', '+', ';', ':', ',', '.' };
		BufferedImage source = ImageIO.read(new File("picture.jpg"));
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < source.getHeight(); i++) {
			for (int j = 0; j < source.getWidth(); j++) {
				int rgb = source.getRGB(j, i);
				int r = rgb >> 16 & 0xFF;
				int g = rgb >> 8 & 0xFF;
				int b = rgb & 0xFF;
				int average = (r + g + b) / 3;
				sb.append(chars[Math.min(average * chars.length / 255, chars.length - 1)]);
			}
			sb.append('\n');
		}
		Files.write(Paths.get("output.txt"), sb.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
	}
}
