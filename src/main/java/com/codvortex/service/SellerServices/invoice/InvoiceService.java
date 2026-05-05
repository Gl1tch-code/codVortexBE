package com.codvortex.service.SellerServices.invoice;

import com.codvortex.configuration.JwtTokenService;
import com.codvortex.domain.*;
import com.codvortex.dto.InvoiceDTO;
import com.codvortex.dtoMappers.InvoiceMapper;
import com.codvortex.repository.*;
import com.codvortex.utils.Constants;
import com.codvortex.utils.InvoiceStatus;
import com.codvortex.utils.OrderShippinStatusEnum;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceService {

    @Value("${app.base-url}")
    private String baseUrl;

    private final InvoiceRepository invoiceRepository;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final InvoiceMapper invoiceMapper;
    private final OrderRepository orderRepository;
    private final SourcingRepository sourcingRepository;
    private final FileRepository fileRepository;

    public Page<InvoiceDTO> getAllInvoices(String q, InvoiceStatus status, Pageable pageable, String token) {
        User user = userRepository.findByUsername(jwtTokenService.extractEmail(token)).orElseThrow(() -> new RuntimeException("User Not Found"));
        Page<Invoice> invoices = invoiceRepository.findByKeyword(q, user.getId(), status, pageable);
        return invoices.map(inv -> {
            InvoiceDTO invDTO = invoiceMapper.toDTO(inv);
            invDTO.setOrdersCount(inv.getSellerOrders().size());
            return invDTO;
        });
    }

    public void createInvoice(String token) {
        User user = userRepository.findByUsername(jwtTokenService.extractEmail(token)).orElseThrow(() -> new RuntimeException("User Not Found"));

        List<Order> sellerOrders = new ArrayList<>();
        List<Order> sellerOrdersFromDrop = new ArrayList<>();
        List<Order> sellerSoldProductsInDrop = new ArrayList<>();

        BigDecimal balance = BigDecimal.ZERO;

        BigDecimal minus = Constants.DELIVERY_FEES;
        BigDecimal divisor = Constants.CHANGE;

        for (Order order : orderRepository.findAllByUserId(user.getId())) {
            if (order.getShippingStatus() == OrderShippinStatusEnum.DELIVERED) {

                if (order.getUser().getId().equals(user.getId())) {
                    if (order.getProduct().getUser().getId().equals(user.getId())) {
                        sellerOrders.add(order);
                        balance = balance.add(order.getPrice()
                                .subtract(minus)
                                .divide(divisor, 2, BigDecimal.ROUND_HALF_UP));
                    } else {
                        if (!order.getIsSellerPayed()) {
                            sellerOrdersFromDrop.add(order);
                            balance = balance.add(order.getPrice()
                                    .subtract(minus)
                                    .subtract(order.getProduct().getPrice())
                                    .divide(divisor, 2, BigDecimal.ROUND_HALF_UP));
                        }
                    }
                } else if (order.getProduct().getUser().getId().equals(user.getId())) {
                    if (!order.getIsProductOwnerPayed()) {
                        sellerSoldProductsInDrop.add(order);
                        balance = balance.add(order.getProduct().getPrice()
                                .divide(divisor, 2, BigDecimal.ROUND_HALF_UP));
                    }
                }
            }
        }

        List<Sourcing> shippingNotPayedSourcing = sourcingRepository.findAllByUserIdAndIsShippingFeesPayedFalse(user.getId());

        for (Sourcing userSourcing : shippingNotPayedSourcing) {
            balance = balance.subtract(userSourcing.getShippingFees().divide(divisor, 2, BigDecimal.ROUND_HALF_UP));
        }

        if (balance.doubleValue() > 200) {
            List<Order> allSellerOrders = new ArrayList<>(sellerOrders);
            allSellerOrders.addAll(sellerOrdersFromDrop);

            List<Order> allProductOwnerOrders = new ArrayList<>(sellerOrders);
            allProductOwnerOrders.addAll(sellerSoldProductsInDrop);

            Invoice invoice = invoiceRepository.save(
                    Invoice.builder()
                            .status(InvoiceStatus.WAITING)
                            .updatedAt(LocalDateTime.now())
                            .user(user)
                            .sellerOrders(allSellerOrders)
                            .totalPrice(balance)
                            .productOwnerOrders(allProductOwnerOrders)
                            .build()
            );

            String fileUrl = baseUrl + "/files/download/" + generateInvoicePdf(invoice);
            invoice.setFileLink(fileUrl);

            sellerOrders.forEach(order -> {
                order.setSellerInvoice(invoice);
                order.setProductOwnerInvoice(invoice);
                order.setIsSellerPayed(true);
                order.setIsProductOwnerPayed(true);
            });
            sellerOrdersFromDrop.forEach(order -> {
                order.setSellerInvoice(invoice);
                order.setIsSellerPayed(true);
            });
            sellerSoldProductsInDrop.forEach(order -> {
                order.setProductOwnerInvoice(invoice);
                order.setIsProductOwnerPayed(true);
            });
        } else {
            throw new RuntimeException("You dont have enough money");
        }
    }

    public Long generateInvoicePdf(Invoice invoice) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font sectionFont = new Font(Font.HELVETICA, 14, Font.BOLD, new Color(126, 63, 242));
            Font bold = new Font(Font.HELVETICA, 12, Font.BOLD);

            // === HEADER WITH LOGO ===
            try {
                java.net.URL logoUrl = getClass().getResource("/logo.png");
                if (logoUrl != null) {
                    Image logo = Image.getInstance(logoUrl);
                    logo.scalePercent(15);
                    logo.setAlignment(Element.ALIGN_CENTER);
                    doc.add(logo);
                } else {
                    doc.add(new Paragraph("COD VORTEX", new Font(Font.HELVETICA, 18, Font.BOLD, new Color(126, 63, 242))));
                }
            } catch (Exception e) {
                doc.add(new Paragraph("COD VORTEX", new Font(Font.HELVETICA, 18, Font.BOLD, new Color(126, 63, 242))));
            }
            doc.add(new Paragraph(" ")); // spacing

            Paragraph invoiceTitle = new Paragraph("Invoice id: " + invoice.getId(),
                    new Font(Font.HELVETICA, 12, Font.BOLD, new Color(31, 9, 44))); // Purple theme
            invoiceTitle.setAlignment(Element.ALIGN_LEFT);
            doc.add(invoiceTitle);

            doc.add(new Paragraph(" ")); // spacing

            // Seller info
            doc.add(new Paragraph("Seller: " + invoice.getUser().getFullName()));

            Date updatedAtDate = Date.from(invoice.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant());
            String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(updatedAtDate);
            doc.add(new Paragraph("Date: " + formattedDate));

            doc.add(new Paragraph("Total Balance: " + invoice.getTotalPrice() + " MAD"));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph(" "));

            // === Section 1: Orders with your products ===
            doc.add(new Paragraph("Orders With Your Products", sectionFont));
            doc.add(new Paragraph(" "));
            BigDecimal totalOrdersTablePrice = addOrdersTable(doc, invoice.getSellerOrders().stream()
                    .filter(o -> o.getUser().getId().equals(invoice.getUser().getId())
                            && o.getProduct().getUser().getId().equals(invoice.getUser().getId()))
                    .toList());

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph(" "));

            // === Section 2: Orders you sold from AvailableStock ===
            doc.add(new Paragraph("Orders You Sold From AvailableStock", sectionFont));
            doc.add(new Paragraph(" "));
            BigDecimal totalAvailableStockOrdersTable = addAvailableStockOrdersTable(doc, invoice.getSellerOrders().stream()
                    .filter(o -> o.getUser().getId().equals(invoice.getUser().getId())
                            && !o.getProduct().getUser().getId().equals(invoice.getUser().getId()))
                    .toList());

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph(" "));

            // === Section 3: Other sellers sold your products ===
            doc.add(new Paragraph("Other Sellers Sold Your Products", sectionFont));
            doc.add(new Paragraph(" "));
            BigDecimal totalProductSoldInAvailableStockOrdersTable = addProductSoldInAvailableStockOrdersTable(doc, invoice.getProductOwnerOrders().stream()
                    .filter(o -> !o.getUser().getId().equals(invoice.getUser().getId())
                            && o.getProduct().getUser().getId().equals(invoice.getUser().getId()))
                    .toList());

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph(" "));

            // ==== Section 4: Shipping fees
            List<Sourcing> shippingNotPayedSourcing = sourcingRepository.findAllByUserIdAndIsShippingFeesPayedFalse(invoice.getUser().getId());
            doc.add(new Paragraph("Sourcings Shipping fees", sectionFont));
            doc.add(new Paragraph(" "));
            BigDecimal totalShippingFees = addShippingFees(doc, shippingNotPayedSourcing);

            doc.add(new Paragraph(" "));
            // Create a table with 1 row and 2 columns
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(100);
            totalTable.setWidths(new float[]{50, 50}); // Equal width columns

            // Total in CFA
            PdfPCell cfaCell = new PdfPCell(new Phrase(
                    "Total (IN CFA): " + totalOrdersTablePrice
                            .add(totalAvailableStockOrdersTable)
                            .add(totalProductSoldInAvailableStockOrdersTable)
                            .subtract(totalShippingFees) + " CFA", bold));
            cfaCell.setBorder(Rectangle.NO_BORDER);
            cfaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

            // Total in MAD
            PdfPCell madCell = new PdfPCell(new Phrase(
                    "Total (IN MAD): " + invoice.getTotalPrice() + " MAD", bold));
            madCell.setBorder(Rectangle.NO_BORDER);
            madCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

            // Add cells to table
            totalTable.addCell(cfaCell);
            totalTable.addCell(madCell);

            // Add table to document
            doc.add(totalTable);

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        } finally {
            doc.close();
        }

        // save PDF in files table
        File file = new File();
        file.setName("invoice-" + invoice.getId() + ".pdf");
        file.setContentType("application/pdf");
        file.setData(baos.toByteArray());

        return fileRepository.save(file).getId();
    }

    private BigDecimal addShippingFees(Document doc, List<Sourcing> sourcings) throws DocumentException {
        if (sourcings.isEmpty()) {
            doc.add(new Paragraph("No shipping fees for this invoice."));
            return BigDecimal.ZERO;
        }

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        addHeader(table, "Sourcing ID", "Shipping fees");

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (Sourcing sourcing : sourcings) {
            sourcing.setIsShippingFeesPayed(true);
            table.addCell(sourcing.getId().toString());
            table.addCell(sourcing.getShippingFees().toPlainString());

            totalPrice = totalPrice.add(sourcing.getShippingFees());
        }

        doc.add(table);

        return totalPrice;
    }

    private BigDecimal addOrdersTable(Document doc, List<Order> orders) throws DocumentException {
        if (orders.isEmpty()) {
            doc.add(new Paragraph("No orders in this category."));
            return BigDecimal.ZERO;
        }

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{10, 20, 10, 20, 20, 20});

        addHeader(table, "Order ID", "Product", "Qty", "Price (CFA)", "Fees", "Total (CFA)");

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (Order order : orders) {
            table.addCell(order.getId().toString());
            table.addCell(order.getProduct().getName());
            table.addCell(order.getQuantity().toString());
            table.addCell(order.getPrice().toPlainString());
            table.addCell(Constants.DELIVERY_FEES + " CFA");
            table.addCell(order.getPrice().subtract(Constants.DELIVERY_FEES).toPlainString());
            totalPrice = totalPrice.add(order.getPrice().subtract(Constants.CHANGE));
        }

        doc.add(table);

        return totalPrice;
    }

    private BigDecimal addAvailableStockOrdersTable(Document doc, List<Order> orders) throws DocumentException {
        if (orders.isEmpty()) {
            doc.add(new Paragraph("No orders in this category."));
            return BigDecimal.ZERO;
        }

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{10, 20, 10, 15, 15, 15, 15});

        addHeader(table, "Order ID", "Product", "Qty", "Price (CFA)", "Fees", "Product Price", "Total (CFA)");

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (Order order : orders) {
            table.addCell(order.getId().toString());
            table.addCell(order.getProduct().getName());
            table.addCell(order.getQuantity().toString());
            table.addCell(order.getPrice().toPlainString());
            table.addCell(Constants.DELIVERY_FEES + " CFA");
            table.addCell(order.getProduct().getPrice().toPlainString());
            table.addCell(order.getPrice().subtract(Constants.DELIVERY_FEES).subtract(order.getProduct().getPrice()).toPlainString());
            totalPrice = totalPrice.add(order.getPrice().subtract(Constants.CHANGE).subtract(order.getProduct().getPrice()));
        }

        doc.add(table);

        return totalPrice;
    }

    private BigDecimal addProductSoldInAvailableStockOrdersTable(Document doc, List<Order> orders) throws DocumentException {
        if (orders.isEmpty()) {
            doc.add(new Paragraph("No orders in this category."));
            return BigDecimal.ZERO;
        }

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);

        addHeader(table, "Order ID", "Product", "Qty", "Product price");

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (Order order : orders) {
            table.addCell(order.getId().toString());
            table.addCell(order.getProduct().getName());
            table.addCell(order.getQuantity().toString());
            table.addCell(order.getProduct().getPrice().toPlainString());
            totalPrice = totalPrice.add(order.getProduct().getPrice());
        }

        doc.add(table);

        return totalPrice;
    }

    private void addHeader(PdfPTable table, String... headers) {
        Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(new Color(218, 218, 218)); // Gray
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPadding(6f);
            table.addCell(cell);
        }
    }

}
