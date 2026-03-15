package lk.ijse.aad.backend.utill;

import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

/**
 * Utility for encoding uploaded image files to Base64 strings
 * suitable for storage as LONGTEXT in the database.
 *
 * Fixed: was referenced in controllers but never defined anywhere in the project.
 */
public class PicEncoder {

    private PicEncoder() {
        // Utility class — no instantiation
    }

    /**
     * Converts a MultipartFile image to a Base64-encoded data URI string.
     *
     * @param file the uploaded image file
     * @return a Base64 data URI, e.g. "data:image/jpeg;base64,/9j/4AAQ..."
     * @throws Exception if the file cannot be read
     */
    public static String generatePicture(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String mimeType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
        String base64 = Base64.getEncoder().encodeToString(file.getBytes());
        return "data:" + mimeType + ";base64," + base64;
    }
}