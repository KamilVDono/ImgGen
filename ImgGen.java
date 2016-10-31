import java.awt.*;
import java.io.IOException;
import java.util.Random;

import org.lwjgl.*;
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
    static final String fileName = "12.jpg";
    static final int triangleFactor = 190;
    static final int populationCount = 55;
    static final int eliteCount = 9;


    static float mutationChance = 0.1f;
    static float mutationStrong = 10f;
    static final float similarityEnd = 96f;

    static float[] simArr = {90f, 91f, 91.5f, 92f, 92.5f, 93f, 93.5f, 94f, 94.5f, 95f, 95.5f};

    static float minSimilarity;
    static int indexMin;
    static int width;
    static int height;
    static int trianglesCount;
    static double maxDistance;
    static Canvas[] population;
    static Canvas[] elite;

    long startTime, startProgramTime, startCanvasTime;
    Random rand;
    Color[][] pixels;
    int WIDTH, HEIGHT;

    private long window;

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        try {
            initImg();
            init();
            loop();

            // Free the window callbacks and destroy the window
            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);
        } finally {
            // Terminate GLFW and free the error callback
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure our window
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will be resizable

        WIDTH = width;
        HEIGHT = height;

        //if (WIDTH * HEIGHT < 500*500) WIDTH = HEIGHT = 500;

        // Create the window
        window = glfwCreateWindow(WIDTH, HEIGHT, "Hello World!", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in our rendering loop
        });

        // Get the resolution of the primary monitor
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        glfwSetWindowPos(
                window,
                100,
                100
        );

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(0);


        // Make the window visible
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
        //float[] eliteSimilarity = new float[eliteCount];

        width = pixels.length;
        height = pixels[0].length;

        maxDistance = width * height * Math.sqrt(Math.pow(255,2) * 3);
        trianglesCount = (width * height) / triangleFactor;
        //trianglesCount = 220;


        startCanvasTime = System.nanoTime();
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

        long pokolenie = 1;
        long startLoop;
        while ( !glfwWindowShouldClose(window) ) {
             // clear the framebuffer

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
            /*
            if(elite[0] == null || rand.nextFloat() < 0.1f){
                findElite();
            }
            else {
                tryAddToElite();
            }
            */
            //END ELITE


            //START STUFFS
            double best = elite[findMaxSimilarity()].similarity;
            for(int ind = 0; ind < simArr.length; ind++){
                if(simArr[ind] > 0 && best >= simArr[ind]){
                    save(elite[findMaxSimilarity()].draw(window), "out_"+String.format("%.3f",best)+".png");
                    simArr[ind] = -1;
                    //mutationStrong *= 1.2;
                    //mutationChance *= 1.2;
                }
            }

            if(best >= similarityEnd) break;
            //System.out.println("Best similarity: " + best + " Pokolenie: "+ pokolenie + " w " + elapseTime(startLoop) + "sec." + " Calkowity czas: " + elapseTime(startProgramTime));
            glfwSetWindowTitle(window, (String.format("Best similarity:%.4f Pokolenie: %d w ", best, pokolenie) + elapseTime(startLoop) + "sec."));
            //END STUFFS

            pokolenie++;
            glfwPollEvents();
        }
    }

    public static void main(String[] args) throws IOException {
        long startProgramTime = System.nanoTime();
        new ImgGen().run();

        //END LOOP

        //save(elite[findMaxSimilarity()].draw(), "out.png");

        System.out.println("Program done in "+elapseTime(startProgramTime)+" sec");
        System.out.println("Best similarity: " + elite[findMaxSimilarity()].similarity);
    }

    private static Canvas reproduce(Canvas mother, Canvas father) {
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

    static int findMinSimilarity(){
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

    static Canvas createEliteFromPopulation(Canvas p){
        Canvas n = new Canvas(width, height, p.getTriangles());
        n.similarity = p.similarity;
        return n;
    }

    static String elapseTime(long startTime){
        double elapse = (System.nanoTime() - startTime) / 1000000000.0;
        return String.format("%.3f", elapse);
    }

    static void save(Color[][] pixels, String name){
        Image.saveImage("out/"+name, pixels);
    }

    static void findElite(){
        elite[0] = createEliteFromPopulation(population[0]);
        minSimilarity = elite[0].similarity;
        indexMin = 0;

        for(int i = 1; i < eliteCount; i++){
            elite[i] = createEliteFromPopulation(population[i]);
            if(elite[i].similarity < minSimilarity){
                minSimilarity = elite[i].similarity;
                indexMin = i;
            }
        }

        for(int i = eliteCount; i < populationCount; i++){
            if(population[i].similarity > minSimilarity){
                elite[indexMin] = createEliteFromPopulation(population[i]);
                indexMin = findMinSimilarity();
                minSimilarity = elite[indexMin].similarity;
            }
        }
    }

    static void tryAddToElite(){
        for(int i = 0; i < populationCount; i++){
            if(population[i].similarity > minSimilarity){
                elite[indexMin] = createEliteFromPopulation(population[i]);
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
