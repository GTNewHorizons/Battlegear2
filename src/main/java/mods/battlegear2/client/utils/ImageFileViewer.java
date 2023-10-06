package mods.battlegear2.client.utils;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileView;

import mods.battlegear2.utils.FileExtension;

/**
 * Created by Aaron on 4/08/13.
 */
public class ImageFileViewer extends FileView {

    @Override
    public Icon getIcon(File f) {
        String extention = new FileExtension(f.getName()).get();
        if (extention != null) {

            if (extention.equalsIgnoreCase("png") || extention.equalsIgnoreCase("tiff")
                    || extention.equalsIgnoreCase("tif")
                    || extention.equalsIgnoreCase("gif")
                    || extention.equalsIgnoreCase("bmp")
                    || extention.equalsIgnoreCase("jpeg")
                    || extention.equalsIgnoreCase("jpg")) {
                try {
                    BufferedImage original = ImageIO.read(f);
                    BufferedImage resized = new BufferedImage(16, 16, original.getType());
                    Graphics2D g = resized.createGraphics();
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.drawImage(original, 0, 0, 16, 16, 0, 0, original.getWidth(), original.getHeight(), null);
                    g.dispose();

                    return new ImageIcon(resized);
                } catch (Exception e) {
                    return super.getIcon(f);
                }
            } else {
                return super.getIcon(f);
            }
        } else {
            return super.getIcon(f);
        }
    }
}
