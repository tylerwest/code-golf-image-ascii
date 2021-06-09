package ca.tylerwest.golf.imagetoascii;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import javax.imageio.ImageIO;

public class Iteration3 {
	public static void main(String[] args) throws Exception {
		BufferedImage i; StringBuilder s = new StringBuilder(); char[] c = new char[] { '@', '#', '&', '$', '%', '?', '*', '+', ';', ':', ',', '.' };
		Arrays.stream((i = ImageIO.read(new File("picture.jpg"))).getRGB(0, 0, i.getWidth(), i.getHeight(), null, 0, i.getWidth())).forEach(p -> s.append(s.length() % i.getWidth() == 0 ? '\n' : c[Math.min((((p >> 16) & 0xFF) + ((p >> 8) & 0xFF) + ((p) & 0xFF)) / 3 * c.length / 255, c.length - 1)]));
		Files.write(Paths.get("output.txt"), s.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
	}
}
