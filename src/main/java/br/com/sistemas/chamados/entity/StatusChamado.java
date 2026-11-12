package br.com.sistemas.chamados.entity;

public enum StatusChamado {
    ABERTO("Aberto"),
    EM_ATENDIMENTO("Em atendimento"),
    AGUARDANDO_CLIENTE("Aguardando cliente"),
    FECHADO("Fechado");

    private final String rotulo;

    StatusChamado(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
