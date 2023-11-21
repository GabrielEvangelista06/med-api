package med.voll.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import med.voll.api.domain.medico.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("medicos")
@SecurityRequirement(name = "bearer-key")
public class MedicoController
{

	@Autowired
	private MedicoService service;

	@PostMapping
	@Transactional
	public ResponseEntity cadastrar(@RequestBody @Valid DadosCadastroMedico dados, UriComponentsBuilder uriBuilder)
	{
		var medico = this.service.cadastrar(dados);

		var uri = uriBuilder.path("/medicos/{id}").buildAndExpand(medico.id()).toUri();

		return ResponseEntity.created(uri).body(medico);
	}

	@GetMapping
	public ResponseEntity<Page<DadosListagemMedico>> listar(@PageableDefault(size = 10, sort = { "nome" }) Pageable paginacao)
	{
		return ResponseEntity.ok(this.service.listar(paginacao));
	}

	@PutMapping
	@Transactional
	public ResponseEntity<DadosDetalhamentoMedico> atualizar(@RequestBody @Valid DadosAtualizacaoMedico dados)
	{
		return ResponseEntity.ok(this.service.atualizar(dados));
	}

	@DeleteMapping("/{id}")
	@Transactional
	public ResponseEntity excluir(@PathVariable Long id)
	{
		this.service.excluir(id);

		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}")
	public ResponseEntity<DadosDetalhamentoMedico> detalhar(@PathVariable Long id)
	{
		return ResponseEntity.ok(this.service.detalhar(id));
	}

}
