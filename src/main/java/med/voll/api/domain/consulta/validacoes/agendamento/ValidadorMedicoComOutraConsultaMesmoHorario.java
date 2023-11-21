package med.voll.api.domain.consulta.validacoes.agendamento;

import med.voll.api.domain.ValidacaoException;
import med.voll.api.domain.consulta.ConsultaRepository;
import med.voll.api.domain.consulta.DadosAgendamentoConsulta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorMedicoComOutraConsultaMesmoHorario implements ValidadorAgendamentoConsulta
{
	@Autowired
	private ConsultaRepository repository;

	public void validar(DadosAgendamentoConsulta dados)
	{
		var medicoPossuiOutraConsultaMesmoHorario = this.repository.existsByMedicoIdAndDataAndMotivoCancelamentoIsNull(dados.idMedico(), dados.data());

		if (medicoPossuiOutraConsultaMesmoHorario)
		{
			throw new ValidacaoException("Médico já possuí outra consulta agendada nesse mesmo horário");
		}
	}
}
