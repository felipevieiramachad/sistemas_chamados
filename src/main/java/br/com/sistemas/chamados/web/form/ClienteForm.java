package br.com.sistemas.chamados.web.form;

import br.com.sistemas.chamados.dto.ClienteRequest;
import br.com.sistemas.chamados.dto.ClienteResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ClienteForm {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 120, message = "E-mail deve ter no máximo 120 caracteres")
    private String email;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String telefone;

    public static ClienteForm de(ClienteResponse c) {
        ClienteForm form = new ClienteForm();
        form.nome = c.nome();
        form.email = c.email();
        form.telefone = c.telefone();
        return form;
    }

    public ClienteRequest paraRequest() {
        String fone = (telefone == null || telefone.isBlank()) ? null : telefone;
        return new ClienteRequest(nome, email, fone);
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
}
