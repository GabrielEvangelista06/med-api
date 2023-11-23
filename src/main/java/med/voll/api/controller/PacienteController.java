package med.voll.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import med.voll.api.domain.paciente.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("pacientes")
@SecurityRequirement(name = "bearer-key")
public class PacienteController
{

	@Autowired
	private PacienteService service;

	@PostMapping
	@Transactional
	public ResponseEntity<DadosDetalhamentoPaciente> cadastrar(@RequestBody @Valid DadosCadastroPaciente dados,
			UriComponentsBuilder uriBuilder)
	{
		DadosDetalhamentoPaciente paciente = this.service.cadastrar(dados);

		URI uri = uriBuilder.path("/pacientes/{id}").buildAndExpand(paciente.id()).toUri();
		return ResponseEntity.created(uri).body(paciente);
	}

	@GetMapping
	public ResponseEntity<Page<DadosListagemPaciente>> listar(@PageableDefault(size = 10, sort = { "nome" }) Pageable paginacao)
	{
		return ResponseEntity.ok(this.service.listar(paginacao));
	}

	@PutMapping
	@Transactional
	public ResponseEntity<DadosDetalhamentoPaciente> atualizar(@RequestBody @Valid DadosAtualizacaoPaciente dados)
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
	public ResponseEntity<DadosDetalhamentoPaciente> detalhar(@PathVariable Long id)
	{
		return ResponseEntity.ok(this.service.detalhar(id));
	}

}
