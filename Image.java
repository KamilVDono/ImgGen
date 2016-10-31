import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

public class Image {

    public static Color[][] getImagePixels(String fileName){
        BufferedImage img;
        int[][] pixelsInt = new int[1][1];
        try{
            img = ImageIO.read(new File(fileName));
            pixelsInt = getIntPixels(img);
        }
        catch (IOException e){
            System.out.println("fileName = [" + fileName + "] don't exist");
        }
        finally {
            return getPixels(pixelsInt);
        }
    }

    public static boolean saveImage(String fileName, Color[][] pixels){
        BufferedImage img = new BufferedImage(pixels.length, pixels[0].length, BufferedImage.TYPE_INT_ARGB);
        for(int i = 0; i < pixels.length; i++){
            for (int j = 0; j < pixels[0].length; j++){
                img.setRGB(i, j, pixels[i][j].getRGB());
            }
        }
        try{
            ImageIO.write(img, "png", new File(fileName));
            return true;
        }
        catch (IOException e){
            System.out.println("fileName = [" + fileName + "] don't exist");
            return false;
        }
    }

    public static Color sumColors(Color c0, Color c1){
        double totalAlpha = c0.getAlpha() + c1.getAlpha();
        double weight0 = c0.getAlpha() / totalAlpha;
        double weight1 = c1.getAlpha() / totalAlpha;

        double r = weight0 * c0.getRed() + weight1 * c1.getRed();
        double g = weight0 * c0.getGreen() + weight1 * c1.getGreen();
        double b = weight0 * c0.getBlue() + weight1 * c1.getBlue();
        double a = Math.max(c0.getAlpha(), c1.getAlpha());

        return new Color((int) r, (int) g, (int) b, (int) a);
    }

    private static Color[][] getPixels(int[][] intPixels){
        int rowLenght = intPixels.length;
        int columnLenght = intPixels[0].length;
        Color[][] pixels = new Color[rowLenght][columnLenght];
        for (int i = 0; i < columnLenght; i++){
            for (int j = 0; j < rowLenght; j++){
                pixels[j][i] = new Color(intPixels[j][i]);
            }
        }
        return pixels;
    }

    private static int[][] getIntPixels(BufferedImage image) {

        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;

        int[][] result = new int[width][height];
        if (hasAlphaChannel) {
            final int pixelLength = 4;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
                argb += ((int) pixels[pixel + 1] & 0xff); // blue
                argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
                result[col][row] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        } else {
            final int pixelLength = 3;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += -16777216; // 255 alpha
                argb += ((int) pixels[pixel] & 0xff); // blue
                argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
                result[col][row] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        }

        return result;
    }
}
