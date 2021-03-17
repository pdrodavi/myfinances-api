package br.com.pedrodavi.myfinances.repository;

import br.com.pedrodavi.myfinances.model.Usuario;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UsuarioRepositoryTest {

    @Autowired
    UsuarioRepository repository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    public void deveVerificarSeExisteEmail() {
        // cenário
        Usuario usuario = Usuario.builder().nome("usuario").email("usuario@email.com").build();
        entityManager.persist(usuario);

        // ação
        boolean existsByEmail = repository.existsByEmail(usuario.getEmail());

        // verificação
        assertThat(existsByEmail).isTrue();
    }

    @Test
    public void deveRetornarFalsoQuandoNaoExistirEmailCadastrado() {
        // ação
        boolean existsByEmail = repository.existsByEmail("usuario@email.com");

        // verificação
        assertThat(existsByEmail).isFalse();
    }

    @Test
    public void devePersistirUsuarioNaBase() {
        // cenário
        Usuario usuario = Usuario.builder()
                .nome("usuario")
                .email("usuario@email.com")
                .senha("senha")
                .build();

        // ação
        Usuario result = repository.save(usuario);

        // verificação
        assertThat(result.getId()).isNotNull();
    }

    @Test
    public void deveBuscarUsuarioPorEmail() {
        // cenário
        Usuario usuario = Usuario.builder()
                .nome("usuario")
                .email("usuario@email.com")
                .senha("senha")
                .build();

        // ação
        entityManager.persist(usuario);

        // verificação
        Optional<Usuario> result = repository.findByEmail(usuario.getEmail());

        assertThat(result).isPresent();
    }

    @Test
    public void deveRetornarVazioAoBuscarUsuarioPorEmailQuandoNaoExistirNaBase() {
        // verificação
        Optional<Usuario> result = repository.findByEmail("usuario@email.com");
        assertThat(result).isEmpty();
    }



}