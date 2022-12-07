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


