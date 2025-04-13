import org.w3c.dom.*;

public class DigestEntry {
    private String type;
    private String hex;

    public DigestEntry(String type, String hex) {
        this.type = type;
        this.hex = hex;
    }

    public DigestEntry(Element digestEntryElem) {
        this.type = digestEntryElem.getElementsByTagName("DIGEST_TYPE").item(0).getTextContent();
        this.hex = digestEntryElem.getElementsByTagName("DIGEST_HEX").item(0).getTextContent();
    }

    public String getType() {
        return type;
    }

    public String getHex() {
        return hex;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setHex(String hex) {
        this.hex = hex;
    }

    public Element buildDigestEntryElement(Document document) {
        Element digestEntryElem = document.createElement("DIGEST_ENTRY");

        Element typeElem = document.createElement("DIGEST_TYPE");
        typeElem.setTextContent(this.type);

        Element hexElem = document.createElement("DIGEST_HEX");
        hexElem.setTextContent(this.hex);

        digestEntryElem.appendChild(typeElem);
        digestEntryElem.appendChild(hexElem);

        return digestEntryElem;
    }

    public String toString() {
        return "DigestEntry{" +
                "type='" + type + '\'' +
                ", hex='" + hex + '\'' +
                '}';
    }
}
