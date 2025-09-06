package com.example.mailtrend.MailSend.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
@Slf4j
@Service
public class HtmlSnapshotService {

    public byte[] htmlToPng(String html, int targetWidthPx) {
        try {
            byte[] pdfBytes = renderPdf(html, /*baseUri*/ null); // 2)에서 설명
            return pdfToSinglePng(pdfBytes, targetWidthPx);
        } catch (Exception e) {
            log.error("htmlToPng failed", e);
            // ✅ 항상 유효한 PNG를 리턴 (플레이스홀더 이미지)
            return placeholderPng(targetWidthPx, 200,
                    "SNAPSHOT FAILED",
                    (e.getMessage() != null ? e.getMessage() : "unknown"));
        }
    }

    private byte[] renderPdf(String html, String baseUri) throws Exception {
        try (var pdfOut = new ByteArrayOutputStream()) {
            var builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
            builder.useFastMode();

            // (선택) 외부 리소스 상대경로가 있다면 baseUri 지정
            builder.withHtmlContent(html, baseUri);

            // ✅ 한글 폰트 등록 (resources/fonts에 추가해두고 빌드에 포함)
            // NotoSansKR 예시 (Regular/Bold)
            builder.useFont(
                    () -> getClass().getResourceAsStream("/fonts/NotoSansKR-Regular.ttf"),
                    "NotoSansKR", 400, com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle.NORMAL, true);
            builder.useFont(
                    () -> getClass().getResourceAsStream("/fonts/NotoSansKR-Bold.ttf"),
                    "NotoSansKR", 700, com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle.NORMAL, true);

            // (선택) 페이지 사이즈를 컨테이너 폭에 맞게
            // builder.useDefaultPageSize(600 / 96f, 11.69f, BaseRendererBuilder.PageSizeUnits.INCHES);

            builder.toStream(pdfOut);
            builder.run();
            return pdfOut.toByteArray();
        }
    }

    private byte[] pdfToSinglePng(byte[] pdfBytes, int targetWidthPx) throws Exception {
        try (var doc = org.apache.pdfbox.pdmodel.PDDocument.load(pdfBytes)) {
            var renderer = new org.apache.pdfbox.rendering.PDFRenderer(doc);

            // 여러 페이지면 전부 렌더해서 세로로 이어붙임 (메일 스냅샷은 길어질 수 있음)
            int pageCount = doc.getNumberOfPages();
            java.util.List<BufferedImage> pages = new java.util.ArrayList<>(pageCount);

            for (int i = 0; i < pageCount; i++) {
                float widthPt = doc.getPage(i).getMediaBox().getWidth();
                float dpi = (targetWidthPx * 72f) / widthPt; // pt(1/72in) → 픽셀폭 맞춤
                pages.add(renderer.renderImageWithDPI(i, dpi, org.apache.pdfbox.rendering.ImageType.RGB));
            }

            BufferedImage merged = verticalConcat(pages);
            try (var out = new ByteArrayOutputStream()) {
                javax.imageio.ImageIO.write(merged, "png", out);
                return out.toByteArray();
            }
        }
    }

    private BufferedImage verticalConcat(java.util.List<BufferedImage> imgs) {
        int w = imgs.stream().mapToInt(BufferedImage::getWidth).max().orElse(1);
        int h = imgs.stream().mapToInt(BufferedImage::getHeight).sum();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        var g = result.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, w, h);

        int y = 0;
        for (BufferedImage img : imgs) {
            g.drawImage(img, 0, y, null);
            y += img.getHeight();
        }
        g.dispose();
        return result;
    }

    private byte[] placeholderPng(int w, int h, String title, String detail) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        var g = img.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setColor(java.awt.Color.RED);
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18));
        g.drawString(title, 12, 32);
        g.setColor(java.awt.Color.DARK_GRAY);
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
        // 긴 메세지는 줄바꿈
        var t = wrap(detail, 60);
        int y = 60;
        for (String line : t.split("\n")) {
            g.drawString(line, 12, y);
            y += 16;
        }
        g.dispose();
        try (var out = new ByteArrayOutputStream()) {
            javax.imageio.ImageIO.write(img, "png", out);
            return out.toByteArray();
        } catch (Exception ex) {
            // 이 경우도 거의 없지만, 최소한 유효한 1x1 PNG 리턴
            return new byte[]{
                    (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                    0, 0, 0, 0x0D, 0x49, 0x48, 0x44, 0x52,
                    0, 0, 0, 1, 0, 0, 0, 1, 8, 2, 0, 0, 0,
                    (byte)0x90, 0x77, 0x53, (byte)0xDE, 0x00, 0, 0, 0, 0x0A,
                    0x49, 0x44, 0x41, 0x54, 0x08, (byte)0xD7, 0x63, 0x60, 0, 0, 0, 0x02,
                    0, 0x01, (byte)0xE2, 0x21, (byte)0xBC, 0x33, 0, 0, 0, 0,
                    0x49, 0x45, 0x4E, 0x44, (byte)0xAE, 0x42, 0x60, (byte)0x82
            };

        }
    }

    private String wrap(String s, int width) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        int col = 0;
        for (char c : s.toCharArray()) {
            sb.append(c);
            if (++col >= width && Character.isWhitespace(c)) {
                sb.append('\n');
                col = 0;
            }
        }
        return sb.toString();
    }
}
