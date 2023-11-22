package med.voll.api.domain.consulta.validacoes.agendamento;

import med.voll.api.domain.ValidacaoException;
import med.voll.api.domain.consulta.DadosAgendamentoConsulta;
import med.voll.api.domain.medico.MedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorMedicoAtivo implements ValidadorAgendamentoConsulta
{
	@Autowired
	private MedicoRepository repository;

	public void validar(DadosAgendamentoConsulta dados)
	{
		if (dados.idMedico() == null)
		{
			return;
		}

		boolean medicoEstaAtivo = this.repository.findAtivoById(dados.idMedico());

		if (!medicoEstaAtivo)
		{
			throw new ValidacaoException("Consulta não pode ser agendada com médico que não trabalha mais na clínica.");
		}
	}
}
