package com.xm.api_mini.drawable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * 图片工具类
 *
 * @author qbhy
 */
@Component
public class ResourceUtils {

    /**
     * 获取图片
     * @param url
     * @return BufferedImage
     * @throws IOException
     */
    public static BufferedImage getImage(String url) throws IOException {
        if (url.contains("://")) {
            return getImageFromUrl(url);
        }
        File imgFile = org.springframework.util.ResourceUtils.getFile("classpath:img/"+url);
        if (imgFile != null) {
            return ImageIO.read(imgFile);
        }
        throw new IOException("Can't get input stream from URL!");
    }

    /**
     * 通过 URL 获取图片并缓存到本地文件夹中
     * @param url
     * @return BufferedImage
     * @throws IOException
     */
    public static BufferedImage getImageFromUrl(String url) throws IOException {
        BufferedImage image = ImageIO.read(new URL(url));
        System.out.println(image);
        return image;
    }

    /**
     * 图片设置圆角
     *
     * @param srcImage
     * @param radius
     * @param border
     * @param padding
     *
     * @return
     */
    public static BufferedImage setRadius(BufferedImage srcImage, int radius, int border, int padding) {
        int width = srcImage.getWidth();
        int height = srcImage.getHeight();
        int canvasWidth = width + padding * 2;
        int canvasHeight = height + padding * 2;

        BufferedImage image = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gs = image.createGraphics();
        gs.setComposite(AlphaComposite.Src);
        gs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gs.setColor(Color.WHITE);
        gs.fill(new RoundRectangle2D.Float(0, 0, canvasWidth, canvasHeight, radius, radius));
        gs.setComposite(AlphaComposite.SrcAtop);
        gs.drawImage(setClip(srcImage, radius), padding, padding, null);
        if (border != 0) {
            gs.setColor(Color.GRAY);
            gs.setStroke(new BasicStroke(border));
            gs.drawRoundRect(padding, padding, canvasWidth - 2 * padding, canvasHeight - 2 * padding, radius, radius);
        }
        gs.dispose();
        return image;
    }

    public static File getFontFile(String font) throws IOException {
        return org.springframework.util.ResourceUtils.getFile("classpath:ttf/"+font+".ttf");
    }

    /**
     * 图片设置圆角
     * @param srcImage
     * @return BufferedImage
     */
    public static BufferedImage setRadius(BufferedImage srcImage) {
        int radius = (srcImage.getWidth() + srcImage.getHeight()) / 6;
        return setRadius(srcImage, radius, 2, 5);
    }

    /**
     * 图片切圆角
     *
     * @param srcImage
     * @param radius
     *
     * @return BufferedImage
     */
    public static BufferedImage setClip(BufferedImage srcImage, int radius) {
        int width = srcImage.getWidth();
        int height = srcImage.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gs = image.createGraphics();

        gs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gs.setClip(new RoundRectangle2D.Double(0, 0, width, height, radius, radius));
        gs.drawImage(srcImage, 0, 0, null);
        gs.dispose();
        return image;
    }

    /**
     * 获取资源路径
     *
     * @param resourceName
     *
     * @return String
     *
     * @throws IOException
     */
    public static String getResourcePath(String resourceName) throws IOException {
        URL url = Drawable.class.getClassLoader().getResource(resourceName);
        if (url != null) {
            return url.getPath();
        }

        throw new IOException(resourceName + " resource not found!");
    }
}
