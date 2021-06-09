# Code Golf - ASCII Image Converter
> Code golf is a type of recreational computer programming competition in which participants strive to achieve 
> the shortest possible source code that implements a certain algorithm.
[Wikipedia](https://en.wikipedia.org/wiki/Code_golf)

## Goal
Challenge myself to implement an image-to-ascii converter in Java, in as few lines as possible.

### Algorithm
- Read in an image from disk
- Convert each pixel to grayscale, averaging the red, green, and blue channels (ignore alpha)
- Map each grayscale pixel value to an associated character to represent contrast
- Write the resulting text out to disk

### Rules
1. Use as few lines, `{}` and `;` as possible. The `main()` method definition and `{}` do not count towards the overall score.
2. `main()` can throw `Exception` to remove the need for exception handling.
3. Resource leaks are allowed, since the program will terminate immediately after a single run.
4. Output text character count **must** match the input image pixel count.

#### Iteration 1 - The basic implementation
- `char[]` defines the mapping from 'darkest' to 'lightest' pixels to characters
- Image file is loaded, and iterate over the pixels row-by-row
- Pixel RGB is bit-shifted to get the red, green, and blue channels, ignoring alpha
- Red, green and blue channels are averaged to get a grayscale value
- Grayscale value is converted to an index in the `char[]` using cross-multiplication

| Lines        | {}           | ;            | Score        |
| ------------ | ------------ | ------------ | ------------ |
| 15           | 6            | 15           | **36**       |


```java
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
```

#### Iteration 2 - Consolidating variables, removing lines
I can reduce some lines by consolidating the RGB `int` variables into a single expression to calculate the average.
Moving everything into a single line expression inside the `for` loops allows us to remove the braces as well.
I can save some lines by moving all of our variable declarations onto a single line.
- Removed `int rgb`
- Removed `int r, g, b`
- Removed `int average`
- Replaced variables with calls directly to `source.getRGB(j, i)`
- Removed braces from the `for` loops
- Moved variable declarations to a single line

| Lines        | {}           | ;            | Score        |
| ------------ | ------------ | ------------ | ------------ |
| 15 -> 6      | 6 -> 2       | 10           | **18**       |


```java
public static void main(String[] args) throws Exception {
	BufferedImage source = ImageIO.read(new File("picture.jpg")); StringBuilder sb = new StringBuilder(); char[] chars = new char[] { '@', '#', '&', '$', '%', '?', '*', '+', ';', ':', ',', '.' };
	for (int i = 0; i < source.getHeight(); i++) 
		for (int j = 0; j < source.getWidth(); j++) 
			sb.append(chars[Math.min(((((source.getRGB(j, i) >> 16) & 0xFF) + ((source.getRGB(j, i) >> 8) & 0xFF) + ((source.getRGB(j, i)) & 0xFF)) / 3) * chars.length / 255, chars.length - 1)]);
		sb.append('\n');
	Files.write(Paths.get("output.txt"), sb.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
}
```

#### Iteration 3 - Removing `for` loops
Two `for` loops are used to iterate over the pixels, `getRGB(int, int)` to retreive the `int` RGB value and calculate the grayscale values.

Instead I can use the [`BufferedImage.getRGB(int, int, int, int, int[], int, int)`](https://docs.oracle.com/javase/8/docs/api/java/awt/image/BufferedImage.html#getRGB-int-int-int-int-int:A-int-int-) method to
retreive the entire image pixels as an `int[]` and use [`Arrays.stream(int[])`](https://docs.oracle.com/javase/8/docs/api/java/util/Arrays.html#stream-int:A-) to iterate over the pixels.
Funny enough, this actually realized a performance gain due to getting the RGB array *once* instead of getting each individual pixels' RGB value.

This poses a problem however - in a flat `int[]` of pixels how do I know when to insert the `\n` into the output text buffer? 
I can determine when a row of pixels has 'ended' by performing a modulus of text buffer length with the image width and check for a zero remainder.

- Replaced `for` loop iteration with streams and lambdas `Arrays.stream(int[]).forEach(int)`
- Replaced the `\n` append in the outer `for` loop with a modulus check against the width of the image in the `forEach(int)`
- Moved the `ImageIO.read(String)` image assignment into `Arrays.stream(int[])`
- Introduced single character variable names for shorter code length

| Lines        | {}           | ;            | Score        |
| ------------ | ------------ | ------------ | ------------ |
| 6 -> 3       | 2            | 10 -> 5      | **10**       |


```java
public static void main(String[] args) throws Exception {
	BufferedImage i; StringBuilder s = new StringBuilder(); char[] c = new char[] { '@', '#', '&', '$', '%', '?', '*', '+', ';', ':', ',', '.' };
	Arrays.stream((i = ImageIO.read(new File("picture.jpg"))).getRGB(0, 0, i.getWidth(), i.getHeight(), null, 0, i.getWidth())).forEach(p -> s.append(s.length() % i.getWidth() == 0 ? '\n' : c[Math.min((((p >> 16) & 0xFF) + ((p >> 8) & 0xFF) + ((p) & 0xFF)) / 3 * c.length / 255, c.length - 1)]));
	Files.write(Paths.get("output.txt"), s.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
}
```

#### Iteration 4 - Removing the text buffer
A `StringBuilder` is used as a buffer to append characters to as the pixels are being iterated over. The `StringBuilder` is then written to disk at the end.
This isn't really necessary if I can find a way to write the characters to disk as soon as they are processed.
However this poses a problem - I also need to keep track of the number of characters so I can determine when to insert the `\n` at the end of a line.
Previously this was accomplished via `StringBuilder.length()`.

Unfortunately I can't increment a simple `int count` via `count++` because the lambda expression requires that variables referenced outside the expression be effectively `final`. So I need to use a type that allows for reading and incrementing (in one method call, so as not to add an unnecessary line).
The answer was found in [`AtomicInteger.incrementAndGet()`](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/atomic/AtomicInteger.html#incrementAndGet--)

This allowed me to both increment and get the character count for the `\n` insertion, and replace the `StringBuilder` text buffer in favor of a `PrintWriter`.
I chose `PrintWriter` because it does not enforce checking for an `IOException` like other writers. This saves me from needing to add a `try{} catch(){}`, saving unnecessary lines and braces.
However `PrintWriter` is a buffered writer so it's necessary to call `flush()` after every `append(char)` to ensure the `char` was written to disk.

- Introduced `AtomicInteger` to track the number of characters written to disk
- Replaced `StringBuilder` with `PrintWriter` to get rid of the additional 'write buffer to disk' line
- Added `flush()` to each `append(char)` to ensure every character was written to disk immediately

| Lines        | {}           | ;            | Score        |
| ------------ | ------------ | ------------ | ------------ |
| 3 -> 2       | 2            | 5            | **9**        |


```java
public static void main(String[] args) throws Exception {
	BufferedImage i; AtomicInteger a = new AtomicInteger(); PrintWriter o = new PrintWriter("output.txt"); char[] c = new char[] { '@', '#', '&', '$', '%', '?', '*', '+', ';', ':', ',', '.' };
	Arrays.stream((i = ImageIO.read(new File("picture.jpg"))).getRGB(0, 0, i.getWidth(), i.getHeight(), null, 0, i.getWidth())).forEach(p -> o.append(a.incrementAndGet() % i.getWidth() == 0 ? '\n' : c[Math.min((((p >> 16) & 0xFF) + ((p >> 8) & 0xFF) + ((p) & 0xFF)) / 3 * c.length / 255, c.length - 1)]).flush());
}
```

#### Iteration 5 - Removing the `char[]` mapping array
I am using a `char[]` to map the grayscale values to a character representing various 'contrasts'. The array is sorted from 'darkest' to 'lightest' characters.
This isn't really necessary if I can find a starting code point and range that represents a somewhat-sorted set of characters from 'darkest' to 'lightest'.
Instead of an index in an array, I can simply calculate the `int` value of the character, cast to `char` and return that.

Remembering old DOS screens and ANSI characters, I began to look at the Unicode character set, especially around the [Block Elements](https://unicode-table.com/en/blocks/block-elements/) code range.
While the code points `\u2591` to `\u2593` represent the 'shade' characters, they are in the wrong order and only allow for three shades of contrast.

I ended up going with a code point start index of 35 and a range of 12, giving a possible character set of `[ #$%&'()*+,-. ]`.
This represented a pretty good set of contrasting characters, mostly in order from 'darkest' to 'lightest'.

- Removed the `char[]` mapping array
- Calculated the `char` value to write to disk instead of referencing an index in the mapping array

| Lines        | {}           | ;            | Score        |
| ------------ | ------------ | ------------ | ------------ |
| 2            | 2 -> 0       | 5 -> 4       | **6**        |


```java
public static void main(String[] args) throws Exception {
	BufferedImage i; AtomicInteger a = new AtomicInteger(); PrintWriter o = new PrintWriter("output.txt", "UTF-8");
	Arrays.stream((i = ImageIO.read(new File("picture3.jpg"))).getRGB(0, 0, i.getWidth(), i.getHeight(), null, 0, i.getWidth())).forEach(p -> o.append(a.incrementAndGet() % i.getWidth() == 0 ? '\n' : (char)(((((p >> 16) & 0xFF) + ((p >> 8) & 0xFF) + ((p) & 0xFF)) / 3 * 12 / 255) + 35)).flush());
}
```

#### Final Iteration 5 - The one-liner
Finally, let's move everything together to remove one additional line.

| Lines        | {}           | ;            | Score        |
| ------------ | ------------ | ------------ | ------------ |
| 2 -> 1       | 0            | 4            | **5**        |


```java
public static void main(String[] args) throws Exception {
	BufferedImage i; AtomicInteger a = new AtomicInteger(); PrintWriter o = new PrintWriter("output.txt", "UTF-8"); Arrays.stream((i = ImageIO.read(new File("picture3.jpg"))).getRGB(0, 0, i.getWidth(), i.getHeight(), null, 0, i.getWidth())).forEach(p -> o.append(a.incrementAndGet() % i.getWidth() == 0 ? '\n' : (char)(((((p >> 16) & 0xFF) + ((p >> 8) & 0xFF) + ((p) & 0xFF)) / 3 * 12 / 255) + 35)).flush());
}
```