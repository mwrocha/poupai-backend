package io.poupai.backend.investment.service;

import io.poupai.backend.gamification.service.GamificationService;
import io.poupai.backend.investment.dto.InvestmentDtos;
import io.poupai.backend.investment.model.Investment;
import io.poupai.backend.investment.repository.InvestmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final GamificationService gamificationService;

    public List<InvestmentDtos.InvestmentResponse> getAll(String userId) {
        return investmentRepository.findByUserId(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public InvestmentDtos.InvestmentResponse create(String userId, InvestmentDtos.CreateRequest request) {
        double profitability = request.getInvestedValue() > 0
                ? ((request.getCurrentValue() - request.getInvestedValue()) / request.getInvestedValue()) * 100
                : 0.0;

        Investment investment = Investment.builder()
                .userId(userId).name(request.getName()).type(request.getType())
                .currentValue(request.getCurrentValue()).investedValue(request.getInvestedValue())
                .profitability(profitability).build();

        InvestmentDtos.InvestmentResponse response = toResponse(investmentRepository.save(investment));

        // ─── Gamificação ───
        gamificationService.onInvestmentCreated(userId);

        return response;
    }

    public InvestmentDtos.InvestmentResponse update(String userId, String investmentId, InvestmentDtos.UpdateRequest request) {
        Investment investment = investmentRepository.findById(investmentId)
                .orElseThrow(() -> new RuntimeException("Investimento não encontrado"));
        if (!investment.getUserId().equals(userId))
            throw new RuntimeException("Sem permissão para editar este investimento");

        if (request.getCurrentValue() != null) investment.setCurrentValue(request.getCurrentValue());
        if (request.getInvestedValue() != null) investment.setInvestedValue(request.getInvestedValue());

        double profitability = investment.getInvestedValue() > 0
                ? ((investment.getCurrentValue() - investment.getInvestedValue()) / investment.getInvestedValue()) * 100
                : 0.0;
        investment.setProfitability(profitability);

        return toResponse(investmentRepository.save(investment));
    }

    public void delete(String userId, String investmentId) {
        Investment investment = investmentRepository.findById(investmentId)
                .orElseThrow(() -> new RuntimeException("Investimento não encontrado"));
        if (!investment.getUserId().equals(userId))
            throw new RuntimeException("Sem permissão para deletar este investimento");
        investmentRepository.delete(investment);
    }

    private InvestmentDtos.InvestmentResponse toResponse(Investment i) {
        return InvestmentDtos.InvestmentResponse.builder()
                .id(i.getId()).name(i.getName()).type(i.getType().name())
                .currentValue(i.getCurrentValue()).investedValue(i.getInvestedValue())
                .profitability(i.getProfitability()).build();
    }
}