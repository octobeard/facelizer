import ddf.minim.*;
import ddf.minim.analysis.*;

/** 
    window functions:

    FFT.NONE - default
    FFT.BARTLETT
    FFT.BARTLETTHANN
    FFT.BLACKMAN
    FFT.COSINE
    FFT.GAUSS
    FFT.HAMMING
    FFT.HANN
    FFT.LANCZOS
    FFT.TRIANGULAR
*/

class AudioProcessor {
    boolean     streamAudio    = true,
                recordOutput   = false;

    Minim       minim;
    AudioInput  audioInput     = null;
    AudioPlayer audioPlayer    = null;

    FFT         fft;

    int         fftBlockCount  = 11;
    float[]     fftData        = new float[fftBlockCount];
    int         fftBlockMax    = 100; // fft range per block will be 0 - 100 by default

    float       fftAmp         = 40.0; // base amplification of fft signal for all blocks in range
    float       fftStep        = 0.25; // linear base compensation for each block up chain
    float       fftCompStep    = 0.4;  // compounded compensation coefficient
    WindowFunction fftWindowFunc = FFT.NONE; // windowing function

    float       fftStepAmp     = fftStep;
    int         cueProgress    = 0;

    PApplet     applet;

    AudioProcessor(PApplet app) {
        applet = app;
    }

    AudioProcessor(PApplet app, boolean strAudio) {
        applet = app;
        streamAudio = strAudio;
    }

    AudioProcessor(PApplet app, boolean strAudio, boolean rcrdOutput) {
        applet = app;
        streamAudio = strAudio;
        recordOutput = rcrdOutput;
    }

    float[] fftData() {
        return fftData;
    }

    void updateTransformSettings(int   blockCount,
                                 float amp,
                                 float index,
                                 float step,
                                 WindowFunction wfunc) {
        fftBlockCount = blockCount;
        fftAmp = amp;
        fftStep = index;
        fftCompStep = step;
        fftWindowFunc = wfunc;
        fftData = new float[fftBlockCount];    
    }

    void setup(String audioFile) {
        minim = new Minim(applet);
        if (streamAudio == true || audioFile == null) {
            audioInput = minim.getLineIn(Minim.MONO);
            fft = new FFT(audioInput.bufferSize(), audioInput.sampleRate());
        } else {
            audioPlayer = minim.loadFile(audioFile);
            audioPlayer.cue(cueProgress);
            if (!recordOutput) audioPlayer.play();
            fft = new FFT(audioPlayer.bufferSize(), audioPlayer.sampleRate());
        }
        fft.linAverages(fftBlockCount);
        fft.window(fftWindowFunc);
    }

    void process() {
        if (!streamAudio && recordOutput) {
            audioPlayer.play();
            delay(30); // hard coded for 30fps; lower for 60fps renders
        }

        if (streamAudio == true) {
            fft.forward(audioInput.mix);
        } else {
            fft.forward(audioPlayer.mix);
        }

        updateAudioData();

        // pause playback immediately after calculating FFT values when recording to allow
        // time for stage to render
        if (!streamAudio && recordOutput) audioPlayer.pause();

        // println(fftData);
        if (recordOutput) {
            // 30fps is 33.33.. ms per frame. Correct every 4 frames by adding 1ms to keep in time with audio.
            if (frameCount % 4 == 0) {
                cueProgress += 33;
            } else {
                cueProgress += 34;
            }

            audioPlayer.cue(cueProgress);
        }
    }

    void updateAudioData() {
        for (int i = 0; i < fftBlockCount; ++i) {
            float tempIndexAvg = (fft.getAvg(i) * fftAmp) * fftStepAmp;
            float tempIndexCon = constrain(tempIndexAvg, 0, fftBlockMax);
            fftData[i]     = tempIndexCon;
            fftStepAmp += fftCompStep;
        }
        fftStepAmp = fftStep;
    }

    void drawWidget() {
        noLights();
        hint(DISABLE_DEPTH_TEST);
        noStroke(); fill(0,200); rect(0, height-112, width, 102);

        for (int i = 0; i < fftBlockCount; ++i) {
            if     (i==0) fill(#237D26); // base  / subitem 0
            else if(i==3) fill(#80C41C); // snare / subitem 3
            else          fill(#CCCCCC); // others

            rect(5 + (i*width/fftBlockCount), (height-fftData[i])-11, width/fftBlockCount, fftData[i]);
        }
        hint(ENABLE_DEPTH_TEST);
    }

    void stop() {
        if (audioInput != null) audioInput.close();
        if (audioPlayer != null) audioPlayer.close();
        
        minim.stop();  
    }
}