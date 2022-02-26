import ddf.minim.AudioInput
import ddf.minim.AudioPlayer
import ddf.minim.Minim
import ddf.minim.analysis.FFT
import processing.core.PApplet
import processing.core.PApplet.constrain
import processing.core.PConstants.DISABLE_DEPTH_TEST
import processing.core.PConstants.ENABLE_DEPTH_TEST

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
**/

/**
 * AudioProcessor helper class that wraps around Minim and supplies some simple
 * GUI monitoring for processing sketches.
 *
 * @param applet Processing applet to render monitoring data
 * @param streamAudio If true, will stream the default system line in audio
 * @param recordOutput If true, will begin an attempt at deterministic rendering of visual data given audio input
 * @param audioFile If streamAudio is set to false, take in a path to an audio file as source
 */
class AudioProcessor(
    private val applet: PApplet,
    private val streamAudio: Boolean = true,
    private val recordOutput: Boolean = false,
    private val audioFile: String? = null,
    val transformSettings: TransformSettings = TransformSettings()
) {
    private val minim: Minim = Minim(applet)
    private lateinit var audioInput: AudioInput
    private lateinit var audioPlayer: AudioPlayer

    private val fftBlockMax = 100 // fft range per block will be 0 - 100 by default
    private var cueProgress = 0
    private var stepAmp = transformSettings.step

    val fftData = FloatArray(transformSettings.blockCount)
    var fft: FFT

//    var fftBlockCount = 11 // num blocks to divide freq spectrum

    init {
        if (streamAudio || audioFile == null) {
            println("Audio Processor streaming audio from default system line input")
            audioInput = minim.getLineIn(Minim.MONO)
            fft = FFT(audioInput.bufferSize(), audioInput.sampleRate())
        } else {
            println("Audio Processor playing audio file $audioFile")
            audioPlayer = minim.loadFile(audioFile)
            audioPlayer.cue(cueProgress)
            if (!recordOutput) audioPlayer.play()
            fft = FFT(audioPlayer.bufferSize(), audioPlayer.sampleRate())
        }
        fft.linAverages(transformSettings.blockCount)
        fft.window(transformSettings.windowFunction)
    }

    fun process() {
        if (!streamAudio && recordOutput) {
            audioPlayer.play()
            applet.delay(30) // hard coded for 30fps; lower for 60fps renders
        }
        if (streamAudio) fft.forward(audioInput.mix) else fft.forward(audioPlayer.mix)

        updateAudioData()

        // pause playback immediately after calculating FFT values when recording to allow
        // time for stage to render
        if (!streamAudio && recordOutput) audioPlayer.pause()

        // println(fftData)
        if (recordOutput) {
            // 30fps is 33.33.. ms per frame. Correct every 4 frames by adding 1ms to keep in time with audio.
            cueProgress += if (applet.frameCount % 4 == 0) 33 else 34
            audioPlayer.cue(cueProgress)
        }
    }
    fun updateAudioData() {
        for (i in 0 until transformSettings.blockCount) {
            val tempIndexAvg: Float = fft.getAvg(i) * transformSettings.amp * stepAmp
            val tempIndexCon: Float = constrain(tempIndexAvg, 0f, fftBlockMax.toFloat())
            fftData[i] = tempIndexCon
            stepAmp += transformSettings.compStep
        }
        stepAmp = transformSettings.step
    }

    fun drawWidget() {
        applet.noLights()
        applet.hint(DISABLE_DEPTH_TEST)
        applet.noStroke()
        applet.fill(0f,200f)
        applet.rect(0f, applet.height - 112f, applet.width.toFloat(), 102f)

        for (i in 0 until transformSettings.blockCount) {
            when (i) {
                0 -> applet.fill(0x237D26.rgb())    // base  / subitem 0
                3 -> applet.fill(0x80C41C.rgb())    // snare / subitem 3
                else -> applet.fill(0xCCCCCC.rgb()) // others
            }

            applet.rect(
                5 + (i*applet.width/transformSettings.blockCount.toFloat()),
                (applet.height-fftData[i])-11,
                applet.width/transformSettings.blockCount.toFloat(),
                fftData[i]
            )
        }
        applet.hint(ENABLE_DEPTH_TEST)
    }

    fun stop() {
        audioInput.close()
        audioPlayer.close()
        minim.stop()
    }
}