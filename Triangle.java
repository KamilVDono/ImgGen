import java.awt.*;
import java.util.Random;

import static org.lwjgl.opengl.GL11.glColor4d;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glColor4fv;

/**
 * Created by KVD on 15.10.2016.
 */
public class Triangle {
    Color _color;
    Point[] _points;
    Random _random;

    public Triangle(Color color, Point[] points){
        _random = new Random();
        _color = color;
        _points = points;
        sortPoints();
    }

    public Triangle(int width, int height){
        _random = new Random();

        _color = new Color(_random.nextFloat(), _random.nextFloat(), _random.nextFloat(), _random.nextFloat());

        _points = new Point[3];

        generatePoints(width, height);

        while (isTriangle()) {
            continue;
        }

        sortPoints();
    }

    public Triangle clone(float mutationProbability, int width, int height){
        float mutation = _random.nextFloat();
        float noChangesProbability = 1.0f - mutationProbability;
        float parameterMutationProbability = mutationProbability / 4.0f;
        Point[] points = new Point[_points.length];

        int r = _color.getRed();
        int g = _color.getGreen();
        int b = _color.getBlue();
        int a = _color.getAlpha();
        Color color = new Color(r, g, b, a);

        for(int i = 0; i < points.length; i++){
            points[i] = new Point((int)_points[i].getX(), (int)_points[i].getY());
        }

        if (mutation < noChangesProbability)
        {
            return new Triangle(color, points);
        }
        else if (mutation < noChangesProbability + parameterMutationProbability)
        {
            points[0].x = _points[0].x + (int)(gauss()*width);
            if (points[0].x > width) points[0].x = width;
            else if (points[0].x < 0) points[0].x = 0;

            points[0].y = _points[0].y + (int)(gauss()*height);
            if (points[0].y > height) points[0].y = height;
            else if (points[0].y < 0) points[0].y = 0;
        }
        else if (mutation < noChangesProbability + 2.0 * parameterMutationProbability)
        {
            points[1].x = _points[1].x + (int)(gauss()*width);
            if (points[1].x > width) points[1].x = width;
            else if (points[1].x < 0) points[1].x = 0;

            points[1].y = _points[0].y + (int)(gauss()*height);
            if (points[1].y > height) points[1].y = height;
            else if (points[1].y < 0) points[1].y = 0;
        }
        else if (mutation < noChangesProbability + 3.0 * parameterMutationProbability)
        {
            points[2].x = _points[2].x + (int)(gauss()*width);
            if (points[2].x > width) points[2].x = width;
            else if (points[2].x < 0) points[2].x = 0;

            points[2].y = _points[2].y + (int)(gauss()*height);
            if (points[2].y > height) points[2].y = height;
            else if (points[2].y < 0) points[2].y = 0;
        }
        else
        {
            r += gauss()*255;
            if (r > 255){ r = 255;}
            else if (r < 0){ r = 0;}

            g += gauss()*255;
            if (g > 255) g = 255;
            else if (g < 0) g = 0;

            b += gauss()*255;
            if (b > 255) b = 255;
            else if (b < 0) b = 0;

            a += gauss()*255;
            if (a> 255) a = 255;
            else if (a < 0) a = 0;

            color = new Color(r, g, b, a);
        }

        return new Triangle(color, points);
    }

    void sortPoints(){
        boolean change = true;
        while (change){
            change = false;
            for(int i =0; i < 2; i++){
                if(_points[i].y < _points[i+1].y){
                    change = true;
                    Point tmp = _points[i];
                    _points[i] = _points[i+1];
                    _points[i+1] = tmp;
                }
            }
        }
    }

    void generatePoints(int width, int height){
        _points[0] = new Point((int)(_random.nextFloat() * width), (int)(_random.nextFloat() * height));

        Point p = _points[0];

        while(_points[0].x == p.x && _points[0].y == p.y){
            p = new Point((int)(_points[0].x + _random.nextFloat() * 10 - 2),
                    (int)(_points[0].y + _random.nextFloat() * 10 - 2));
        }

        _points[1] = p;

        while((_points[0].x == p.x && _points[0].y == p.y) || (_points[1].x == p.x && _points[1].y == p.y)){
            p = new Point((int)(_points[0].x + _random.nextFloat() * 10 - 2),
                    (int)(_points[0].y + _random.nextFloat() * 10 - 2));
        }

        _points[2] = p;
    }

    boolean isTriangle(){
        return false;
    }

    float gauss(){
        float x = _random.nextFloat();
        float y = _random.nextFloat();

        float r = 0.003f * ImgGen.mutationStrong * ((float)Math.sqrt(-2.0f * (float)Math.log(x)) * (float)Math.cos(2.0f * Math.PI * y));
        if (r<-1.0) r=0f;
        if (r>1.0) r=0f;
        return r;
    }

    public Point[] getPoints(){return _points;}
    public Color getColor(){return _color;}
    public void setGlColor(){
        float[] ar = _color.getComponents(null);
        glColor4fv(ar);
    }
}
