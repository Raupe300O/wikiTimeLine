package basecamp.project.server.util;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Vector;


public class WaveFormGenerator {
    private AudioInputStream audioInputStream;
    private Vector<Line2D.Double> lines = new Vector<Line2D.Double>();
    private String errStr;
    private Capture capture = new Capture();
    private double duration, seconds;
    private File file;
    private SamplingGraph samplingGraph;
    private String waveformFilename;
    private Color imageBackgroundColor = new Color(20,20,20, 100);
    private int width = 1000;
    private int height = 250;

    public WaveFormGenerator(String fileName, String waveformFilename) throws UnsupportedAudioFileException, IOException {
        file = new File(fileName);
        this.waveformFilename = waveformFilename;
    }

    public void createAudioInputStream() throws Exception {
        if (file != null && file.isFile()) {
            try {
                errStr = null;
                audioInputStream = AudioSystem.getAudioInputStream(file);
                long milliseconds = (long)((audioInputStream.getFrameLength() * 1000) / audioInputStream.getFormat().getFrameRate());
                duration = milliseconds / 1000.0;
                samplingGraph = new SamplingGraph();
                samplingGraph.createWaveForm(null);
            } catch (Exception ex) {
                reportStatus(ex.toString());
                throw ex;
            }
        } else {
            reportStatus("Audio file required.");
        }
    }
    /**
     * Render a WaveForm.
     */
    class SamplingGraph implements Runnable {

        private Thread thread;
        Color jfcBlue = new Color(000, 000, 255);


        public SamplingGraph() {
        }


        public void createWaveForm(byte[] audioBytes) {

            lines.removeAllElements();  // clear the old vector

            AudioFormat format = audioInputStream.getFormat();
            if (audioBytes == null) {
                try {
                    audioBytes = new byte[
                            (int) (audioInputStream.getFrameLength()
                                    * format.getFrameSize())];
                    audioInputStream.read(audioBytes);
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                    return;
                }
            }
            int w = width;
            int h = height;
            int[] audioData = null;
            if (format.getSampleSizeInBits() == 16) {
                int nlengthInSamples = audioBytes.length / 2;
                audioData = new int[nlengthInSamples];
                if (format.isBigEndian()) {
                    for (int i = 0; i < nlengthInSamples; i++) {
                        /* First byte is MSB (high order) */
                        int MSB = audioBytes[2*i];
                        /* Second byte is LSB (low order) */
                        int LSB = audioBytes[2*i+1];
                        audioData[i] = MSB << 8 | (255 & LSB);
                    }
                } else {
                    for (int i = 0; i < nlengthInSamples; i++) {
                        /* First byte is LSB (low order) */
                        int LSB = audioBytes[2*i];
                        /* Second byte is MSB (high order) */
                        int MSB = audioBytes[2*i+1];
                        audioData[i] = MSB << 8 | (255 & LSB);
                    }
                }
            } else if (format.getSampleSizeInBits() == 8) {
                int nlengthInSamples = audioBytes.length;
                audioData = new int[nlengthInSamples];
                if (format.getEncoding().toString().startsWith("PCM_SIGN")) {
                    for (int i = 0; i < audioBytes.length; i++) {
                        audioData[i] = audioBytes[i];
                    }
                } else {
                    for (int i = 0; i < audioBytes.length; i++) {
                        audioData[i] = audioBytes[i] - 128;
                    }
                }
            }

            int frames_per_pixel = audioBytes.length / format.getFrameSize()/w;
            byte my_byte;
            double y_last = 0;
            int numChannels = format.getChannels();
            for (double x = 0; x < w && audioData != null; x++) {
                int idx = (int) (frames_per_pixel * numChannels * x);
                if (format.getSampleSizeInBits() == 8) {
                    my_byte = (byte) audioData[idx];
                } else {
                    my_byte = (byte) (128 * audioData[idx] / 32768 );
                }
                double y_new = (double) (h * (128 - my_byte) / 256);
                lines.add(new Line2D.Double(x, y_last, x, y_new));
                y_last = y_new;
            }
            saveToFile(waveformFilename);
        }


