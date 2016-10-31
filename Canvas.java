import java.awt.*;

import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glVertex2f;

/**
 * Created by KVD on 15.10.2016.
 */
public class Canvas {
    Triangle[] _children;
    int _width;
    int _height;
    //Color[][] _pixels;
    int _pixelIndex;

    public float similarity;

    public Canvas(int width, int height, int childrenCount){
        _children = new Triangle[childrenCount];
        _width = width;
        _height = height;
        //_pixels = new Color[width][height];
        _pixelIndex = 0;
    }

    public Canvas(int width, int height, Triangle[] triangles){
        _children = triangles;
        _width = width;
        _height = height;
        //_pixels = new Color[width][height];
        _pixelIndex = _children.length;
    }

    public void addTriangle(Triangle triangle){
        _children[_pixelIndex++] = triangle;
    }

    public Color[][] draw(long window){
        Color[][] pixels = new Color[_width][_height];
        glBegin(GL_TRIANGLES);
        for(int i = 0; i < _children.length; i++){
            drawTriangle(i, pixels);
        }
        glEnd();
        glfwSwapBuffers(window);

        float[] pixelsInt = new float[_width*_height*3];
        glReadBuffer(GL_FRONT);
        glReadPixels(0,0, _width, _height, GL_RGB, GL_FLOAT, pixelsInt);
        int pixelsIndex = pixelsInt.length-1;
        for (int i = 0; i < pixels[0].length; i++){
            for(int j = pixels.length-1; j >= 0; j--){
                if(pixels[j][i] == null){
                    //int indexGL = (i*pixels.length*4) + (j*4);
                    //float a = pixelsInt[pixelsIndex--];
                    float b = pixelsInt[pixelsIndex--];
                    float g = pixelsInt[pixelsIndex--];
                    float r = pixelsInt[pixelsIndex--];
                    //pixels[j][i] = new Color(r, g, b, a);
                    pixels[j][i] = new Color(r, g, b);
                }
            }
        }
        //Image.saveImage("t.png", pixels);
        return pixels;
    }

    public Triangle[] getTriangles(){
        return  _children;
    }
    public Triangle getTriangle(int index){return _children[index];}

    public float calculateSimilarity(Color[][] orginPixels, double maxDistance, long window){
        double distance = 0.0f;
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        Color[][] newPixels = draw(window);

        for(int i = 0; i < orginPixels.length; i++){
            for(int j = 0; j < orginPixels[0].length; j++){
                distance += Math.sqrt(
                        (Math.pow(orginPixels[i][j].getRed()-newPixels[i][j].getRed(), 2)+
                         Math.pow(orginPixels[i][j].getGreen()-newPixels[i][j].getGreen(), 2)+
                         Math.pow(orginPixels[i][j].getBlue()-newPixels[i][j].getBlue(), 2))
                );
            }
        }
        similarity = (float)(((maxDistance-distance) * 100.0)/maxDistance);
        return similarity;
    }

    private void drawTriangle(int index, Color[][] pixels){
        _children[index].setGlColor();
        Point[] points = _children[index].getPoints();
        glVertex2f(points[0].x, points[0].y);
        glVertex2f(points[1].x, points[1].y);
        glVertex2f(points[2].x, points[2].y);

        /*
        Point[] points = _children[index].getPoints();
        if (points[1].y == points[2].y)
        {
            fillBottomFlatTriangle(points, _children[index].getColor(), pixels);
        }
        else if (points[0].y == points[1].y)
        {
            fillTopFlatTriangle(points, _children[index].getColor(), pixels);
        }
        else
        {
            Point v4 = new Point(
                    (int)(points[0].x +
                            ((float)(points[1].y - points[0].y) / (float)(points[2].y - points[0].y))
                            * (points[2].x - points[0].x)),
                    points[1].y);



            Point[] ps = points.clone();
            Point[] ps2 = points.clone();
            ps2[2] = v4;
            ps[0] = ps[1];
            ps[1] = v4;


            fillBottomFlatTriangle(ps2, _children[index].getColor(), pixels);
            fillTopFlatTriangle(ps, _children[index].getColor(), pixels);
        }
        */
    }

    private void fillBottomFlatTriangle(Point[] points, Color color, Color[][] pixels)
    {
        float invslope1 = (points[1].x - points[0].x) / (float)(points[1].y - points[0].y);
        float invslope2 = (points[2].x - points[0].x) / (float)(points[2].y - points[0].y);

        float curx1 = points[0].x;
        float curx2 = points[0].x;

        for (int scanlineY = points[0].y; scanlineY >= points[1].y; scanlineY--)
        {
            Point p1 = new Point((int)curx1, scanlineY);
            Point p2 = new Point((int)curx2, scanlineY);
            drawLine(p1, p2, color, pixels);
            curx1 -= invslope1;
            curx2 -= invslope2;
        }
    }

    private void fillTopFlatTriangle(Point[] points, Color color, Color[][] pixels)
    {
        float invslope1 = (points[2].x - points[0].x) / (float)(points[2].y - points[0].y);
        float invslope2 = (points[2].x - points[1].x) / (float)(points[2].y - points[1].y);

        float curx1 = points[2].x;
        float curx2 = points[2].x;

        for (int scanlineY = points[2].y; scanlineY < points[0].y; scanlineY++)
        {
            Point p1 = new Point((int)curx1, scanlineY);
            Point p2 = new Point((int)curx2, scanlineY);
            drawLine(p1, p2, color, pixels);
            curx1 += invslope1;
            curx2 += invslope2;
        }
    }

    private void drawLine(Point p1, Point p2, Color color, Color[][] pixels){
        if(p1.x > p2.x){
            Point tmp = p1;
            p1 = p2;
            p2 = tmp;
        }
        for(int i = p1.x; i <= p2.x; i++){
            setPixel(i, p1.y, color, pixels);
        }
    }

    private void setPixel(int x, int y, Color color, Color[][] _pixels){
        if(_pixels.length <= x || _pixels[0].length <= y) return;
        if(x < 0 || y < 0) return;
        if(_pixels[x][y] == null) _pixels[x][y] = color;
        else _pixels[x][y] = Image.sumColors(color, _pixels[x][y]);
    }


}
