package com.OdontoHelpBackend.service.Usuario;

import com.OdontoHelpBackend.domain.usuario.Paciente;
import com.OdontoHelpBackend.domain.usuario.Usuario;
import com.OdontoHelpBackend.domain.usuario.enums.PerfilUsuario;
import com.OdontoHelpBackend.infra.exception.AcessoNegadoException;
import com.OdontoHelpBackend.infra.security.token.RefreshTokenRepository;
import com.OdontoHelpBackend.repository.Usuario.PacienteRepository;
import com.OdontoHelpBackend.service.Clinico.OdontogramaService;
import com.OdontoHelpBackend.service.Utils.PrivacidadeService;
import com.OdontoHelpBackend.service.Utils.ValidacoesService;
import com.OdontoHelpBackend.Mapper.PacienteMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock private PacienteRepository pacienteRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private UsuarioService usuarioService;
    @Mock private PacienteMapper pacienteMapper;
    @Mock private ValidacoesService validacoesService;
    @Mock private OdontogramaService odontogramaService;
    @Mock private PrivacidadeService privacidadeService;
    @InjectMocks private PacienteService pacienteService;

    private Paciente paciente;

    @BeforeEach
    void setUp() {
        paciente = new Paciente();
        paciente.setId(10L);
        paciente.setNome("Maria");
        paciente.setCpf("12345678901");
        paciente.setEmail("maria@test.com");
    }

    @Test
    void adminConsultaDadosPessoais() {
        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(paciente));
        var dto = pacienteService.dadosPessoais(10L, usuario(1L, PerfilUsuario.ADMIN));
        assertThat(dto.cpf()).isEqualTo("12345678901");
    }

    @Test
    void pacienteNaoAcessaOutroTitular() {
        assertThatThrownBy(() -> pacienteService.dadosPessoais(10L, usuario(99L, PerfilUsuario.PACIENTE)))
                .isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    void anonimizarLimpaIdentificadores() {
        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(paciente));
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(i -> i.getArgument(0));
        pacienteService.anonimizar(10L);
        assertThat(paciente.getCpf()).isNull();
        assertThat(paciente.getEmailHash()).isNull();
        verify(refreshTokenRepository).revogarTodosPorUsuario(10L);
    }

    private static Usuario usuario(Long id, PerfilUsuario perfil) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setPerfil(perfil);
        return u;
    }
}
