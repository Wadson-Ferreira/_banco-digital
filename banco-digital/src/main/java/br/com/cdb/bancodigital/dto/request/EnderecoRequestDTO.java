package br.com.cdb.bancodigital.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EnderecoRequestDTO {
    @NotBlank(message = "Rua é obrigatória.")
    private String rua;

    @NotBlank(message = "Número é obrigatório.")
    private String numero;

    public void setNumero(String numero) {
        this.numero = "0".equals(numero) ? "S/N" : numero;
    }

    private String complemento;

    @NotBlank(message = "Bairro é obrigatório.")
    private String bairro;

    @NotBlank(message = "Cidade é obrigatória.")
    private String cidade;

    @NotBlank(message = "Estado é obrigatório.")
    @Size(min = 2, max = 2, message = "Estado deve ser a sigla com 2 caracteres.")
    private String estado;

    @NotBlank(message = "CEP é obrigatório.")
    @Pattern(regexp = "\\d{8}", message = "CEP deve conter 8 dígitos.")
    private String cep;
}