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

    /**
     * HTML을 PNG 바이트로 변환.
     * @param html            렌더링할 HTML
     * @param targetWidthPx   결과 이미지 가로 픽셀 (예: 600)
     */
    public byte[] htmlToPng(String html, int targetWidthPx) {
        try {
            // 1) HTML -> PDF (메모리)
            ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            // baseURI가 있으면 relative URL 리소스도 불러올 수 있음. (여기선 null)
            builder.withHtmlContent(html, null);
            builder.toStream(pdfOut);
            builder.run();
            byte[] pdfBytes = pdfOut.toByteArray();

            // 2) PDF -> PNG (첫 페이지만)
            try (PDDocument doc = PDDocument.load(pdfBytes)) {
                PDFRenderer renderer = new PDFRenderer(doc);
                float widthPt = doc.getPage(0).getMediaBox().getWidth(); // PDF 포인트(1/72 inch)
                // 원하는 픽셀 폭에 맞추는 DPI 계산: 픽셀 = (pt/72)*dpi => dpi = 픽셀*72/pt
                float dpi = (targetWidthPx * 72f) / widthPt;

                BufferedImage img = renderer.renderImageWithDPI(0, dpi, ImageType.RGB);
                ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
                ImageIO.write(img, "png", pngOut);
                return pngOut.toByteArray();
            }
        } catch (Exception e) {
            log.warn("htmlToPng failed: {}", e.toString());
            // 실패 시 안전하게 빈 PNG라도 주고 싶다면 여기서 대체 생성 가능
            return ("PNG_ERROR:" + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }
}
