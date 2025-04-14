package main.java;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.w3c.dom.*;

public class FileEntry {
    private String fileName;
    private List<DigestEntry> digestEntries;

    public FileEntry(String fileName) {
        this.fileName = fileName;
        this.digestEntries = new ArrayList<DigestEntry>();
    }

    public FileEntry(String fileName, List<DigestEntry> digestEntries) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        if (digestEntries == null) {
            throw new IllegalArgumentException("Digest entries cannot be null");
        }

        this.fileName = fileName;
        this.digestEntries = digestEntries;
    }

    public FileEntry(Element fileEntryElem) {
        if (fileEntryElem == null) {
            throw new IllegalArgumentException("File entry element cannot be null");
        }

        this.fileName = fileEntryElem.getElementsByTagName("FILE_NAME").item(0).getTextContent();
        this.digestEntries = new ArrayList<DigestEntry>();

        NodeList digestEntriesList = fileEntryElem.getElementsByTagName("DIGEST_ENTRY");
        for (int i = 0; i < digestEntriesList.getLength(); i++) {
            DigestEntry digestEntry = new DigestEntry((Element) digestEntriesList.item(i));
            this.digestEntries.add(digestEntry);
        }
    }

    public String getFileName() {
        return fileName;
    }

    public List<DigestEntry> getDigests() {
        return digestEntries;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setDigestEntry(List<DigestEntry> digestEntries) {
        this.digestEntries = digestEntries;
    }

    public DigestEntry findDigestEntry(String type) {
        return this.digestEntries.stream()
                .filter(d -> d.getType().equals(type))
                .findFirst()
                .orElse(null);
    }

    public void addDigestEntry(String type, String hex) {
        DigestEntry digestEntry = new DigestEntry(type, hex);
        addDigestEntry(digestEntry);
    }

    public void addDigestEntry(DigestEntry digestEntry) {
        Optional<DigestEntry> digestType = this.digestEntries.stream()
                .filter(d -> d.getType().equals(digestEntry.getType()))
                .findFirst();

        if (digestType.isPresent()) {
            digestType.get().setHex(digestEntry.getHex());
        } else {
            this.digestEntries.add(digestEntry);
        }
    }

    public Element buildFileEntryElement(Document document) {
        Element fileEntryElem = document.createElement("FILE_ENTRY");

        Element nameElem = document.createElement("FILE_NAME");
        nameElem.setTextContent(this.fileName);
        fileEntryElem.appendChild(nameElem);

        for (DigestEntry digest : this.digestEntries) {
            Element digestEntryElem = digest.buildDigestEntryElement(document);
            fileEntryElem.appendChild(digestEntryElem);
        }

        return fileEntryElem;
    }
}
