import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final String AUTH_TITLE = "Test123"; // This name will show up in your Google Authenticator

    public static void main(String[] args) throws WriterException, InterruptedException {
        if ((args == null) || (args.length == 0)) {
            String secretKey = createAuthenticatorCode();
            System.out.println("Random generated Authenticator Secret Key :");
            System.out.println(stringPer4(secretKey.toLowerCase(Locale.ROOT)));
            //
            // Show QR code in window
            JFrame f = new JFrame("Authenticator QR code");
            f.setLayout(new FlowLayout());
            f.setSize(400, 400);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JLabel j = new JLabel();
            j.setIcon(new ImageIcon(createQRImage("otpauth://totp/" + AUTH_TITLE + "?secret=" + secretKey, 350)));
            f.add(j);
            f.setVisible(true);
            //
            String lastCode = null;
            while (true) {
                String code = GoogleAuthenticatorCode(secretKey);
                if (!code.equals(lastCode)) {
                    System.out.println(code);
                }
                lastCode = code;
                Thread.sleep(2000);
            }
        } else {
            System.out.println(GoogleAuthenticatorCode(args[0].replaceAll(" ", "")));
        }
    }

    private static String truncateHash(byte[] hash) {
        String hashString = new String(hash);
        int offset = Integer.parseInt(hashString.substring(hashString.length() - 1), 16);
        String truncatedHash = hashString.substring(offset * 2, offset * 2 + 8);
        int val = Integer.parseUnsignedInt(truncatedHash, 16) & 0x7FFFFFFF;
        String finalHash = String.valueOf(val);
        return finalHash.substring(finalHash.length() - 6);
    }

    private static byte[] hmacSha1(byte[] value, byte[] keyBytes) {
        SecretKeySpec signKey = new SecretKeySpec(keyBytes, "HmacSHA1");
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signKey);
            byte[] rawHmac = mac.doFinal(value);
            return new Hex().encode(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String GoogleAuthenticatorCode(String secret) {
        if ((secret == null) || (secret.isEmpty())) {
            throw new IllegalArgumentException("Secret key is empty or null");
        }
        long value = new Date().getTime() / TimeUnit.SECONDS.toMillis(30);
        Base32 base = new Base32();
        byte[] key = base.decode(secret);
        byte[] data = new byte[8];
        for (int i = 8; i-- > 0; value >>>= 8) {
            data[i] = (byte) value;
        }
        byte[] hash = hmacSha1(data, key);
        return truncateHash(hash);
    }

    private static String createAuthenticatorCode() {
        SecureRandom sr = new SecureRandom();
        byte[] code = new byte[20];
        sr.nextBytes(code);
        return new Base32().encodeToString(code);
    }

    private static String stringPer4(String string) {
        StringBuilder builder = new StringBuilder();
        int counter = 0;
        while (counter < string.length()) {
            builder.append(string.charAt(counter++));
            if (counter % 4 == 0) builder.append(" ");
        }
        return builder.toString();
    }

    private static BufferedImage createQRImage(String qrCodeText, int size) throws WriterException {
        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix byteMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, size, size, hintMap);
        int matrixSize = byteMatrix.getWidth();
        BufferedImage image = new BufferedImage(matrixSize, matrixSize, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, matrixSize, matrixSize);
        graphics.setColor(Color.BLACK);
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (byteMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }
        return image;
    }

}