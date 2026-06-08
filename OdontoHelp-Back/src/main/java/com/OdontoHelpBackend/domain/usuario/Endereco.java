package com.OdontoHelpBackend.domain.usuario;

import com.OdontoHelpBackend.util.SensitiveDataConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "TB_ENDERECO")
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = true, unique = true)
    private Usuario usuario;

    @Convert(converter = SensitiveDataConverter.class)
    @Column(name = "rua_encrypted", nullable = false, length = 512)
    private String rua;

    @Convert(converter = SensitiveDataConverter.class)
    @Column(name = "numero_encrypted", nullable = false, length = 512)
    private String numero;

    @Convert(converter = SensitiveDataConverter.class)
    @Column(name = "complemento_encrypted", length = 512)
    private String complemento;

    @Convert(converter = SensitiveDataConverter.class)
    @Column(name = "bairro_encrypted", nullable = false, length = 512)
    private String bairro;

    @Convert(converter = SensitiveDataConverter.class)
    @Column(name = "cidade_encrypted", nullable = false, length = 512)
    private String cidade;

    @Convert(converter = SensitiveDataConverter.class)
    @Column(name = "uf_encrypted", nullable = false, length = 16)
    private String uf;

    @Convert(converter = SensitiveDataConverter.class)
    @Column(name = "cep_encrypted", nullable = false, length = 512)
    private String cep;

    @PrePersist
    @PreUpdate
    private void prepararEndereco() {
        if (this.cep != null) this.cep = this.cep.replaceAll("\\D", "");
        if (this.rua != null) this.rua = this.rua.trim().toUpperCase();
        if (this.bairro != null) this.bairro = this.bairro.trim().toUpperCase();
        if (this.cidade != null) this.cidade = this.cidade.trim().toUpperCase();
        if (this.uf != null) this.uf = this.uf.trim().toUpperCase();
        if (this.complemento != null) this.complemento = this.complemento.trim().toUpperCase();
    }
}
