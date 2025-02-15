package com.serezk4.snp.telegram.util;

import lombok.SneakyThrows;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;

import java.io.*;
import java.time.LocalDate;

public class DocumentUtil {

    @SneakyThrows
    public static void generateDocument(
            final String fullName,
            final String birthDate,
            final String gender,
            final String photoPath,
            final String outputPath
    ) {
        XWPFDocument document = new XWPFDocument();

        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = title.createRun();
        titleRun.setText("ДОГОВОР КОНФИДЕНЦИАЛЬНОСТИ");
        titleRun.setBold(true);
        titleRun.setFontSize(18);
        titleRun.setFontFamily("Times New Roman");

        addParagraph(document, "\nНастоящий договор заключен между сторонами:", true);
        addParagraph(document, "ФИО: ".concat(fullName));
        addParagraph(document, "Дата рождения: ".concat(birthDate));
        addParagraph(document, "Пол: ".concat(gender));

        addParagraph(document, "\nСТОРОНЫ ДОГОВОРИЛИСЬ О СЛЕДУЮЩЕМ:\n", true);
        addParagraph(document,
                "1. Конфиденциальная информация включает в себя все сведения, передаваемые сторонами.");
        addParagraph(document,
                "2. Стороны обязуются не разглашать полученные данные третьим лицам.");
        addParagraph(document,
                "3. Нарушение условий договора может повлечь за собой юридическую ответственность.");
        addParagraph(document,
                "4. Настоящий договор вступает в силу с момента подписания.");

        if (photoPath != null && !photoPath.isEmpty()) {
            File imageFile = new File(photoPath);
            if (imageFile.exists()) {
                FileInputStream fis = new FileInputStream(imageFile);
                XWPFParagraph imgParagraph = document.createParagraph();
                imgParagraph.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun imgRun = imgParagraph.createRun();
                imgRun.addBreak();
                imgRun.setText("Фото подписанта:");
                imgRun.addBreak();
                imgRun.addPicture(fis, XWPFDocument.PICTURE_TYPE_JPEG, photoPath,
                        Units.toEMU(150), Units.toEMU(150));
                fis.close();
            } else {
                addParagraph(document, "\nФото: [Файл не найден]");
            }
        }

        addParagraph(document, "\n\n__________________________");
        addParagraph(document, "Подпись: ".concat(fullName));
        addParagraph(document, "Дата подписания: ".concat(LocalDate.now().toString()));

        FileOutputStream fos = new FileOutputStream(outputPath);
        document.write(fos);
        fos.close();
        document.close();
    }

    private static void addParagraph(XWPFDocument document, String text) {
        addParagraph(document, text, false);
    }

    private static void addParagraph(XWPFDocument document, String text, boolean bold) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingAfter(200);
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontFamily("Times New Roman");
        run.setFontSize(12);
        if (bold) {
            run.setBold(true);
        }
    }
}
