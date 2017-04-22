package com.passhojao;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

public class Controller {
    @FXML
    private TextField imagePath;
    @FXML
    private TextField fileSize;
    @FXML
    private TextField done;

    private HostServices hostServices;


    @FXML
    protected void onImagePathClick(MouseEvent event) {

        done.setVisible(false);

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter jpegExtentionFilter = new FileChooser.ExtensionFilter("Jpeg Images (.jpeg, .jpg)", "*.jpeg", "*.jpg");
        fileChooser.getExtensionFilters().add(jpegExtentionFilter);
        File image = fileChooser.showOpenDialog(new Stage());
        if (image != null)
            imagePath.setText(image.toString());
    }

    @FXML
    protected void onCompressClick(MouseEvent event) throws IOException {

        if (imagePath.getText().isEmpty() || fileSize.getText().isEmpty()) {
            done.setVisible(true);
            done.setText("Choose Image and set Max Size.");
            return;
        }

        String srcImg = imagePath.getText();
        int dotpos = srcImg.lastIndexOf(".");
        String extension = srcImg.substring(dotpos);
        String destImg = srcImg.substring(0, dotpos) + "_compressed" + extension;
        System.out.println(extension);
        reduceImageQuality(imagePath.getText(), destImg, Integer.parseInt(fileSize.getText()));
    }

    private void reduceImageQuality(String srcImg, String destImg, int sizeThreshold) throws IOException {
        float quality = 1.0f;

        File file = new File(srcImg);

        long fileSize = file.length();

        if (fileSize / 1024 <= sizeThreshold) {
            done.setText("Image file size is under threshold");
            done.setVisible(true);
            return;
        }

        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");

        ImageWriter writer = iter.next();

        ImageWriteParam params = writer.getDefaultWriteParam();

        params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

        FileInputStream inputStream = new FileInputStream(file);

        BufferedImage originalImage = ImageIO.read(inputStream);
        IIOImage image = new IIOImage(originalImage, null, null);

        double percent = 0.1f;

        while (fileSize / 1024 > sizeThreshold) {
            if (percent >= quality) {
                percent = percent * 0.1f;
            }

            quality -= percent;

            File fileOut = new File(destImg);
            if (fileOut.exists()) {
                fileOut.delete();
            }

            FileImageOutputStream output = new FileImageOutputStream(fileOut);

            writer.setOutput(output);

            params.setCompressionQuality(quality);

            writer.write(null, image, params);

            File fileOut2 = new File(destImg);
            long newFileSize = fileOut2.length();
            if (newFileSize == fileSize) {
                // cannot reduce more, return
                break;
            } else {
                fileSize = newFileSize;
            }

            System.out.println("Quality = " + quality + ", New file size = " + fileSize / 1024 + "KB");
            output.close();
        }

        writer.dispose();
        done.setVisible(true);
        done.setText("DONE!");

    }

    protected void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    private HostServices getHostServices() {
        return hostServices;
    }

    @FXML
    protected void openFollowMeLink() {
        getHostServices().showDocument("https://twitter.com/hamidInventions");
    }
}
