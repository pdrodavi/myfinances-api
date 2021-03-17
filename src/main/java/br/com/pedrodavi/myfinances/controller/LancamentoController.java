package br.com.pedrodavi.myfinances.controller;

import br.com.pedrodavi.myfinances.exception.RegraNegocioException;
import br.com.pedrodavi.myfinances.model.Lancamento;
import br.com.pedrodavi.myfinances.model.Usuario;
import br.com.pedrodavi.myfinances.model.dto.AtualizaStatusDTO;
import br.com.pedrodavi.myfinances.model.dto.LancamentoDTO;
import br.com.pedrodavi.myfinances.model.enums.StatusLancamento;
import br.com.pedrodavi.myfinances.model.enums.TipoLancamento;
import br.com.pedrodavi.myfinances.service.LancamentoService;
import br.com.pedrodavi.myfinances.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/lancamentos")
@RequiredArgsConstructor
public class LancamentoController {

    private final LancamentoService service;
    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity buscar (
            @RequestParam(value = "descricao", required = false) String descricao,
            @RequestParam(value = "mes", required = false) Integer mes,
            @RequestParam(value = "ano", required = false) Integer ano,
            @RequestParam("usuario") Long idUsuario) {

        Lancamento lancamentoFiltro = new Lancamento();
        lancamentoFiltro.setDescricao(descricao);
        lancamentoFiltro.setMes(mes);
        lancamentoFiltro.setAno(ano);

        Optional<Usuario> usuario = usuarioService.obterPorId(idUsuario);

        if (usuario.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuário não encontrado para consulta!");
        } else {
            lancamentoFiltro.setUsuario(usuario.get());
        }

        List<Lancamento> lancamentos = service.buscar(lancamentoFiltro);
        return ResponseEntity.ok(lancamentos);
    }

    @PostMapping
    public ResponseEntity salvar (@RequestBody LancamentoDTO lancamentoDTO) {
        try {
            Lancamento entidade = fromDTO(lancamentoDTO);
            entidade = service.salvar(entidade);
            return new ResponseEntity(entidade, HttpStatus.CREATED);
        } catch (RegraNegocioException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("{id}")
    public ResponseEntity atualizar (@PathVariable("id") Long id, @RequestBody LancamentoDTO dto) {
        return service.obterPorId(id).map( entity -> {
            try{
                Lancamento lancamento = fromDTO(dto);
                lancamento.setId(entity.getId());
                service.atualizar(lancamento);
                return ResponseEntity.ok(lancamento);
            } catch (RegraNegocioException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }).orElseGet(() -> new ResponseEntity("Lançamento não encontrado no banco de dados!", HttpStatus.BAD_REQUEST));
    }

    @PutMapping("/atualizar-status/{id}")
    public ResponseEntity atualizarStatus(@PathVariable("id") Long id, @RequestBody AtualizaStatusDTO dto) {
        return service.obterPorId(id).map( entity -> {

            StatusLancamento statusLancamento = StatusLancamento.valueOf(dto.getStatus());

            if (statusLancamento == null) {
                return ResponseEntity.badRequest().body("Status inválido!");
            }

            try {
                entity.setStatus(statusLancamento);
                service.atualizar(entity);
                return ResponseEntity.ok(entity);
            } catch (RegraNegocioException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }).orElseGet(() -> new ResponseEntity("Lançamento não encontrado no banco de dados!", HttpStatus.BAD_REQUEST));
    }

    @DeleteMapping("{id}")
    public ResponseEntity deletar (@PathVariable("id") Long id){
        return service.obterPorId(id).map( entity -> {
            service.deletar(entity);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }).orElseGet(() -> new ResponseEntity("Lançamento não encontrado no banco de dados!", HttpStatus.BAD_REQUEST));
    }
    
    private Lancamento fromDTO(LancamentoDTO dto) {

        Lancamento lancamento = new Lancamento();
        lancamento.setId(dto.getId());
        lancamento.setDescricao(dto.getDescricao());
        lancamento.setAno(dto.getAno());
        lancamento.setMes(dto.getMes());
        lancamento.setValor(dto.getValor());
        Usuario usuario = usuarioService.obterPorId(dto.getUsuario())
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado para o ID informado!"));
        lancamento.setUsuario(usuario);

        if (dto.getTipo() != null || dto.getTipo().equals("DESPESA") || dto.getTipo().equals("RECEITA")) {
            lancamento.setTipo(TipoLancamento.valueOf(dto.getTipo()));
        }

        if (dto.getStatus() != null) {
            lancamento.setStatus(StatusLancamento.valueOf(dto.getStatus()));
        }

        return lancamento;
    }

}
