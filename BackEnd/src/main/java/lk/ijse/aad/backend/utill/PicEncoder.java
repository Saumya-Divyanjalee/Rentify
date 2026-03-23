package lk.ijse.aad.backend.utill;

import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;


public class PicEncoder {

    private PicEncoder() {
        // Utility class — no instantiation
    }


    public static String generatePicture(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String mimeType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
        String base64 = Base64.getEncoder().encodeToString(file.getBytes());
        return "data:" + mimeType + ";base64," + base64;
    }
}