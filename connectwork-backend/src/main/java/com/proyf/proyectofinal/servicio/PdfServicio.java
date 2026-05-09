/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.servicio;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ludwi
 */

public class PdfServicio {

    private static final DeviceRgb COLOR_PRIMARIO = new DeviceRgb(26, 26, 46);
    private static final DeviceRgb COLOR_ACENTO = new DeviceRgb(15, 52, 96);
    private static final DeviceRgb COLOR_DESTACADO = new DeviceRgb(233, 69, 96);
    private static final DeviceRgb COLOR_FILA_PAR = new DeviceRgb(240, 244, 248);
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generarReporte(String titulo, String subtitulo, List<String> encabezados, List<List<String>> filas) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);

            agregarEncabezado(doc, titulo, subtitulo);
            agregarTabla(doc, encabezados, filas);
            agregarPie(doc);

            doc.close();
        } catch (Exception e) {
            System.err.println("Error generando PDF: " + e.getMessage());
        }
        return baos.toByteArray();
    }

    private void agregarEncabezado(Document doc, String titulo, String subtitulo) {
        Paragraph headerBar = new Paragraph("ConnectWork")
                .setFontSize(10)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(COLOR_PRIMARIO)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(0)
                .setPadding(8);
        doc.add(headerBar);

        Paragraph tituloP = new Paragraph(titulo)
                .setFontSize(20)
                .setBold()
                .setFontColor(COLOR_PRIMARIO)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(16)
                .setMarginBottom(4);
        doc.add(tituloP);

        if (subtitulo != null && !subtitulo.isBlank()) {
            Paragraph subP = new Paragraph(subtitulo)
                    .setFontSize(11)
                    .setFontColor(new DeviceRgb(100, 116, 139))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(4);
            doc.add(subP);
        }

        String ahora = LocalDateTime.now().format(FORMATO);
        Paragraph fechaP = new Paragraph("Generado: " + ahora)
                .setFontSize(9)
                .setFontColor(new DeviceRgb(148, 163, 184))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        doc.add(fechaP);
    }

    private void agregarTabla(Document doc, List<String> encabezados, List<List<String>> filas) {
        if (encabezados == null || encabezados.isEmpty()) return;

        float[] anchos = new float[encabezados.size()];
        for (int i = 0; i < anchos.length; i++) anchos[i] = 1f;

        Table tabla = new Table(UnitValue.createPercentArray(anchos)).useAllAvailableWidth();

        for (String enc : encabezados) {
            Cell celda = new Cell()
                    .add(new Paragraph(enc).setFontSize(9).setBold().setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(COLOR_PRIMARIO)
                    .setPadding(8)
                    .setTextAlignment(TextAlignment.CENTER);
            tabla.addHeaderCell(celda);
        }

        for (int i = 0; i < filas.size(); i++) {
            List<String> fila = filas.get(i);
            DeviceRgb bgColor = i % 2 == 0 ? COLOR_FILA_PAR : new DeviceRgb(255, 255, 255);
            for (String valor : fila) {
                Cell celda = new Cell()
                        .add(new Paragraph(valor != null ? valor : "—").setFontSize(9))
                        .setBackgroundColor(bgColor)
                        .setPadding(7);
                tabla.addCell(celda);
            }
        }

        if (filas.isEmpty()) {
            Cell celda = new Cell(1, encabezados.size())
                    .add(new Paragraph("Sin datos para el periodo seleccionado")
                            .setFontSize(9)
                            .setFontColor(new DeviceRgb(148, 163, 184))
                            .setTextAlignment(TextAlignment.CENTER))
                    .setPadding(16);
            tabla.addCell(celda);
        }

        doc.add(tabla);
    }

    private void agregarPie(Document doc) {
        Paragraph pie = new Paragraph("ConnectWork — CUNOC — IPC2 Primer Semestre 2026")
                .setFontSize(8)
                .setFontColor(new DeviceRgb(148, 163, 184))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(24);
        doc.add(pie);
    }
}