        public void saveToFile(String filename) {
            int w = width;
            int h = height;

            BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bufferedImage.createGraphics();
            g2.setComposite(AlphaComposite.Src);

            createSampleOnGraphicsContext(w, h, g2);
            g2.dispose();
            // Write generated image to a file
            try {
                // Save as PNG
                File file = new File(filename);
                ImageIO.write(bufferedImage, "png", file);
            } catch (IOException ignored) {
            }
        }


        private void createSampleOnGraphicsContext(int w, int h, Graphics2D g2) {
            g2.setBackground(imageBackgroundColor);
            g2.clearRect(0, 0, w, h);

            if (audioInputStream != null) {
                // .. render sampling graph ..
                g2.setColor(jfcBlue);
                for (int i = 1; i < lines.size(); i++) {
                    g2.draw(lines.get(i));
                }
            }
        }

        public void start() {
            thread = new Thread(this);
            thread.setName("SamplingGraph");
            thread.start();
            seconds = 0;
        }

        public void stop() {
            if (thread != null) {
                thread.interrupt();
            }
            thread = null;
        }

        public void run() {
            seconds = 0;
            while (thread != null) {
                if ( (capture.line != null) && (capture.line.isActive()) ) {
                    long milliseconds = (long)(capture.line.getMicrosecondPosition() / 1000);
                    seconds =  milliseconds / 1000.0;
                }
                try { thread.sleep(100); } catch (Exception e) { break; }
                while ((capture.line != null && !capture.line.isActive()))
                {
                    try { thread.sleep(10); } catch (Exception e) { break; }
                }
            }
            seconds = 0;
        }
    } // End class SamplingGraph

    /**
     * Reads data from the input channel and writes to the output stream
     */
    class Capture implements Runnable {

        TargetDataLine line;
        Thread thread;

        public void start() {
            errStr = null;
            thread = new Thread(this);
            thread.setName("Capture");
            thread.start();
        }

        public void stop() {
            thread = null;
        }

        private void shutDown(String message) {
            if ((errStr = message) != null && thread != null) {
                thread = null;
                samplingGraph.stop();
                System.err.println(errStr);
            }
        }

        public void run() {

            duration = 0;
            audioInputStream = null;

            // define the required attributes for our line,
            // and make sure a compatible line is supported.

            AudioFormat format = audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class,
                    format);

            if (!AudioSystem.isLineSupported(info)) {
                shutDown("Line matching " + info + " not supported.");
                return;
            }

            // get and open the target data line for capture.

            try {
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format, line.getBufferSize());
            } catch (LineUnavailableException ex) {
                shutDown("Unable to open the line: " + ex);
                return;
            } catch (SecurityException ex) {
                shutDown(ex.toString());
                //JavaSound.showInfoDialog();
                return;
            } catch (Exception ex) {
                shutDown(ex.toString());
                return;
            }

            // play back the captured audio data
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int frameSizeInBytes = format.getFrameSize();
            int bufferLengthInFrames = line.getBufferSize() / 8;
            int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
            byte[] data = new byte[bufferLengthInBytes];
            int numBytesRead;

            line.start();

            while (thread != null) {
                if((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
                    break;
                }
                out.write(data, 0, numBytesRead);
            }

            // we reached the end of the stream.  stop and close the line.
            line.stop();
            line.close();
            line = null;

            // stop and close the output stream
            try {
                out.flush();
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // load bytes into the audio input stream for playback

            byte audioBytes[] = out.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
            audioInputStream = new AudioInputStream(bais, format, audioBytes.length / frameSizeInBytes);

            long milliseconds = (long)((audioInputStream.getFrameLength() * 1000) / format.getFrameRate());
            duration = milliseconds / 1000.0;

            try {
                audioInputStream.reset();
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            samplingGraph.createWaveForm(audioBytes);
        }
    } // End class Capture

    public static void main(String [] args) throws Exception {
        if (args.length != 2) {
            printUsage();
            System.exit(1);
        }
        WaveFormGenerator formGenerator = new WaveFormGenerator(args[0], args[1]);
        formGenerator.createAudioInputStream();
    }

    private void reportStatus(String msg) {
        if ((errStr = msg) != null) {
            System.out.println(errStr);
        }
    }

    private static void printUsage() {
        System.out.println("WaveFormGenerator usage: java WaveFormGenerator.class [path to audio file] [path image file]");
    }
}