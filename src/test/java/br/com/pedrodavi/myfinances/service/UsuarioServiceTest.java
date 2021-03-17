package br.com.pedrodavi.myfinances.service;

import br.com.pedrodavi.myfinances.exception.ErroAutenticacaoException;
import br.com.pedrodavi.myfinances.exception.RegraNegocioException;
import br.com.pedrodavi.myfinances.model.Usuario;
import br.com.pedrodavi.myfinances.repository.UsuarioRepository;
import br.com.pedrodavi.myfinances.service.impl.UsuarioServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class UsuarioServiceTest {

    @SpyBean
    UsuarioServiceImpl service;

    @MockBean
    UsuarioRepository repository;


    @Test(expected = Test.None.class) // espera que não lança exceção
    public void deveValidarEmail() {
        // cenário
        Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(false);
        // ação
        service.validarEmail("email@email.com");
    }

    @Test(expected = RegraNegocioException.class) // espera que lance exceção
    public void deveRetornarErroAoValidarEmailQuandoExistirEmailCadastrado() {
        // cenário
        Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(true);
        // ação
        service.validarEmail("email@email.com");
    }

    @Test(expected = Test.None.class) // espera que não lança exceção
    public void deveAutenticarUsuario() {
        // cenário
        String email = "email@teste.com";
        String senha = "senha";

        Usuario usuario = Usuario.builder().email(email).senha(senha).id(1L).build();
        Mockito.when(repository.findByEmail(email)).thenReturn(Optional.of(usuario));

        // ação
        Usuario autenticarResult = service.autenticar(email, senha);

        // verificação
        assertThat(autenticarResult).isNotNull();
    }

    @Test
    public void erroAoAutenticarUsuario() {
        // cenário
        Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());

        // ação
        Throwable throwable = catchThrowable(() -> service.autenticar("email@email.com", "senha"));

        // verificação
        assertThat(throwable).isInstanceOf(ErroAutenticacaoException.class).hasMessage("Usuário não encontrado!");
    }

    @Test
    public void erroAoAutenticarUsuarioSenhaInvalida() {
        // cenário
        String email = "email@teste.com";
        String senha = "senha";

        Usuario usuario = Usuario.builder().email(email).senha(senha).build();

        Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(usuario));

        // ação
        Throwable throwable = catchThrowable(() -> service.autenticar(email, "123"));

        // verificação
        assertThat(throwable).isInstanceOf(ErroAutenticacaoException.class).hasMessage("Senha inválida!");
    }

    @Test(expected = Test.None.class) // espera que não lança exceção
    public void deveSalvarUsuario() {
        // cenário
        Mockito.doNothing().when(service).validarEmail(Mockito.anyString());
        Usuario usuario = Usuario.builder().id(1L).nome("user").email("email").senha("senha").build();
        Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(usuario);

        // ação
        Usuario result = service.salvarUsuario(usuario);

        // verificação
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNome()).isEqualTo("user");
        assertThat(result.getEmail()).isEqualTo("email");
        assertThat(result.getSenha()).isEqualTo("senha");
    }

    @Test(expected = RegraNegocioException.class) // espera que lance exceção
    public void erroAoSalvarUsuario() {
        // cenário
        String email = "email@email.com";
        Usuario usuario = Usuario.builder().email(email).build();
        Mockito.doThrow(RegraNegocioException.class).when(service).validarEmail(email);

        // ação
        service.salvarUsuario(usuario);

        // verificação
        Mockito.verify(repository, Mockito.never()).save(usuario); // espera que nunca tenha chamado o método save
    }

}