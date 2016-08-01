package com.deckmotion;

import org.jcodec.api.awt.SequenceEncoder;
import org.jcodec.common.FileChannelWrapper;
import org.jcodec.common.NIOUtils;
import org.jcodec.containers.mp4.Brand;
import org.jcodec.containers.mp4.TrackType;
import org.jcodec.containers.mp4.muxer.MP4Muxer;

import javax.imageio.ImageIO;
import javax.media.MediaLocator;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Vector;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException {
 /*       imagesaver ims = new imagesaver();
        Vector<String> imgLst = ims.extractimages("/home/greg/Projects/deckmotion/slideshow.pptx");
        SequenceEncoder enc = new SequenceEncoder(new File("/home/greg/Projects/deckmotion/jcodec.mp4"));
        for(String img : imgLst) {
            BufferedImage image = ImageIO.read(new FileInputStream(img));
            enc.encodeImage(image);
        }
        enc.finish();
        FileChannelWrapper ch = NIOUtils.writableFileChannel(new File("/home/greg/Projects/deckmotion/jcodec2.mp4"));
        MP4Muxer mm = new MP4Muxer(ch, Brand.MP4);

        mm.addPCMAudioTrack(TrackType.SOUND)
        */
        /*
        System.out.println( "Hello World!" );
        imagesaver ims = new imagesaver();
        Vector<String> imgLst = ims.extractimages("/home/greg/Projects/deckmotion/slideshow.pptx");

        JpegImagesToMovie imageToMovie = new JpegImagesToMovie();
        MediaLocator oml;
        if ((oml = imageToMovie.createMediaLocator("/home/greg/Projects/deckmotion/vid.mov")) == null) {
            System.err.println("Cannot build media locator from:");
            System.exit(0);
        }
       int interval = 50;
        imageToMovie.doIt(640, 480, 20, imgLst, oml);

        System.out.println("Beginning merge");
        JpegImagesToMovie imageToMovie2 = new JpegImagesToMovie();
        imageToMovie2.mergeFiles(oml, new File("/home/greg/Projects/deckmotion/slideshow.wav"));
        System.out.println("Finished merge");
*/
        JpegImagesToMovie imageToMovie3 = new JpegImagesToMovie();
        imageToMovie3.convertToYUL(imageToMovie3.createMediaLocator("file:/home/greg/Projects/deckmotion/merged.mp4"));

        //JpegImagesToMovie imageToMovie4 = new JpegImagesToMovie();
        //mageToMovie4.convertToH264("mp4");


        System.out.println( "Hello World! done" );

    }
}
