package med.voll.api.domain.consulta.validacoes.agendamento;

import med.voll.api.domain.ValidacaoException;
import med.voll.api.domain.consulta.DadosAgendamentoConsulta;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Component
public class ValidadorHorarioFuncionamentoClinica implements ValidadorAgendamentoConsulta
{
	public void validar(DadosAgendamentoConsulta dados)
	{
		LocalDateTime dataConsulta = dados.data();
		boolean domingo = dataConsulta.getDayOfWeek().equals(DayOfWeek.SUNDAY);
		boolean antesDaAberturaDaClinica = dataConsulta.getHour() < 7;
		boolean depoisDoEncerrmanetoDaClinica = dataConsulta.getHour() > 18;

		if (domingo || antesDaAberturaDaClinica || depoisDoEncerrmanetoDaClinica)
		{
			throw new ValidacaoException("Consulta fora do horário de funcionamento da clínica");
		}
	}
}
