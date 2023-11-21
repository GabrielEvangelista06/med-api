package med.voll.api.domain.medico;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MedicoService
{
	@Autowired
	private MedicoRepository repository;

	public DadosDetalhamentoMedico cadastrar(DadosCadastroMedico dados)
	{
		var medico = new Medico(dados);
		repository.save(medico);

		return new DadosDetalhamentoMedico(medico);
	}

	public Page<DadosListagemMedico> listar(Pageable paginacao)
	{
		return repository.findAllByAtivoTrue(paginacao).map(DadosListagemMedico::new);
	}

	public DadosDetalhamentoMedico atualizar(DadosAtualizacaoMedico dados)
	{
		var medico = repository.getReferenceById(dados.id());
		medico.atualizarInformacoes(dados);

		return new DadosDetalhamentoMedico(medico);
	}

	public void excluir(Long id)
	{
		var medico = repository.getReferenceById(id);
		medico.excluir();
	}

	public DadosDetalhamentoMedico detalhar(Long id)
	{
		Medico medico = repository.getReferenceById(id);

		return new DadosDetalhamentoMedico(medico);
	}
}
