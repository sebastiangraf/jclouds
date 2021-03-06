/**
 * 
 */
package org.jclouds.imagestore;

import org.jclouds.imagestore.imagehoster.IImageHost;
import org.jclouds.imagestore.imagehoster.flickr.ImageHostFlickr;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class DeleteAllImageSets {

    public static void main(String[] args) {
        IImageHost hoster = new ImageHostFlickr();
        for (int i = 2; i <= 256; i = i * 2) {
            for (int j = 1; j <= 21; j++) {
                for (int k = 0; k <= 52; k++) {
                    final StringBuilder builder = new StringBuilder("benchContainer:Layered");
                    builder.append(i);
                    builder.append(":");
                    builder.append(j);
                    builder.append(":");
                    builder.append(k);
                    String name = builder.toString();
                    System.out.println("Deleting set " + name);
                    System.out.println(hoster.deleteImageSet(builder.toString()));
                    System.out.println("---------");
                }
            }
        }

    }

}
