package com.deckmotion;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import com.bric.image.transition.BlendTransition2D;
import com.bric.image.transition.FlurryTransition2D;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;


/**
 * Created by greg on 7/10/16.
 */
public class imagesaver {
    public Vector<String> extractimages(String filename) throws IOException {
        Vector<String> result = new Vector<String>();
        FileInputStream is = new FileInputStream(filename);
        XMLSlideShow ppt = new XMLSlideShow(is);
        is.close();

        Dimension pgsize = ppt.getPageSize();

        int idx = 1;
        BufferedImage blast = null;
        for (XSLFSlide slide : ppt.getSlides()) {

            BufferedImage img = new BufferedImage(pgsize.width, pgsize.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = img.createGraphics();
            // clear the drawing area
            graphics.setPaint(Color.white);
            graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));

            // render
            slide.draw(graphics);

            // save the output
            String newFile;

            if (blast != null) {
                //BlendTransition2D trns = new BlendTransition2D();
                FlurryTransition2D trns = new FlurryTransition2D();
                for (int ii=0;ii<10;ii++) {
                    BufferedImage img2 = new BufferedImage(pgsize.width, pgsize.height, BufferedImage.TYPE_INT_RGB);
                    Graphics2D graphics2 = img2.createGraphics();
                    // clear the drawing area
                    graphics2.setPaint(Color.white);
                    graphics2.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));
                    float igf = ii/10f;
                    trns.paint(graphics2, blast, img, igf);
                    String newFile2 = "slide-" + idx + ".jpeg";
                    FileOutputStream out2 = new FileOutputStream(newFile2);
                    javax.imageio.ImageIO.write(img2, "jpeg", out2);
                    out2.close();
                    result.add(newFile2);
                    idx++;
                }
            }
            for (int oo=0;oo<50;oo++) {
                newFile = "slide-" + idx + ".jpeg";
                FileOutputStream out = new FileOutputStream(newFile);
                javax.imageio.ImageIO.write(img, "jpeg", out);
                out.close();
                result.add(newFile);
                idx++;
            }

            blast = img;
            idx++;
        }
        return result;
    }
}
