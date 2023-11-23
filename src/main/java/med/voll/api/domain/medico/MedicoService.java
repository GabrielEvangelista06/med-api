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
		Medico medico = new Medico(dados);
		this.repository.save(medico);

		return new DadosDetalhamentoMedico(medico);
	}

	public Page<DadosListagemMedico> listar(Pageable paginacao)
	{
		return this.repository.findAllByAtivoTrue(paginacao).map(DadosListagemMedico::new);
	}

	public DadosDetalhamentoMedico atualizar(DadosAtualizacaoMedico dados)
	{
		Medico medico = this.repository.getReferenceById(dados.id());
		medico.atualizarInformacoes(dados);

		return new DadosDetalhamentoMedico(medico);
	}

	public void excluir(Long id)
	{
		Medico medico = this.repository.getReferenceById(id);
		medico.excluir();
	}

	public DadosDetalhamentoMedico detalhar(Long id)
	{
		return new DadosDetalhamentoMedico(this.repository.getReferenceById(id));
	}
}
