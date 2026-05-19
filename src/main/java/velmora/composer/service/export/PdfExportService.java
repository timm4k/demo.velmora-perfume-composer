package velmora.composer.service.export;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;
import velmora.composer.model.Note;
import velmora.composer.service.analysis.AnalysisResult;

@Component
public class PdfExportService {

  public void export(File file, String formulaName, List<Note> notes, AnalysisResult result)
      throws DocumentException, IOException {
    Document doc = new Document();
    PdfWriter.getInstance(doc, new FileOutputStream(file));
    doc.open();

    Font titleF = new Font(Font.HELVETICA, 18, Font.BOLD);
    Font hF = new Font(Font.HELVETICA, 14, Font.BOLD);
    Font bF = new Font(Font.HELVETICA, 12, Font.BOLD);
    Font nF = new Font(Font.HELVETICA, 11);

    doc.add(new Paragraph("VELMORA OLFACTORY LAB", titleF));
    doc.add(new Paragraph("FORMULA REPORT", hF));
    doc.add(new Paragraph(" "));

    doc.add(new Paragraph("Formula: " + formulaName, bF));
    doc.add(new Paragraph("Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()), nF));
    doc.add(new Paragraph(" "));

    PdfPTable table = new PdfPTable(5);
    table.setWidthPercentage(100);
    float[] colWidths = {35, 14, 18, 13, 10};
    table.setWidths(colWidths);
    String[] headers = {"Note", "Type", "Category", "Int.", "%"};
    for (String h : headers) {
      PdfPCell cell = new PdfPCell(new Phrase(h, new Font(Font.HELVETICA, 9, Font.BOLD)));
      cell.setHorizontalAlignment(Element.ALIGN_CENTER);
      table.addCell(cell);
    }
    for (Note n : notes) {
      table.addCell(n.getName() != null ? n.getName() : "—");
      table.addCell(n.getType() != null ? n.getType().name() : "—");
      table.addCell(n.getCategory() != null ? n.getCategory() : "—");
      table.addCell(n.getIntensity() != null ? String.valueOf(n.getIntensity()) : "—");
      table.addCell("—");
    }
    doc.add(table);
    doc.add(new Paragraph(" "));

    doc.add(new Paragraph("PERFORMANCE", bF));
    doc.add(new Paragraph("Harmony: " + result.harmony(), nF));
    doc.add(new Paragraph("Longevity: " + result.longevityScore(), nF));
    doc.add(new Paragraph("Complexity: " + result.complexityScore(), nF));
    doc.add(new Paragraph("Projection: " + result.projectionScore(), nF));
    doc.add(new Paragraph("Overall Score: " + result.overallScore(), bF));
    doc.add(new Paragraph(" "));

    doc.add(new Paragraph("PHASE LONGEVITY", bF));
    doc.add(new Paragraph("Top Notes: " + result.topHoursDisplay(), nF));
    doc.add(new Paragraph("Heart Notes: " + result.heartHoursDisplay(), nF));
    doc.add(new Paragraph("Base Notes: " + result.baseHoursDisplay(), nF));
    doc.add(new Paragraph("Est. Total: " + result.estLongevity(), nF));
    doc.add(new Paragraph("Sillage: " + result.sillage(), nF));

    doc.close();
  }
}
