public class Main {
    public static void main(String[] args) {
        try {
            var xmlHandler = new XMLHandler("src/xml_example.xml");
            xmlHandler.addNewDigestForFile("pedro mama e engole", "sha1", "nao");

        } catch (Exception e) {
            System.out.println("Erro ao carregar o arquivo XML: " + e.getMessage());
        }
    }
}