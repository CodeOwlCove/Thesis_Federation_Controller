package thesis.rommler.federation_controller.api.service;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import thesis.rommler.federation_controller.api.controller.Orchestrator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import static java.lang.Thread.sleep;

@Service
public class FileSenderService {

    private static final Logger logger = LoggerFactory.getLogger(FileSenderService.class);

    private static final String FILE_PATH = "src/main/resources/Outgoing/Outgoing.zip";

    public FileSenderService(){

    }

    public void SendAllFilesToFrontend(HttpServletResponse response) throws IOException {
        File downloadFile = new File(FILE_PATH);

        // Send file to frontend
        byte[] isr = Files.readAllBytes(downloadFile.toPath());
        ByteArrayOutputStream out = new ByteArrayOutputStream(isr.length);
        out.write(isr, 0, isr.length);

        response.setContentType("application/rar");
        // Use 'inline' for preview and 'attachement' for download in browser.
        response.addHeader("Content-Disposition", "inline; filename=" + "Outgoing.rar");

        OutputStream os;
        try {
            os = response.getOutputStream();
            out.writeTo(os);
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
