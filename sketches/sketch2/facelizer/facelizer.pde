int         stageW         = 1077;
int         stageH         = 1077;
// int         stageW         = 1920;
// int         stageH         = 1080;
color       bgColor        = #000000;
String      PATH_DATA      = "../../data/";
String      PATH_RENDER    = "../../render/";

// PRIMARY SETTINGS
boolean     fullscreen       = false;
boolean     recordOutput     = false;
boolean     streamAudio      = true;
boolean     showVisualizer   = true;

// AUDIO DATA
String          audioFile    = "dewdrop.aiff";
String          audioPath    = PATH_DATA + audioFile;
AudioProcessor  audio;

// RENDERING
String      renderFileName   = "dewdrop-01/dewdrop-";
int         outputLength     = 332; // seconds
int         recordTimeDelay  = 100;
int         renderFrameRate  = 30;
int         renderFrameCount = 0;

// *************************************************************************************************************

import hype.*;
import hype.extended.layout.*;
import hype.extended.colorist.*;
import hype.extended.behavior.*;

int cellSize = 10;
PImage img1, img2;
HPixelColorist photo, mask;
HDrawablePool pool;


void settings() {
    size(stageW, stageH, P3D);
    if (fullscreen) fullScreen();
}

void setup() {
    frameRate(renderFrameRate);
    audio = new AudioProcessor(this, streamAudio, recordOutput);
    audio.updateTransformSettings(100, 25, 0.10, 0.05, FFT.NONE);
    audio.setup(audioPath);
    smooth();
    H.init(this).background(bgColor).autoClear(true).use3D(true);

    img1 = loadImage(PATH_DATA + "chris.png");
    img2 = loadImage(PATH_DATA + "chris-mask.png");
    final HPixelColorist colors = new HPixelColorist(img1);
    final HPixelColorist mask = new HPixelColorist(img2);
    pool = new HDrawablePool((width/cellSize)*(width/cellSize))
        .add(new HIcosahedron())
        .autoAddToStage()
        .layout(new HGridLayout()
            .startX(cellSize/2)
            .startY(cellSize/2)
            .spacing(cellSize+2,cellSize+2)
            .cols(height/cellSize)
        )
        .onCreate(new HCallback() {
            public void run(Object obj) {
                HDrawable3D d = (HDrawable3D) obj;
                d
                    .depth(-100)
                    .anchorAt(H.CENTER)
                    .size(cellSize - 4)
                    .noStroke()
                ;
                mask.applyColor(d);
            }
        })
        .requestAll()
    ;
    for(HDrawable d : pool) {
        if(d.fill() <= color(1)) {
            colors.applyColor(d);
            d.bool("active", true);
        } else {
            d.bool("active", false);
            d.hide();
        }
    }
}

void draw() {   
    if (recordOutput) {
        delay(recordTimeDelay);
    }  else {
        surface.setTitle( "FPS : " + int(frameRate) );
    }
    lights();

    audio.process();
    int count = 0;
    for (HDrawable d : pool) {
        if(d.bool("active")) {
            HDrawable3D s = (HDrawable3D) d;
            float fftData = audio.fftData()[count];
            float depth = map(fftData, 0, 100, 8, 50);
            float z = map(fftData, 0, 100, -100, 150);
            s.z(z);
            s.depth((int) depth);
            if (count == 10) {
                //println(depth);
            }
            ++count;
            if (count == audio.fftData().length) count = 0;
        }
    }

    H.drawStage();

    pointLight(255, 255, 255, width/2, height/3, 50);

    if (showVisualizer) audio.drawWidget();
    if (recordOutput) renderOutput();
}

void renderOutput() {
    ++renderFrameCount;
    saveFrame(PATH_RENDER + renderFileName + "#########.tif"); if (renderFrameCount == (outputLength * renderFrameRate)) exit();
    println("Remaining frames: " + ((outputLength * renderFrameRate) - renderFrameCount));
}

void stop() {
    audio.stop();
    super.stop();
}