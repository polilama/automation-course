package courseplayw;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class FileUploadTest {

    @Test
    void testFileUploadAndDownload() {
        Playwright playwright = null;
        Browser browser = null;

        try {
            playwright = Playwright.create();
            browser = playwright.chromium().launch();
            Page page = browser.newPage();

            // 1. Генерация тестового PNG-файла в памяти (1x1 пиксель, чёрный)
            byte[] testPngBytes = generateTestPng();
            String testFileName = "test_image.png";

            // 2. Загрузка файла на сервер через multipart/form-data
            Path tempFile = Files.createTempFile("test_", ".png");
            Files.write(tempFile, testPngBytes);

            APIResponse uploadResponse = page.context().request().post(
                    "https://httpbin.org/post",
                    RequestOptions.create().setMultipart(
                            FormData.create().set("file", tempFile.toFile().toPath())
                    ));

            // Проверка статуса ответа сервера
            assertEquals(200, uploadResponse.status(),
                    "Статус ответа при загрузке файла должен быть 200");

            // 3. Проверка получения файла сервером (наличие base64-данных в ответе)
            String responseBody = uploadResponse.text();
            assertTrue(responseBody.contains("data:image/png;base64"),
                    "Ответ сервера должен содержать base64-данные PNG-файла");

            // Извлечение base64 данных из ответа сервера
            String base64Data = extractBase64FromResponse(responseBody);
            byte[] receivedBytes = Base64.getDecoder().decode(base64Data);

            // 4. Проверка точного соответствия содержимого исходного и загруженного файла
            assertArrayEquals(testPngBytes, receivedBytes,
                    "Содержимое загруженного файла должно точно соответствовать исходному");

            // 5. Скачивание эталонного PNG-файла
            APIResponse downloadResponse = page.context().request().get("https://httpbin.org/image/png");

            // Проверка статуса ответа при скачивании
            assertEquals(200, downloadResponse.status(),
                    "Статус ответа при скачивании файла должен быть 200");

            // 6. Проверка корректности MIME-типа
            String contentType = downloadResponse.headers().get("content-type");
            assertNotNull(contentType, "Заголовок Content-Type не должен быть null");
            assertTrue(contentType.contains("image/png"),
                    "MIME-тип должен содержать 'image/png', но найден: " + contentType);

            // 7. Проверка валидности формата через сигнатуру файла
            byte[] downloadedBytes = downloadResponse.body();
            validatePngSignature(downloadedBytes);

            System.out.println("Все проверки успешно пройдены!");

        } catch (Exception e) {
            fail("Тест завершился с ошибкой: " + e.getMessage());
        } finally {
            // Очистка ресурсов
            if (browser != null) {
                browser.close();
            }
            if (playwright != null) {
                playwright.close();
            }
        }
    }


    private byte[] generateTestPng() {
        return new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, // PNG сигнатура
                0x00, 0x00, 0x00, 0x0D, // Длина чанка IHDR
                0x49, 0x48, 0x44, 0x52, // Тип чанка IHDR
                0x00, 0x00, 0x00, 0x01, // Ширина: 1
                0x00, 0x00, 0x00, 0x01, // Высота: 1
                0x08, 0x02, 0x00, 0x00, 0x00, // Битовая глубина, цвет, сжатие и т. д.
                (byte) 0x90, 0x77, 0x53, (byte) 0xDE, // CRC IHDR
                0x00, 0x00, 0x00, 0x01, // Длина чанка IDAT
                0x49, 0x44, 0x41, 0x54, // Тип чанка IDAT
                0x00, (byte) 0xD7, (byte) 0xFF, (byte) 0xFF, // Сжатые данные (чёрный пиксель)
                0x60, (byte) 0x82, 0x0B, (byte) 0xE1, // CRC IDAT
                0x00, 0x00, 0x00, 0x00, // Длина чанка IEND
                0x49, 0x45, 0x4E, 0x44, // Тип чанка IEND
                (byte) 0xAE, 0x42, 0x60, (byte) 0x82  // CRC IEND
        };
    }

    private String extractBase64FromResponse(String responseBody) {
        int startIndex = responseBody.indexOf("data:image/png;base64,");
        if (startIndex == -1) {
            fail("Не удалось найти base64-данные в ответе сервера");
        }

        startIndex += "data:image/png;base64,".length();
        int endIndex = responseBody.indexOf("\"", startIndex);
        if (endIndex == -1) {
            endIndex = responseBody.indexOf("'", startIndex); // Альтернативный вариант кавычек
        }
        if (endIndex == -1) {
            // Если кавычек нет, ищем конец строки или другие ограничители
            endIndex = responseBody.indexOf(",", startIndex);
            if (endIndex == -1) {
                endIndex = responseBody.length();
            }
        }

        return responseBody.substring(startIndex, endIndex);
    }

    private void validatePngSignature(byte[] fileBytes) {
        assertNotNull(fileBytes, "Массив байтов файла не должен быть null");
        assertTrue(fileBytes.length >= 8, "Файл слишком короткий для проверки сигнатуры PNG");

        assertEquals(0x89, fileBytes[0] & 0xFF,
                "Первый байт сигнатуры PNG должен быть 0x89 (специальный байт)");
        assertEquals(0x50, fileBytes[1] & 0xFF,
                "Второй байт сигнатуры PNG должен быть 0x50 (P)");
        assertEquals(0x4E, fileBytes[2] & 0xFF,
                "Третий байт сигнатуры PNG должен быть 0x4E (N)");
        assertEquals(0x47, fileBytes[3] & 0xFF,
                "Четвёртый байт сигнатуры PNG должен быть 0x47 (G)");

        System.out.println("Сигнатура PNG-файла успешно проверена");
    }
}
