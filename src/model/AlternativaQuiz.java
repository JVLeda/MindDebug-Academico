package model;

public class AlternativaQuiz extends Entidade {
    private String texto;
    private String explicacao;
    private boolean correta;

    public AlternativaQuiz(String texto, String explicacao, boolean correta) {
        this.texto = texto;
        this.explicacao = explicacao;
        this.correta = correta;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getExplicacao() {
        return explicacao;
    }

    public void setExplicacao(String explicacao) {
        this.explicacao = explicacao;
    }

    public boolean isCorreta() {
        return correta;
    }
}
