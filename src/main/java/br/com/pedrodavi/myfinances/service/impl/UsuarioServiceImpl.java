package br.com.pedrodavi.myfinances.service.impl;

import br.com.pedrodavi.myfinances.exception.ErroAutenticacaoException;
import br.com.pedrodavi.myfinances.exception.RegraNegocioException;
import br.com.pedrodavi.myfinances.model.Usuario;
import br.com.pedrodavi.myfinances.repository.UsuarioRepository;
import br.com.pedrodavi.myfinances.service.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private UsuarioRepository repository;

    public UsuarioServiceImpl(UsuarioRepository repository) {
        super();
        this.repository = repository;
    }

    @Override
    public Usuario autenticar(String email, String senha) {

        Optional<Usuario> usuario = repository.findByEmail(email);

        if (usuario.isEmpty()) {
            throw new ErroAutenticacaoException("Usuário não encontrado!");
        }

        if (!usuario.get().getSenha().equals(senha)) {
            throw new ErroAutenticacaoException("Senha inválida!");
        }

        return usuario.get();
    }

    @Override
    @Transactional
    public Usuario salvarUsuario(Usuario usuario) {
        validarEmail(usuario.getEmail());
        return repository.save(usuario);
    }

    @Override
    public void validarEmail(String email) {
        boolean exists = repository.existsByEmail(email);
        if (exists) {
            throw new RegraNegocioException("Já existe um cadastro com este email!");
        }
    }

    @Override
    public Optional<Usuario> obterPorId(Long id) {
        return repository.findById(id);
    }
}
