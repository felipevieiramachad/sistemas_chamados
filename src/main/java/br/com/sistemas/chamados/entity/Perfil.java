package br.com.sistemas.chamados.entity;

public enum Perfil {
    ADMIN("Administrador"),
    TECNICO("Técnico");

    private final String rotulo;

    Perfil(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
