package User;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class test {
/*
    static LocalDateTime t = LocalDateTime.now();
    static LocalDateTime l1 = t.plusDays(1);
    static LocalDate l2 = t.plusDays(100);
    static LocalDate l3 = t.plusDays(6000);
    static LocalDate l0 = t.minusDays(10000);

    static byte[] tb = t.toString().getBytes(StandardCharsets.UTF_8);

    static byte[] b = t.toString().getBytes(StandardCharsets.UTF_8);
    static byte[] b1 = l1.toString().getBytes(StandardCharsets.UTF_8);
    static byte[] b2 = l2.toString().getBytes(StandardCharsets.UTF_8);
    static byte[] b3 = l3.toString().getBytes(StandardCharsets.UTF_8);
*/
    public static void main(String[] args) throws IOException {
        byte[] signedHash;
        String array = "[B@1cccef17";
        signedHash = array.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(signedHash);
        BufferedImage someImage = ImageIO.read(bais);
        someImage.getGraphics().drawLine(1, 1, someImage.getWidth()-1, someImage.getHeight()-1);
        someImage.getGraphics().drawLine(1, someImage.getHeight()-1, someImage.getWidth()-1, 1);

        //Icon created = new ImageIcon(someImage);

    }
}


