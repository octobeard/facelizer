import ddf.minim.analysis.FFT
import ddf.minim.analysis.WindowFunction

data class TransformSettings(
    // num blocks to divide freq spectrum
    val blockCount: Int = 11,
    // base amplification of fft signal for all blocks in range
    val amp: Float = 40f,
    // linear base compensation for each block up chain
    val step: Float = 0.25f,
    // compounded compensation coefficient
    val compStep: Float = 0.40f,
    // windowing function
    val windowFunction: WindowFunction = FFT.NONE
)