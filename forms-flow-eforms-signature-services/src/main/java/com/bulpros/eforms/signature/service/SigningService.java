package com.bulpros.eforms.signature.service;

import com.bulpros.eforms.signature.exception.SignatureRequestException;
import com.bulpros.eforms.signature.model.CAdESDefaultParameters;
import com.bulpros.eforms.signature.model.DataToSignRequest;
import com.bulpros.eforms.signature.model.DefaultParameters;
import com.bulpros.eforms.signature.model.DigestToSignRequest;
import com.bulpros.eforms.signature.model.DocumentToSignRequest;
import com.bulpros.eforms.signature.model.PAdESDefaultParameters;
import com.bulpros.eforms.signature.model.XAdESDefaultParameters;
import com.bulpros.eforms.signature.utils.DocumentUtils;
import com.google.common.net.MediaType;
import eu.europa.esig.dss.AbstractSignatureParameters;
import eu.europa.esig.dss.asic.cades.ASiCWithCAdESSignatureParameters;
import eu.europa.esig.dss.asic.cades.signature.ASiCWithCAdESService;
import eu.europa.esig.dss.cades.CAdESSignatureParameters;
import eu.europa.esig.dss.cades.signature.CAdESService;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.SignatureAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.SignerTextHorizontalAlignment;
import eu.europa.esig.dss.enumerations.SignerTextPosition;
import eu.europa.esig.dss.enumerations.SignerTextVerticalAlignment;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.DigestDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.pades.DSSFont;
import eu.europa.esig.dss.pades.DSSJavaFont;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.SignatureFieldParameters;
import eu.europa.esig.dss.pades.SignatureImageParameters;
import eu.europa.esig.dss.pades.SignatureImageTextParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.pdf.AnnotationBox;
import eu.europa.esig.dss.pdf.PdfAnnotation;
import eu.europa.esig.dss.pdf.modifications.DefaultPdfDifferencesFinder;
import eu.europa.esig.dss.pdf.pdfbox.PdfBoxDocumentReader;
import eu.europa.esig.dss.pdf.pdfbox.PdfBoxNativeObjectFactory;
import eu.europa.esig.dss.signature.DocumentSignatureService;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import eu.europa.esig.dss.xades.signature.XAdESService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class SigningService {
    @Autowired
    private CAdESService cadesService;

    @Autowired
    private ASiCWithCAdESService asicWithCAdESService;

    @Autowired
    private PAdESService padesService;

    @Autowired
    private XAdESService xadesService;

    private UrlValidator urlValidator = UrlValidator.getInstance();

    private final int MIN_X = 20;
    private final int MIN_Y = 20;
    private Tika tika = new Tika();

    static private final Pattern NAME_PATTERN = Pattern.compile("CN=(?<fullName>[^,]*)");
    static private final String FULL_NAME = "fullName";

    final static DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private class DocumentDetails {
        float width;
        float height;
        float cropPartX;
        float cropPartY;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ToBeSigned getDataToSign(DocumentToSignRequest request) {
        log.info(String.format("Start getDataToSign with one document: %s", request.getDocumentName()));
        DocumentSignatureService service = getSignatureService(request.getContainerType(), request.getSignatureForm());

        AbstractSignatureParameters parameters = fillParameters(request);

        try {
            String documentToSign = request.getDocumentToSign();
            DSSDocument toSignDocument = DocumentUtils.toDSSDocument(documentToSign, request.getDocumentName());
            ToBeSigned toBeSigned = service.getDataToSign(toSignDocument, parameters);
            log.info("End getDataToSign with one document");
            return toBeSigned;
        } catch (Exception e) {
            throw new SignatureRequestException(e.getMessage());
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ToBeSigned getDataToSign(DigestToSignRequest request) {
        log.info(String.format("Start getDataToSign with one digest: %s", request.getDigestToSign()));
        DocumentSignatureService service = getSignatureService(null, request.getSignatureForm());

        AbstractSignatureParameters parameters = fillParameters(request);

        try {
            DigestDocument toSignDigest = new DigestDocument(request.getDigestAlgorithm(), request.getDigestToSign(),
                    request.getDocumentName());
            ToBeSigned toBeSigned = service.getDataToSign(toSignDigest, parameters);
            log.info("End getDataToSign with one digest");
            return toBeSigned;
        } catch (Exception e) {
            throw new SignatureRequestException(e.getMessage());
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public DSSDocument signDigest(DigestToSignRequest request) {
        log.info(String.format("Start signDigest with one digest: %s", request.getDigestToSign()));
        DocumentSignatureService service = getSignatureService(null, request.getSignatureForm());

        AbstractSignatureParameters parameters = fillParameters(request);

        try {
            DigestDocument toSignDigest = new DigestDocument(request.getDigestAlgorithm(), request.getDigestToSign(),
                    request.getDocumentName());
            SignatureAlgorithm sigAlgorithm = SignatureAlgorithm.getAlgorithm(request.getEncryptionAlgorithm(),
                    request.getDigestAlgorithm());
            SignatureValue signatureValue = new SignatureValue(sigAlgorithm,
                    Utils.fromBase64(request.getSignatureValue()));
            DSSDocument signedDocument = service.signDocument(toSignDigest, parameters, signatureValue);
            log.info("End signDigest with one digest");
            return signedDocument;
        } catch (Exception e) {
            log.error(String.format("Problem in signDigest with one digest: %s", e.getMessage()));
            throw new SignatureRequestException(e.getMessage());
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public DSSDocument signDocument(DocumentToSignRequest request) {
        log.info("Start signDocument with one document: %s", request.getDocumentName());
        DocumentSignatureService service = getSignatureService(request.getContainerType(), request.getSignatureForm());

        AbstractSignatureParameters parameters = fillParameters(request);

        try {
            DSSDocument toSignDocument = DocumentUtils.toDSSDocument(request.getDocumentToSign(),
                    request.getDocumentName());
            SignatureAlgorithm sigAlgorithm = SignatureAlgorithm.getAlgorithm(request.getEncryptionAlgorithm(),
                    request.getDigestAlgorithm());
            SignatureValue signatureValue = new SignatureValue(sigAlgorithm,
                    Utils.fromBase64(request.getSignatureValue()));
            DSSDocument signedDocument = service.signDocument(toSignDocument, parameters, signatureValue);
            log.info("End signDocument with one document");
            return signedDocument;
        } catch (Exception e) {
            log.error(String.format("Problem in signDocument with one document: %s", e.getMessage()));
            throw new SignatureRequestException(e.getMessage());
        }
    }

    /**
     * Tries to get the appropriate signature form depending on the document type or return CAdES
     *
     * @param request
     * @return
     */
    public DocumentToSignRequest fillSignatureParametersByDocument(DocumentToSignRequest request) {
        DefaultParameters parameters = new CAdESDefaultParameters();

        MediaType mediaType = getMediaType(request);
        if (mediaType != null) {
            if (mediaType.is(MediaType.PDF)) {
                parameters = new PAdESDefaultParameters();
                request.fillWith(parameters);
                SignatureImageParameters signatureImageParameters = createVisibleImageParameters(request);
                request.setSignatureImageParameters(signatureImageParameters);
                return request;
            } else if (mediaType.subtype().contains("xml")) {
                parameters = new XAdESDefaultParameters();
                request.fillWith(parameters);
                return request;
            }
        }
        request.fillWith(parameters);
        return request;
    }

    private SignatureImageParameters createVisibleImageParameters(DocumentToSignRequest request) {
        return createSignatureImageParameters(request);
    }

    private MediaType getMediaType(DocumentToSignRequest request) {
        MediaType mediaType = null;
        String document = request.getDocumentToSign();
        if (Utils.isBase64Encoded(document)) {
            mediaType = MediaType.parse(tika.detect(Utils.fromBase64(document)));
        } else if (urlValidator.isValid(document)) {
            try {
                mediaType = MediaType.parse(tika.detect(new URL(document)));
            } catch (IOException e) {
                log.warn(String.format("Couldn't get mime type: %s", e.getMessage()));
            }
        }
        return mediaType;
    }

    private SignatureImageParameters createSignatureImageParameters(DocumentToSignRequest documentToSignRequest) {
        SignatureImageParameters imageParameters = new SignatureImageParameters();
        DocumentDetails imageDimensions = new DocumentDetails(20, 100, 0, 0);
        if(documentToSignRequest.getVisibleSignImage() != null) {
            InMemoryDocument logo = new InMemoryDocument(Utils.fromBase64(documentToSignRequest.getVisibleSignImage()));
            imageDimensions = getLogoDimensions(logo);
            imageParameters.setImage(logo);
            //Get PDF document
        }
        byte[] pdfSignDocument = new InMemoryDocument(
                Utils.fromBase64(documentToSignRequest.getDocumentToSign())).getBytes();
        PDDocument pdfDocument = null;
        try {
            pdfDocument = PDDocument.load(pdfSignDocument);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int pageNumber = pdfDocument.getNumberOfPages();
        PDPage pdPage = pdfDocument.getPage(pageNumber - 1);
        DocumentDetails pdfMediaBoxDimensions = calculateWatermarkPosition(pdPage);
        // set an image
        try {
            pdfDocument.close();
        } catch (IOException e) {
            log.error("Could not close pdf document reader!");
        }
        // initialize signature field parameters
        SignatureFieldParameters fieldParameters = new SignatureFieldParameters();
        imageParameters.setFieldParameters(fieldParameters);
        fieldParameters.setPage(pageNumber);
        fieldParameters.setOriginX(pdfMediaBoxDimensions.getCropPartX() + MIN_X);
        fieldParameters.setOriginY(pdfMediaBoxDimensions.getHeight() - imageDimensions.getHeight() - MIN_Y);
        fieldParameters.setWidth(imageDimensions.getWidth() + 150);
        fieldParameters.setHeight(imageDimensions.getHeight());
        SignatureImageTextParameters textParameters = new SignatureImageTextParameters();

        float isOverlapped = isOverlappedSignatures(documentToSignRequest,
                new AnnotationBox(MIN_X, MIN_Y, MIN_X + imageDimensions.getWidth() + 140, MIN_Y + 100 ), pageNumber);

        fieldParameters.setOriginX(fieldParameters.getOriginX() + isOverlapped);
        // Defines the text content
        String visibleSignatureText = getVisibleSignatureText(documentToSignRequest);
        textParameters.setText(visibleSignatureText);
        // Defines the color of the characters
        textParameters.setTextColor(new Color(153, 0, 0));
        // Define Font
        DSSFont font = new DSSJavaFont(Font.SERIF);
        font.setSize(10);
        // Defines the background color for the area filled out by the text
        textParameters.setBackgroundColor(Color.WHITE);
        // Defines a padding between the text and a border of its bounding area
        textParameters.setPadding(10);
        // Set textParameters to a SignatureImageParameters object
        textParameters.setSignerTextPosition(SignerTextPosition.RIGHT);
        // Specifies a horizontal alignment of a text with respect to its area
        textParameters.setSignerTextHorizontalAlignment(SignerTextHorizontalAlignment.LEFT);
        // Specifies a vertical alignment of a text block with respect to a signature fieldarea
        textParameters.setSignerTextVerticalAlignment(SignerTextVerticalAlignment.MIDDLE);
        imageParameters.setTextParameters(textParameters);
        return imageParameters;
    }

    private static float isOverlappedSignatures(DocumentToSignRequest documentToSignRequest, AnnotationBox box, int pageNumber) {
        try {
            float maxX = 0;
            DSSDocument toSignDocument = DocumentUtils.toDSSDocument(documentToSignRequest.getDocumentToSign(),
                    documentToSignRequest.getDocumentName());
            try (PdfBoxDocumentReader pdfDocumentReader = new PdfBoxDocumentReader(toSignDocument)) {
                List<PdfAnnotation> pdfAnnotations = pdfDocumentReader.getPdfAnnotations(pageNumber);
                for (PdfAnnotation pdfBox : pdfAnnotations) {
                    float currentMaxX = pdfBox.getAnnotationBox().getMaxX();
                    if (maxX < currentMaxX) {
                        maxX = currentMaxX;
                    }

                }
                DefaultPdfDifferencesFinder pdfDifferencesFinder = new DefaultPdfDifferencesFinder();
                if (pdfDifferencesFinder.isAnnotationBoxOverlapping(box, pdfAnnotations)) {
                    return maxX;
                }
            }
        } catch (IOException e) {
            log.error("Error: could not read document");
        }
        return 0;
    }

    private DocumentDetails getLogoDimensions(InMemoryDocument logo) {
        BufferedImage buf = null;
        try {
            InputStream in = new ByteArrayInputStream(logo.getBytes());
            buf = ImageIO.read(in);
            return new DocumentDetails(buf.getWidth(), buf.getHeight(), 0, 0);
        } catch (IOException e) {
            log.error("The image dimensions could not be read! Reason: " + e.getMessage());
        }
        return null;
    }

    private DocumentDetails calculateWatermarkPosition(PDPage pdPage) {
        PDRectangle cropBox = pdPage.getCropBox();
        PDRectangle mediaBox = pdPage.getMediaBox();

        float height = mediaBox.getHeight();
        float width = mediaBox.getWidth();

        return new DocumentDetails(width, height, (width - cropBox.getWidth()) / 2, (height - cropBox.getHeight()) / 2);
    }

    private String getVisibleSignatureText(DocumentToSignRequest documentToSignRequest) {
        String fullName = getSignerNames(documentToSignRequest);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Digitally signed by:\n");
        String[] names = fullName.split(" ");
        for (String name : names) {
            stringBuilder.append(name);
            stringBuilder.append("\n");
        }
        stringBuilder.append("Date: ");
        LocalDateTime localDateTime = documentToSignRequest.getSigningDate().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        String signingDate = localDateTime.format(LOCAL_DATE_TIME_FORMATTER);

        stringBuilder.append(signingDate);
        stringBuilder.append("\n");
        stringBuilder.append("Compliant with eIDAS");
        String visibleSignatureText = stringBuilder.toString();
        return visibleSignatureText;
    }

    private String getSignerNames(DocumentToSignRequest documentToSignRequest) {
        String subject = documentToSignRequest.getSigningCertificateToken().getCertificate().getSubjectX500Principal()
                .getName();
        Matcher matcher = NAME_PATTERN.matcher(subject);
        matcher.find();
        return matcher.group(FULL_NAME);
    }

    @SuppressWarnings({ "rawtypes" })
    protected DocumentSignatureService getSignatureService(ASiCContainerType containerType,
            SignatureForm signatureForm) {
        DocumentSignatureService service = null;
        if (containerType != null) {
            service = asicWithCAdESService;
        } else {
            switch (signatureForm) {
            case CAdES:
                service = cadesService;
                break;
            case PAdES:
                padesService.setPdfObjFactory(new PdfBoxNativeObjectFactory());
                service = padesService;
                break;
            case XAdES:
                service = xadesService;
                break;
            default:
                log.error(String.format("Unknown signature form: %s", signatureForm));
            }
        }
        return service;
    }

    @SuppressWarnings("rawtypes")
    protected AbstractSignatureParameters fillParameters(DocumentToSignRequest request) {
        AbstractSignatureParameters parameters = getSignatureParameters(request.getContainerType(),
                request.getSignatureForm(), request.getSignatureImageParameters());
        parameters.setSignaturePackaging(request.getSignaturePackaging());

        fillParameters(parameters, request);

        return parameters;
    }

    @SuppressWarnings("rawtypes")
    protected AbstractSignatureParameters fillParameters(DigestToSignRequest request) {
        AbstractSignatureParameters parameters = getSignatureParameters(null, request.getSignatureForm(), null);
        parameters.setSignaturePackaging(SignaturePackaging.DETACHED);

        fillParameters(parameters, request);

        return parameters;
    }

    @SuppressWarnings("rawtypes")
    protected void fillParameters(AbstractSignatureParameters parameters, DataToSignRequest request) {
        parameters.setSignatureLevel(request.getSignatureLevel());
        parameters.setDigestAlgorithm(request.getDigestAlgorithm());
        // parameters.setEncryptionAlgorithm(form.getEncryptionAlgorithm()); retrieved from certificate
        parameters.bLevel().setSigningDate(request.getSigningDate());
        parameters.setSignWithExpiredCertificate(request.isSignWithExpiredCertificate());
        parameters.setSigningCertificate(request.getSigningCertificateToken());
        List<String> base64CertificateChain = request.getCertificateChain();
        if (Utils.isCollectionNotEmpty(base64CertificateChain)) {
            List<CertificateToken> certificateChain = new LinkedList<>();
            for (String base64Certificate : base64CertificateChain) {
                certificateChain.add(DSSUtils.loadCertificateFromBase64EncodedString(base64Certificate));
            }
            parameters.setCertificateChain(certificateChain);
        }
    }

    @SuppressWarnings("rawtypes")
    protected AbstractSignatureParameters getSignatureParameters(ASiCContainerType containerType,
            SignatureForm signatureForm, SignatureImageParameters imageParameters) {
        AbstractSignatureParameters parameters = null;
        if (containerType != null) {
            parameters = getASiCSignatureParameters(containerType, signatureForm);
        } else {
            switch (signatureForm) {
            case CAdES:
                parameters = new CAdESSignatureParameters();
                break;
            case PAdES:
                PAdESSignatureParameters padesParams = new PAdESSignatureParameters();
                padesParams.setContentSize(9472 * 2); // double reserved space for signature
                padesParams.setImageParameters(imageParameters);
                parameters = padesParams;
                break;
            case XAdES:
                parameters = new XAdESSignatureParameters();
                break;
            default:
                log.error(String.format("Unknown signature form: %s", signatureForm));
            }
        }
        return parameters;
    }

    @SuppressWarnings("rawtypes")
    protected AbstractSignatureParameters getASiCSignatureParameters(ASiCContainerType containerType,
            SignatureForm signatureForm) {
        AbstractSignatureParameters parameters = null;
        switch (signatureForm) {
        case CAdES:
            ASiCWithCAdESSignatureParameters asicCadesParams = new ASiCWithCAdESSignatureParameters();
            asicCadesParams.aSiC().setContainerType(containerType);
            parameters = asicCadesParams;
            break;
        default:
            log.error(String.format("Unknow signature form %s for ASiC container.", signatureForm));
        }
        return parameters;
    }
}
