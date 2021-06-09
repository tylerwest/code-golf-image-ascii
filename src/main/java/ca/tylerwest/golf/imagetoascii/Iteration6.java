package ca.tylerwest.golf.imagetoascii;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

public class Iteration6 {
	public static void main(String[] args) throws Exception {
		BufferedImage i; AtomicInteger a = new AtomicInteger(); PrintWriter o = new PrintWriter("output.txt", "UTF-8"); Arrays.stream((i = ImageIO.read(new File("picture.jpg"))).getRGB(0, 0, i.getWidth(), i.getHeight(), null, 0, i.getWidth())).forEach(p -> o.append(a.incrementAndGet() % i.getWidth() == 0 ? '\n' : (char)(((((p >> 16) & 0xFF) + ((p >> 8) & 0xFF) + ((p) & 0xFF)) / 3 * 12 / 255) + 35)).flush());
	}
}
