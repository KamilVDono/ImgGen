import java.awt.*;
import java.io.IOException;
import java.util.Random;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Created by KVD on 15.10.2016.
 */
public class ImgGen {
    //Path to input image
    static final String fileName = "12.jpg";

    static final String outputFolder = "out/";

    //Variable to calculate triangle count
    //TriangleCount = (width * height) / triangleFactor
    static final int triangleFactor = 190;
    static final int populationCount = 55;
    static final int eliteCount = 9;


    static float mutationChance = 0.1f;
    static float mutationStrong = 10f;

    //Application shutdown itself when similarity will be bigger or equal to this
    static final float similarityEnd = 96f;

    //When level is achive save image
    static float[] simArr = {80f, 90f, 91f, 91.5f, 92f, 92.5f, 93f, 93.5f, 94f, 94.5f, 95f, 95.5f};

    static float minSimilarity;
    static int indexMin;
    static int width;
    static int height;
    static int trianglesCount;
    static double maxDistance;
    static Canvas[] population;
    static Canvas[] elite;

    long startProgramTime, startCanvasTime;
    Random rand;
    Color[][] pixels;
    int WIDTH, HEIGHT;

    private long window;

    public static void main(String[] args) throws IOException {
        long startProgramTime = System.nanoTime();
        new ImgGen().run();

        //END LOOP

        System.out.println("Program done in "+elapseTime(startProgramTime)+" sec");
        System.out.println("Best similarity: " + elite[findMaxSimilarity()].similarity);
    }

    private void loop() {
        GL.createCapabilities();

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, WIDTH, HEIGHT, 0 ,1, -1);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        initPopulation();

        long generation = 1;
        long startLoop;
        while ( !glfwWindowShouldClose(window) ) {
            startLoop = System.nanoTime();

            //START REPRODUCE
            int i, father, mother;
            i = mother = father = 0;
            while(i < populationCount){
                if(mother != father){
                    population[i] = reproduce(elite[mother], elite[father]);
                    i++;
                    if(i >= populationCount) break;
                }

                father++;
                if(father >= eliteCount){
                    mother++;
                    father = 0;
                }
                if(mother >= eliteCount){
                    mother = 0;
                    father = 1;
                }
            }
            //END REPRODUCE

            //START POPULATION
            for (i = 0; i < populationCount; i++)
            {
                population[i].calculateSimilarity(pixels, maxDistance, window);
            }
            //END POPULATION

            //START ELITE
            findElite();
            //END ELITE


            //START STUFFS
            double best = elite[findMaxSimilarity()].similarity;
            for(int ind = 0; ind < simArr.length; ind++){
                if(simArr[ind] > 0 && best >= simArr[ind]){
                    save(elite[findMaxSimilarity()].draw(window), fileName+"_"+String.format("%.3f",best)+".png");
                    simArr[ind] = -1;
                }
            }

            if(best >= similarityEnd) break;
            glfwSetWindowTitle(window, (String.format("Best similarity:%.4f Generation: %d w ", best, generation) + elapseTime(startLoop) + "sec."));
            //END STUFFS

            generation++;
            glfwPollEvents();
        }
    }

    public void run() {
        try {
            initImg();
            init();
            loop();

            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);
        } finally {
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        WIDTH = width;
        HEIGHT = height;

        window = glfwCreateWindow(WIDTH, HEIGHT, "IMGGEN", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in our rendering loop
        });


        glfwSetWindowPos(
                window,
                100,
                100
        );

        glfwMakeContextCurrent(window);
        // Disable v-sync
        glfwSwapInterval(0);

        glfwShowWindow(window);
    }

    void initImg(){
        rand = new Random();

        startProgramTime = System.nanoTime();

        pixels = Image.getImagePixels(fileName);
        if(pixels.length < 50 || pixels[0].length < 50){
            System.out.println("File don't work");
            return;
        }
        population = new Canvas[populationCount];
        elite = new Canvas[eliteCount];

        width = pixels.length;
        height = pixels[0].length;

        maxDistance = width * height * Math.sqrt(Math.pow(255,2) * 3);
        trianglesCount = (width * height) / triangleFactor;


        startCanvasTime = System.nanoTime();
    }


    Canvas reproduce(Canvas mother, Canvas father) {
        Triangle[] triangles = new Triangle[trianglesCount];
        Random r = new Random();
        for(int i = 0; i < trianglesCount; i++){
            if(r.nextFloat() < 0.5f)
                triangles[i] = mother.getTriangle(i).clone(mutationChance, width, height);
            else
                triangles[i] = father.getTriangle(i).clone(mutationChance, width, height);
        }
        return new Canvas(width,height, triangles);
    }

    int findMinSimilarity(){
        float min = elite[0].similarity;
        int index = 0;
        for(int i = 1; i < elite.length; i++){
            if(elite[i].similarity < min){
                min = elite[i].similarity;
                index = i;
            }
        }
        return index;
    }

    static int findMaxSimilarity(){
        float min = elite[0].similarity;
        int index = 0;
        for(int i = 1; i < elite.length; i++){
            if(elite[i].similarity > min){
                min = elite[i].similarity;
                index = i;
            }
        }
        return index;
    }

    Canvas createEliteFromIndividual(Canvas p){
        Canvas n = new Canvas(width, height, p.getTriangles());
        n.similarity = p.similarity;
        return n;
    }

    static String elapseTime(long startTime){
        double elapse = (System.nanoTime() - startTime) / 1000000000.0;
        return String.format("%.3f", elapse);
    }

    void save(Color[][] pixels, String name){
        Image.saveImage(outputFolder+name, pixels);
    }

    //Find elite in new population
    void findElite(){
        elite[0] = createEliteFromIndividual(population[0]);
        minSimilarity = elite[0].similarity;
        indexMin = 0;

        for(int i = 1; i < eliteCount; i++){
            elite[i] = createEliteFromIndividual(population[i]);
            if(elite[i].similarity < minSimilarity){
                minSimilarity = elite[i].similarity;
                indexMin = i;
            }
        }

        for(int i = eliteCount; i < populationCount; i++){
            if(population[i].similarity > minSimilarity){
                elite[indexMin] = createEliteFromIndividual(population[i]);
                indexMin = findMinSimilarity();
                minSimilarity = elite[indexMin].similarity;
            }
        }
    }

    //Find if someone from new population can be elite
    void tryAddToElite(){
        for(int i = 0; i < populationCount; i++){
            if(population[i].similarity > minSimilarity){
                elite[indexMin] = createEliteFromIndividual(population[i]);
                indexMin = findMinSimilarity();
                minSimilarity = elite[indexMin].similarity;
            }
        }
    }

    void initPopulation(){
        for (int i =0; i < populationCount; i++)
        {
            population[i] = new Canvas(width, height, trianglesCount);
            for(int j = 0; j < trianglesCount; j++){
                population[i].addTriangle(new Triangle(width, height));
            }
            population[i].calculateSimilarity(pixels, maxDistance, window);
        }
        findElite();
    }
}
