package bytechats;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class HighQualityImagePanel extends JPanel {
    private BufferedImage originalImage;
    private BufferedImage processedImage;
    
    // --- Configuration for Effects ---
    private float blurRadius = 1.5f;
    private float darkeningFactor = 0.6f; // Increased to make the image darker
    private float overallOpacity = 1.0f; // Set to 1.0f to prevent background bleed-through
    // --- End Configuration ---

    public HighQualityImagePanel() {
        // ... (rest of the constructor code remains the same)
        try {
            java.net.URL imageUrl = getClass().getResource("/media/bg1.jpg");
            if (imageUrl != null) {
                originalImage = ImageIO.read(imageUrl);
                processImage();
            } else {
                System.err.println("Background image not found!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ... (rest of the processImage, paintComponent, and applyBlur methods)
    private void processImage() {
        if (originalImage == null) {
            processedImage = null;
            return;
        }

        BufferedImage currentImage = originalImage;

        // 1. Apply Blur
        if (blurRadius > 0.0f) {
            currentImage = applyBlur(currentImage, blurRadius);
        }

        // 2. Apply Darkening
        if (darkeningFactor > 0.0f) {
            BufferedImage dimmedImage = new BufferedImage(
                currentImage.getWidth(), currentImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = dimmedImage.createGraphics();
            
            g2d.drawImage(currentImage, 0, 0, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, darkeningFactor));
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, dimmedImage.getWidth(), dimmedImage.getHeight());
            g2d.dispose();
            currentImage = dimmedImage;
        }
        
        // 3. Apply Overall Opacity
        if (overallOpacity < 1.0f) {
             BufferedImage translucentImage = new BufferedImage(
                currentImage.getWidth(), currentImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = translucentImage.createGraphics();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, overallOpacity));
            g2d.drawImage(currentImage, 0, 0, null);
            g2d.dispose();
            currentImage = translucentImage;
        }
        processedImage = currentImage;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (processedImage != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(processedImage, 0, 0, getWidth(), getHeight(), this);
            g2d.dispose();
        }
    }
    
    // ... (rest of the applyBlur and setter methods)
    private BufferedImage applyBlur(BufferedImage sourceImage, float blurRadius) {
        if (sourceImage == null || blurRadius <= 0.0f) return sourceImage;
        int radius = (int) Math.max(1, blurRadius);
        int size = radius * 2 + 1;
        float[] data = new float[size * size];
        float sigma = blurRadius / 3.0f;
        float sum = 0.0f;
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                float value = (float) (Math.exp(-(x * x + y * y) / (2 * sigma * sigma)) / (2 * Math.PI * sigma * sigma));
                data[(y + radius) * size + (x + radius)] = value;
                sum += value;
            }
        }
        for (int i = 0; i < data.length; i++) {
            data[i] /= sum;
        }
        Kernel kernel = new Kernel(size, size, data);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        BufferedImage dest = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        return op.filter(sourceImage, dest);
    }
    
    public void setBlurRadius(float blurRadius) {
        this.blurRadius = blurRadius;
        processImage();
        repaint();
    }
    public void setDarkeningFactor(float darkeningFactor) {
        this.darkeningFactor = darkeningFactor;
        processImage();
        repaint();
    }
    public void setOverallOpacity(float overallOpacity) {
        this.overallOpacity = overallOpacity;
        processImage();
        repaint();
    }
}