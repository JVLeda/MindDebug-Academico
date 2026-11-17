package model;

import excecao.DadoInvalidoException;
import excecao.RegraDeNegocioException;

import java.util.ArrayList;
import java.util.Date;

public class Periodo extends Entidade {
    private int numero;
    private String nome;
    private int ano;
    private int semestre;
    private Date dataInicio;
    private Date dataFim;
    private boolean ativo = true;
    private ArrayList<Materia> materias = new ArrayList<>();

    public Periodo(int numero, String nome) {
        this.numero = numero;
        this.nome = nome;
    }

    public void adicionarMateria(Materia materia) {
        materias.add(materia);
    }

    public void removerMateria(Materia materia) {
        materias.remove(materia);
    }

    public int getNumero() {
        return numero;
    }

    public String getNome() {
        return nome;
    }

    public void definirDatas(Date dataInicio, Date dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new DadoInvalidoException("Informe a data de inicio e a data de termino.");
        }
        if (!dataFim.after(dataInicio)) {
            throw new RegraDeNegocioException("A data de termino deve ser depois da data de inicio.");
        }
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    public String getAnoSemestre() {
        if (ano == 0) {
            return "";
        }
        return ano + "." + semestre;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public int getSemestre() {
        return semestre;
    }

    public void setSemestre(int semestre) {
        this.semestre = semestre;
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public ArrayList<Materia> getMaterias() {
        return materias;
    }

    @Override
    public String toString() {
        return numero + "o Periodo";
    }
}
