package com.deckmotion;

import javax.media.MediaLocator;
import java.io.File;
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

        System.out.println( "Hello World! done" );
    }
}
