public enum EnumStatus {
    OK("Status do arquivo cujo digest calculado é igual ao digest fornecido no arquivo ArqListaDigest e não colide com o digest de outro arquivo na pasta."),
    NOT_OK("Status do arquivo cujo digest não é igual ao digest fornecido no arquivo ArqListaDigest e não colide com o digest de outro arquivo na pasta."),
    NOT_FOUND(
            "Status do arquivo cujo digest não foi encontrado no arquivo ArqListaDigest e não colide com o digest de outro arquivo na pasta."),
    COLISION(
            "Status do arquivo cujo digest calculado colide com o digest de outro arquivo de nome diferente encontrado no arquivo ArqListaDigest ou com o digest de um dos arquivos presentes na pasta.");

    private final String description;

    EnumStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}