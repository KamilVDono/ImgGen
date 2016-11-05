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
    int _childernLength;

    public float similarity;

    public Canvas(int width, int height, int childrenCount){
        _children = new Triangle[childrenCount];
        _width = width;
        _height = height;
        _childernLength = 0;
    }

    public Canvas(int width, int height, Triangle[] triangles){
        _children = triangles;
        _width = width;
        _height = height;
        _childernLength = _children.length;
    }

    public void addTriangle(Triangle triangle){
        _children[_childernLength++] = triangle;
    }

    public Color[][] draw(long window){
        Color[][] pixels = new Color[_width][_height];
        glBegin(GL_TRIANGLES);
        for(int i = 0; i < _children.length; i++){
            drawTriangle(i);
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
                    float b = pixelsInt[pixelsIndex--];
                    float g = pixelsInt[pixelsIndex--];
                    float r = pixelsInt[pixelsIndex--];
                    pixels[j][i] = new Color(r, g, b);
                }
            }
        }
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

    private void drawTriangle(int index){
        _children[index].setGlColor();
        Point[] points = _children[index].getPoints();
        glVertex2f(points[0].x, points[0].y);
        glVertex2f(points[1].x, points[1].y);
        glVertex2f(points[2].x, points[2].y);
    }
}
