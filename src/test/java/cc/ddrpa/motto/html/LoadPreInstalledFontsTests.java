package cc.ddrpa.motto.html;

import com.lowagie.text.DocumentException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadPreInstalledFontsTests {

    private static final Logger logger = LoggerFactory.getLogger(LoadPreInstalledFontsTests.class);

    @Test
    void generateFontBookTest() throws IOException {
        DocumentBuilder.loadPreinstalledFontsAsCJKFont();
        // get all flying saucer builtin CJK fonts
        logger.info("All loaded fonts:");
        DocumentBuilder.listFontFamily().forEach(logger::info);
        DocumentBuilder builder = new DocumentBuilder();
        builder.loadTemplate("cjk-fonts.html");
        builder.merge(Map.of(
            "font_families",
            DocumentBuilder.listFontFamily()
                .stream()
                .filter(fontFamily -> !fontFamily.endsWith("-V"))
                .sorted()
                .toList()));
        try (FileOutputStream fileOutputStream = new FileOutputStream(
            "target/font-book-sample.pdf")) {
            builder.save(fileOutputStream);
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
        }
    }
}