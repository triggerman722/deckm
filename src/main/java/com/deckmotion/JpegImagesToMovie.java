package com.deckmotion;
/*
 * @(#)JpegImagesToMovie.java   1.3 01/03/13
 *
 * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */



import com.sun.media.codec.video.colorspace.JavaRGBToYUV;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.util.Vector;

import javax.media.*;
import javax.media.control.TrackControl;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.format.JPEGFormat;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.format.YUVFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;



/**
 * This program takes a list of JPEG image files and convert them into a
 * QuickTime movie.
 */


public class JpegImagesToMovie implements ControllerListener, DataSinkListener {

    private static final String inputformat = VideoFormat.YUV;
    private static final String outputformat = VideoFormat.JPEG;

    public boolean doIt(int width, int height, int frameRate, Vector inFiles,
                        MediaLocator outML) throws MalformedURLException {
        ImageDataSource ids = new ImageDataSource(width, height, frameRate,
                inFiles);

        Processor p;

        try {
            //System.err
            //      .println("- create processor for the image datasource ...");
            p = Manager.createProcessor(ids);
        } catch (Exception e) {
            System.err
                    .println("Yikes!  Cannot create a processor from the data source.");
            return false;
        }

        p.addControllerListener(this);

        // Put the Processor into configured state so we can set
        // some processing options on the processor.
        p.configure();
        if (!waitForState(p, p.Configured)) {
            System.err.println("Failed to configure the processor.");
            return false;
        }

        // Set the output content descriptor to QuickTime.
        p.setContentDescriptor(new ContentDescriptor(
                FileTypeDescriptor.QUICKTIME));



        // Query for the processor for supported formats.
        // Then set it on the processor.
        TrackControl tcs[] = p.getTrackControls();
        Format f[] = tcs[0].getSupportedFormats();
        if (f == null || f.length <= 0) {
            System.err.println("The mux does not support the input format: "
                    + tcs[0].getFormat());
            return false;
        }

        tcs[0].setFormat(f[0]);

        //System.err.println("Setting the track format to: " + f[0]);

        // We are done with programming the processor. Let's just
        // realize it.
        p.realize();
        if (!waitForState(p, p.Realized)) {
            System.err.println("Failed to realize the processor.");
            return false;
        }

        // Now, we'll need to create a DataSink.
        DataSink dsink;
        if ((dsink = createDataSink(p, outML)) == null) {
            System.err
                    .println("Failed to create a DataSink for the given output MediaLocator: "
                            + outML);
            return false;
        }

        dsink.addDataSinkListener(this);
        fileDone = false;

        System.out.println("Generating the video : "+outML.getURL().toString());

        // OK, we can now start the actual transcoding.
        try {
            p.start();
            dsink.start();
        } catch (IOException e) {
            System.err.println("IO error during processing");
            return false;
        }

        // Wait for EndOfStream event.
        waitForFileDone();

        // Cleanup.
        try {
            dsink.close();
        } catch (Exception e) {
        }
        p.removeControllerListener(this);

        System.out.println("Video creation completed!!!!!");


        return true;
    }
    public void mergeFiles(MediaLocator oml, File realAudioFile) {
        try {
            //Registry.registerYUVHandler();

            DataSource videoDataSource = javax.media.Manager.createDataSource(oml.getURL()); //your video file
            DataSource audioDataSource = javax.media.Manager.createDataSource(realAudioFile.toURI().toURL()); // your audio file
            DataSource mixedDataSource = null; // data source to combine video with audio
            DataSource arrayDataSource[] = new DataSource[2]; //data source array
            DataSource outputDataSource = null; // file to output

            DataSink outputDataSink = null; // datasink for output file

            FileTypeDescriptor outputType = new FileTypeDescriptor(FileTypeDescriptor.QUICKTIME); //output video format type

            Format outputFormat[] = new Format[2]; //format array


            javax.media.format.AudioFormat audioMediaFormat = new javax.media.format.AudioFormat(
                    javax.media.format.AudioFormat.LINEAR, 44100, 16, 1); //audio format


            outputFormat[0] = new VideoFormat(VideoFormat.CINEPAK);//new H264Format(new Dimension(176, 144), 38016, 15);
            outputFormat[1] = audioMediaFormat;





            Processor videoProcessor = Manager.createProcessor(videoDataSource);

            Processor audioProcessor = Manager.createProcessor(audioDataSource);


            Processor processor = null;

            videoProcessor.configure();

            //wait till configured
            while(videoProcessor.getState() < 180) {
                Thread.sleep(20);
            }

            /*Codec jencoder  = (Codec) new JavaRGBToYUV();
            Codec[] codec = {jencoder};
            TrackControl[] tc = videoProcessor.getTrackControls();
            for (TrackControl t : tc) {
                if (t.getFormat() instanceof VideoFormat) {
                    t.setCodecChain(codec);
                }
            }*/

           /* Codec encoder = (Codec) new H264Encoder();

            Codec[] codec = {encoder};
            TrackControl[] tc = videoProcessor.getTrackControls();
            for (TrackControl t : tc) {
                if (t.getFormat() instanceof VideoFormat) {
                    t.setCodecChain(codec);
                }
            }*/
            //videoProcessor.setContentDescriptor(new ContentDescriptor("video.264"));


            //start video and audio processors
            videoProcessor.realize();
            audioProcessor.realize();
            //wait till they are realized
            while(videoProcessor.getState() < 300 && audioProcessor.getState() < 300) {
                System.out.println("Video State: " + videoProcessor.getState());
                System.out.println("Audio State: " + audioProcessor.getState());
                Thread.sleep(30);
            }
            System.out.println(videoProcessor.getState());
            //get processors dataoutputs to merge
            arrayDataSource[0] = videoProcessor.getDataOutput();
            arrayDataSource[1] = audioProcessor.getDataOutput();

            videoProcessor.start();
            audioProcessor.start();

            //create merging data source
            mixedDataSource = javax.media.Manager.createMergingDataSource(arrayDataSource);
            mixedDataSource.connect();
            mixedDataSource.start();
            //init final processor to create merged file
            ProcessorModel processorModel = new ProcessorModel(mixedDataSource, outputFormat, outputType);
            processor = Manager.createRealizedProcessor(processorModel);
            processor.addControllerListener(this);
            processor.configure();

            //wait till configured
            while(processor.getState() < 180) {
                Thread.sleep(20);
            }

            processor.setContentDescriptor(new ContentDescriptor(FileTypeDescriptor.QUICKTIME));

            TrackControl tcs[] = processor.getTrackControls();
            Format f[] = tcs[0].getSupportedFormats();

            tcs[0].setFormat(f[0]);
            //Codec encoder = new H263Decoder()
            //tcs[0].setCodecChain();

            processor.realize();
            //wait till realized
            while(processor.getState() < 300) {
                Thread.sleep(20);
            }
            //create merged file and start writing media to it
            outputDataSource = processor.getDataOutput();
            MediaLocator outputLocator = new MediaLocator("file:/home/greg/Projects/deckmotion/merged.mp4");
            outputDataSink = Manager.createDataSink(outputDataSource, outputLocator);
            outputDataSink.open();
            outputDataSink.addDataSinkListener(this);
            outputDataSink.start();
            processor.start();

            while(processor.getState() < 500) {
                Thread.sleep(100);
            }
            //wait until writing is done
            waitForFileDone();
            //dispose processor and datasink
            //outputDataSink.stop();
            //processor.stop();

            outputDataSink.close();
            //processor.close();
            processor.removeControllerListener(this);

        } catch (NoDataSourceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IncompatibleSourceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoDataSinkException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoProcessorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CannotRealizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    public void convertH264() throws ResourceUnavailableException {

    }

    public void convertToYUL(MediaLocator oml) {

        //start with a datasource
        DataSource videoDataSource = null; //your video file
        try {
            videoDataSource = Manager.createDataSource(oml.getURL());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoDataSourceException e) {
            e.printStackTrace();
        }
        //then a datasource
        Processor videoProcessor = null;
        try {
            videoProcessor = Manager.createProcessor(videoDataSource);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoProcessorException e) {
            e.printStackTrace();
        }
        //do the whole configure BS routine - FYI this is pure narcism (read:unneccessary)
        videoProcessor.configure();

        //wait till configured
        while(videoProcessor.getState() < 180) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Codec jencoder  = (Codec) new JavaRGBToYUV();
            Codec[] codec = {jencoder};
            TrackControl[] tc = videoProcessor.getTrackControls();
            for (TrackControl t : tc) {
                if (t.getFormat() instanceof VideoFormat) {
                    try {
                        t.setCodecChain(codec);
                    } catch (UnsupportedPlugInException e) {
                        e.printStackTrace();
                    }
                }
        }

        videoProcessor.realize();
        videoProcessor.start();
        DataSource outputDataSource = videoProcessor.getDataOutput();
        MediaLocator outputLocator = new MediaLocator("file:/home/greg/Projects/deckmotion/merged2.mp4");
        DataSink outputDataSink = null;
        try {
            outputDataSink = Manager.createDataSink(outputDataSource, outputLocator);
        } catch (NoDataSinkException e) {
            e.printStackTrace();
        }
        try {
            outputDataSink.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        outputDataSink.addDataSinkListener(this);
        try {
            outputDataSink.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Create the DataSink.
     */
    DataSink createDataSink(Processor p, MediaLocator outML) {

        DataSource ds;

        if ((ds = p.getDataOutput()) == null) {
            System.err
                    .println("Something is really wrong: the processor does not have an output DataSource");
            return null;
        }

        DataSink dsink;

        try {
            //System.err.println("- create DataSink for: " + outML);
            dsink = Manager.createDataSink(ds, outML);
            dsink.open();
        } catch (Exception e) {
            System.err.println("Cannot create the DataSink: " + e);
            return null;
        }

        return dsink;
    }

    Object waitSync = new Object();
    boolean stateTransitionOK = true;

    /**
     * Block until the processor has transitioned to the given state. Return
     * false if the transition failed.
     */
    boolean waitForState(Processor p, int state) {
        synchronized (waitSync) {
            try {
                while (p.getState() < state && stateTransitionOK)
                    waitSync.wait();
            } catch (Exception e) {
            }
        }
        return stateTransitionOK;
    }

    /**
     * Controller Listener.
     */
    public void controllerUpdate(ControllerEvent evt) {

        if (evt instanceof ConfigureCompleteEvent
                || evt instanceof RealizeCompleteEvent
                || evt instanceof PrefetchCompleteEvent) {
            synchronized (waitSync) {
                stateTransitionOK = true;
                waitSync.notifyAll();
            }
        } else if (evt instanceof ResourceUnavailableEvent) {
            synchronized (waitSync) {
                stateTransitionOK = false;
                waitSync.notifyAll();
            }
        } else if (evt instanceof EndOfMediaEvent) {
            evt.getSourceController().stop();
            evt.getSourceController().close();
        }
    }

    Object waitFileSync = new Object();
    boolean fileDone = false;
    boolean fileSuccess = true;

    /**
     * Block until file writing is done.
     */
    boolean waitForFileDone() {
        synchronized (waitFileSync) {
            try {
                while (!fileDone)
                    waitFileSync.wait();
            } catch (Exception e) {
            }
        }
        return fileSuccess;
    }

    /**
     * Event handler for the file writer.
     */
    public void dataSinkUpdate(DataSinkEvent evt) {

        if (evt instanceof EndOfStreamEvent) {
            synchronized (waitFileSync) {
                fileDone = true;
                waitFileSync.notifyAll();
            }
        } else if (evt instanceof DataSinkErrorEvent) {
            synchronized (waitFileSync) {
                fileDone = true;
                fileSuccess = false;
                waitFileSync.notifyAll();
            }
        }
    }

    /**
     * Create a media locator from the given string.
     */
    static MediaLocator createMediaLocator(String url) {

        MediaLocator ml;

        if (url.indexOf(":") > 0 && (ml = new MediaLocator(url)) != null)
            return ml;

        if (url.startsWith(File.separator)) {
            if ((ml = new MediaLocator("file:" + url)) != null)
                return ml;
        } else {
            String file = "file:" + System.getProperty("user.dir")
                    + File.separator + url;
            if ((ml = new MediaLocator(file)) != null)
                return ml;
        }

        return null;
    }

    // /////////////////////////////////////////////
    //
    // Inner classes.
    // /////////////////////////////////////////////

    /**
     * A DataSource to read from a list of JPEG image files and turn that into a
     * stream of JMF buffers. The DataSource is not seekable or positionable.
     */
    class ImageDataSource extends PullBufferDataSource {

        ImageSourceStream streams[];

        ImageDataSource(int width, int height, int frameRate, Vector images) {
            streams = new ImageSourceStream[1];
            streams[0] = new ImageSourceStream(width, height, frameRate, images);
        }

        public void setLocator(MediaLocator source) {
        }

        public MediaLocator getLocator() {
            return null;
        }

        /**
         * Content type is of RAW since we are sending buffers of video frames
         * without a container format.
         */
        public String getContentType() {
            return ContentDescriptor.RAW;
        }

        public void connect() {
        }

        public void disconnect() {
        }

        public void start() {
        }

        public void stop() {
        }

        /**
         * Return the ImageSourceStreams.
         */
        public PullBufferStream[] getStreams() {
            return streams;
        }

        /**
         * We could have derived the duration from the number of frames and
         * frame rate. But for the purpose of this program, it's not necessary.
         */
        public Time getDuration() {
            return DURATION_UNKNOWN;
        }

        public Object[] getControls() {
            return new Object[0];
        }

        public Object getControl(String type) {
            return null;
        }
    }

    /**
     * The source stream to go along with ImageDataSource.
     */
    class ImageSourceStream implements PullBufferStream {

        Vector images;
        int width, height;
        VideoFormat format;

        int nextImage = 0; // index of the next image to be read.
        boolean ended = false;

        public ImageSourceStream(int width, int height, int frameRate,
                                 Vector images) {
            this.width = width;
            this.height = height;
            this.images = images;

            format = new VideoFormat(inputformat, new Dimension(width,
                    height), Format.NOT_SPECIFIED, Format.byteArray,
                                (float) frameRate);

            format = new RGBFormat();

            format = new YUVFormat(YUVFormat.YUV_420);

            format  = new JPEGFormat(new Dimension(width, height),
                    Format.NOT_SPECIFIED,
                    Format.byteArray,
                    (float)frameRate,
                    75,
                    JPEGFormat.DEC_420);

            format = new VideoFormat(VideoFormat.CINEPAK, new Dimension(width,
                    height), Format.NOT_SPECIFIED, Format.byteArray,
                    (float) frameRate);


            //format = new YUVFormat(new Dimension(width, height), Format.NOT_SPECIFIED, Format.byteArray, (float) frameRate, YUVFormat.YUV_420, 0,0,0,0,0);
        }

        /**
         * We should never need to block assuming data are read from files.
         */
        public boolean willReadBlock() {
            return false;
        }

        /**
         * This is called from the Processor to read a frame worth of video
         * data.
         */
        public void read(Buffer buf) throws IOException {

            // Check if we've finished all the frames.
            if (nextImage >= images.size()) {
                // We are done. Set EndOfMedia.
                //System.err.println("Done reading all images.");
                buf.setEOM(true);
                buf.setOffset(0);
                buf.setLength(0);
                ended = true;
                return;
            }

            String imageFile = (String) images.elementAt(nextImage);
            nextImage++;

            //System.err.println("  - reading image file: " + imageFile);

            // Open a random access file for the next image.
            RandomAccessFile raFile;
            raFile = new RandomAccessFile(imageFile, "r");

            byte data[] = null;

            // Check the input buffer type & size.

            if (buf.getData() instanceof byte[])
                data = (byte[]) buf.getData();

            // Check to see the given buffer is big enough for the frame.
            if (data == null || data.length < raFile.length()) {
                data = new byte[(int) raFile.length()];
                buf.setData(data);
            }

            // Read the entire JPEG image from the file.
            raFile.readFully(data, 0, (int) raFile.length());

            //System.err.println("    read " + raFile.length() + " bytes.");

            buf.setOffset(0);
            buf.setLength((int) raFile.length());
            buf.setFormat(format);
            buf.setFlags(buf.getFlags() | buf.FLAG_KEY_FRAME);

            // Close the random access file.
            raFile.close();
        }

        /**
         * Return the format of each video frame. That will be JPEG.
         */
        public Format getFormat() {
            return format;
        }

        public ContentDescriptor getContentDescriptor() {
            return new ContentDescriptor(ContentDescriptor.RAW);
        }

        public long getContentLength() {
            return 0;
        }

        public boolean endOfStream() {
            return ended;
        }

        public Object[] getControls() {
            return new Object[0];
        }

        public Object getControl(String type) {
            return null;
        }
    }
}