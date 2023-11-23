package med.voll.api.domain.paciente;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PacienteService
{
	@Autowired
	private PacienteRepository repository;

	public DadosDetalhamentoPaciente cadastrar(DadosCadastroPaciente dados)
	{
		Paciente paciente = new Paciente(dados);
		repository.save(paciente);

		return new DadosDetalhamentoPaciente(paciente);
	}

	public Page<DadosListagemPaciente> listar(Pageable paginacao)
	{
		return this.repository.findAllByAtivoTrue(paginacao).map(DadosListagemPaciente::new);
	}

	public DadosDetalhamentoPaciente atualizar(DadosAtualizacaoPaciente dados)
	{
		Paciente paciente = repository.getReferenceById(dados.id());
		paciente.atualizarInformacoes(dados);

		return new DadosDetalhamentoPaciente(paciente);
	}

	public void excluir(Long id)
	{
		Paciente paciente = repository.getReferenceById(id);
		paciente.excluir();
	}

	public DadosDetalhamentoPaciente detalhar(Long id)
	{
		return new DadosDetalhamentoPaciente(repository.getReferenceById(id));
	}
}
