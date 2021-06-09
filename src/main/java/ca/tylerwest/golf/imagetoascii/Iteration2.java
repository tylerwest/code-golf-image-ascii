package ca.tylerwest.golf.imagetoascii;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.imageio.ImageIO;

public class Iteration2 {
	public static void main(String[] args) throws Exception {
		char[] chars = new char[] { '@', '#', '&', '$', '%', '?', '*', '+', ';', ':', ',', '.' }; BufferedImage source = ImageIO.read(new File("picture.jpg")); StringBuilder sb = new StringBuilder();
		for (int i = 0; i < source.getHeight(); i++) {
			for (int j = 0; j < source.getWidth(); j++) 
				sb.append(chars[Math.min(((((source.getRGB(j, i) >> 16) & 0xFF) + ((source.getRGB(j, i) >> 8) & 0xFF) + ((source.getRGB(j, i)) & 0xFF)) / 3) * chars.length / 255, chars.length - 1)]);
			sb.append('\n');
		}
		Files.write(Paths.get("output.txt"), sb.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
	}
}
