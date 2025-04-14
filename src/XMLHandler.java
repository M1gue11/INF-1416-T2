import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.List;

public class XMLHandler {
    private final String filePath;
    public Document document;

    public XMLHandler(String filePath) throws Exception {
        this.filePath = filePath;
        loadXML();
    }

    private void removeWhitespaceNodes(Node node) {
        NodeList childNodes = node.getChildNodes();
        for (int i = childNodes.getLength() - 1; i >= 0; i--) {
            Node child = childNodes.item(i);

            // Remove nós de texto que contenham apenas espaços em branco
            if (child.getNodeType() == Node.TEXT_NODE && child.getTextContent().trim().isEmpty()) {
                node.removeChild(child);
            } else if (child.hasChildNodes()) {
                removeWhitespaceNodes(child); // Recursivamente limpa os filhos
            }
        }
    }

    private void loadXML() throws Exception {
        File xmlFile = new File(filePath);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringElementContentWhitespace(true); // Ignora espaços em branco
        DocumentBuilder builder = factory.newDocumentBuilder();

        if (!xmlFile.exists() || xmlFile.length() == 0) {
            document = builder.newDocument();
            Element rootElement = document.createElement("CATALOG");
            document.appendChild(rootElement);
            saveXML();
        } else {
            document = builder.parse(xmlFile);
            document.getDocumentElement().normalize();

            // Remove nós de texto contendo apenas espaços em branco
            removeWhitespaceNodes(document);

            if (document.getDocumentElement() == null ||
                    !document.getDocumentElement().getNodeName().equals("CATALOG")) {
                document = builder.newDocument();
                Element rootElement = document.createElement("CATALOG");
                document.appendChild(rootElement);
                saveXML();
            }
        }
    }

    private void saveXML() throws Exception {
        document.normalizeDocument();

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        // format xml
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); // Define o nível de indentação
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new File(filePath));

        transformer.transform(source, result);
    }

    public void addFile(FileEntry fileEntry) throws Exception {
        Element root = document.getDocumentElement();

        Element fileNameElem = fileEntry.buildFileEntryElement(document);

        root.appendChild(fileNameElem);
        saveXML();
    }

    public void removeFile(String fileName) throws Exception {
        NodeList entries = document.getElementsByTagName("FILE_ENTRY");
        for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            Element nameElem = (Element) entry.getElementsByTagName("FILE_NAME").item(0);

            if (nameElem != null && nameElem.getTextContent().equals(fileName)) {
                entry.getParentNode().removeChild(entry);
                break;
            }
        }
        saveXML();
    }

    public void editFile(FileEntry fileEntry) throws Exception {
        NodeList entries = document.getElementsByTagName("FILE_ENTRY");
        for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            Element nameElem = (Element) entry.getElementsByTagName("FILE_NAME").item(0);

            if (nameElem != null && nameElem.getTextContent().equals(fileEntry.getFileName())) {
                // Remove todos os DIGEST_ENTRY existentes
                NodeList digestNodes = entry.getElementsByTagName("DIGEST_ENTRY");
                while (digestNodes.getLength() > 0) {
                    entry.removeChild(digestNodes.item(0));
                }

                // Adiciona os novos DIGEST_ENTRY
                for (DigestEntry digest : fileEntry.getDigests()) {
                    Element digestEntryElem = document.createElement("DIGEST_ENTRY");

                    Element typeElem = document.createElement("DIGEST_TYPE");
                    typeElem.setTextContent(digest.getType());

                    Element hexElem = document.createElement("DIGEST_HEX");
                    hexElem.setTextContent(digest.getHex());

                    digestEntryElem.appendChild(typeElem);
                    digestEntryElem.appendChild(hexElem);
                    entry.appendChild(digestEntryElem);
                }

                break;
            }
        }
        saveXML();
    }

    public void addNewDigestForFile(String fileName, String type, String hex) throws Exception {
        NodeList entries = document.getElementsByTagName("FILE_ENTRY");
        DigestEntry newDigestEntry = new DigestEntry(type, hex);
        Element fileElement = null;

        // search for the file entry
        for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            Element nameElem = (Element) entry.getElementsByTagName("FILE_NAME").item(0);

            if (nameElem != null && nameElem.getTextContent().equals(fileName)) {
                fileElement = entry;
                break;
            }
        }

        // If the file entry does not exist, create a new one
        if (fileElement == null) {
            FileEntry fileEntry = new FileEntry(fileName);
            fileEntry.addDigestEntry(newDigestEntry);
            addFile(fileEntry);
        } else {
            FileEntry entryFile = new FileEntry(fileElement);
            entryFile.addDigestEntry(newDigestEntry);
            editFile(entryFile);
        }
    }

    public void printXML() throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

        DOMSource source = new DOMSource(document);
        StreamResult consoleResult = new StreamResult(System.out);
        transformer.transform(source, consoleResult);
    }

    public FileEntry findFileEntry(String fileName) {
        NodeList entries = document.getElementsByTagName("FILE_ENTRY");
        FileEntry fileEntryEl = null;
        for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            Element nameElem = (Element) entry.getElementsByTagName("FILE_NAME").item(0);

            if (nameElem.getTextContent().equals(fileName)) {
                fileEntryEl = new FileEntry(entry);
                break;
            }
        }

        return fileEntryEl;
    }

    public List<FileEntry> getFilesEntries() {
        NodeList digestEntries = document.getElementsByTagName("FILE_ENTRY");
        List<FileEntry> fileEntries = new java.util.ArrayList<>();
        for (int i = 0; i < digestEntries.getLength(); i++) {
            Element fileEntry = (Element) digestEntries.item(i);
            fileEntries.add(new FileEntry(fileEntry));
        }
        return fileEntries;
    }

    public EnumStatus getStatus(String fileName, String digestType, String digest) {
        FileEntry fileEntry = findFileEntry(fileName);
        List<FileEntry> fileEntries = getFilesEntries();

        boolean colision = fileEntries.stream()
                .filter(f -> f.getFileName() != fileName)
                .filter(f -> f.getDigests().stream().anyMatch(d -> d.getHex().equals(digest)))
                .anyMatch(null);

        System.out.println("colision: " + colision);
        if (colision) {
            return EnumStatus.COLISION;
        } else if (fileEntry == null) {
            return EnumStatus.NOT_FOUND;
        } else {
            DigestEntry digestEntry = fileEntry.findDigestEntry(digestType);
            if (digestEntry == null) {
                return EnumStatus.NOT_FOUND;
            } else if (digestEntry.getHex().equals(digest)) {
                return EnumStatus.OK;
            } else {
                return EnumStatus.NOT_OK;
            }
        }

    }
}
