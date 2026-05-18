package io.poupai.backend.investment.service;

import io.poupai.backend.gamification.service.GamificationService;
import io.poupai.backend.investment.dto.InvestmentDtos;
import io.poupai.backend.investment.model.Dividend;
import io.poupai.backend.investment.model.Investment;
import io.poupai.backend.investment.model.InvestmentEntry;
import io.poupai.backend.investment.repository.DividendRepository;
import io.poupai.backend.investment.repository.InvestmentEntryRepository;
import io.poupai.backend.investment.repository.InvestmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final InvestmentEntryRepository entryRepository;
    private final DividendRepository dividendRepository;
    private final GamificationService gamificationService;

    private Double cachedCdiYear = null;
    private LocalDate cdiLastFetched = null;

    // ─── CRUD ATIVO ───

    public List<InvestmentDtos.InvestmentResponse> getAll(String userId) {
        return investmentRepository.findByUserId(userId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public InvestmentDtos.InvestmentResponse create(String userId, InvestmentDtos.CreateRequest request) {
        double shares = request.getShares() != null ? request.getShares() : 0.0;
        double avgPrice = (shares > 0 && request.getInvestedValue() > 0) ? request.getInvestedValue() / shares : 0.0;
        double profitability = request.getInvestedValue() > 0 ? ((request.getCurrentValue() - request.getInvestedValue()) / request.getInvestedValue()) * 100 : 0.0;

        Investment investment = Investment.builder().userId(userId).name(request.getName()).type(request.getType()).currentValue(request.getCurrentValue()).investedValue(request.getInvestedValue()).profitability(profitability).shares(shares).averagePrice(avgPrice).allocationTarget(request.getAllocationTarget() != null ? request.getAllocationTarget() : 0.0).build();

        Investment saved = investmentRepository.save(investment);

        if (shares > 0 && avgPrice > 0) {
            entryRepository.save(InvestmentEntry.builder().userId(userId).investmentId(saved.getId()).investmentName(saved.getName()).type(InvestmentEntry.EntryType.APORTE).shares(shares).sharePrice(avgPrice).totalValue(request.getInvestedValue()).previousShares(0.0).previousAveragePrice(0.0).newAveragePrice(avgPrice).newTotalShares(shares).date(LocalDate.now()).notes("Aporte inicial").build());
        }

        gamificationService.onInvestmentCreated(userId);
        return toResponse(saved);
    }

    public InvestmentDtos.InvestmentResponse update(String userId, String id, InvestmentDtos.UpdateRequest request) {
        Investment investment = investmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Investimento não encontrado"));
        if (!investment.getUserId().equals(userId)) throw new RuntimeException("Sem permissão");

        if (request.getName() != null && !request.getName().isBlank()) investment.setName(request.getName());
        if (request.getAllocationTarget() != null) investment.setAllocationTarget(request.getAllocationTarget());

        if (request.getShares() != null) investment.setShares(request.getShares());
        if (request.getAveragePrice() != null) investment.setAveragePrice(request.getAveragePrice());
        if (request.getInvestedValue() != null) investment.setInvestedValue(request.getInvestedValue());

        if (investment.getShares() != null && investment.getShares() > 0 && investment.getAveragePrice() != null && investment.getAveragePrice() > 0) {
            investment.setCurrentValue(investment.getShares() * investment.getAveragePrice());
        } else if (request.getCurrentValue() != null) {
            investment.setCurrentValue(request.getCurrentValue());
        }

        double profitability = investment.getInvestedValue() > 0 ? ((investment.getCurrentValue() - investment.getInvestedValue()) / investment.getInvestedValue()) * 100 : 0.0;
        investment.setProfitability(profitability);

        return toResponse(investmentRepository.save(investment));
    }

    public void delete(String userId, String id) {
        Investment investment = investmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Investimento não encontrado"));
        if (!investment.getUserId().equals(userId)) throw new RuntimeException("Sem permissão");
        investmentRepository.delete(investment);
    }

    // ─── LIVRO CONTÁBIL ───

    public InvestmentDtos.EntrySummaryResponse getEntries(String userId, String investmentId, Integer year, Integer month) {
        List<InvestmentEntry> entries;
        if (year != null && month != null) {
            YearMonth ym = YearMonth.of(year, month);
            entries = investmentId != null && !investmentId.isBlank() ? entryRepository.findByUserIdAndInvestmentIdAndDateBetweenOrderByDateDesc(userId, investmentId, ym.atDay(1), ym.atEndOfMonth()) : entryRepository.findByUserIdAndDateBetweenOrderByDateDesc(userId, ym.atDay(1), ym.atEndOfMonth());
        } else if (investmentId != null && !investmentId.isBlank()) {
            entries = entryRepository.findByUserIdAndInvestmentIdOrderByDateDesc(userId, investmentId);
        } else {
            entries = entryRepository.findByUserIdOrderByDateDesc(userId);
        }

        double totalAported = entries.stream().filter(e -> e.getType() == InvestmentEntry.EntryType.APORTE).mapToDouble(InvestmentEntry::getTotalValue).sum();
        double totalRescued = entries.stream().filter(e -> e.getType() == InvestmentEntry.EntryType.RESGATE).mapToDouble(InvestmentEntry::getTotalValue).sum();

        return InvestmentDtos.EntrySummaryResponse.builder().entries(entries.stream().map(this::toEntryResponse).collect(Collectors.toList())).totalAported(totalAported).totalRescued(totalRescued).totalEntries((long) entries.size()).build();
    }

    public InvestmentDtos.EntryResponse addEntry(String userId, InvestmentDtos.CreateEntryRequest request) {
        Investment investment = investmentRepository.findById(request.getInvestmentId()).orElseThrow(() -> new RuntimeException("Investimento não encontrado"));
        if (!investment.getUserId().equals(userId)) throw new RuntimeException("Sem permissão");

        double prevShares = investment.getShares() != null ? investment.getShares() : 0.0;
        double prevAvgPrice = investment.getAveragePrice() != null ? investment.getAveragePrice() : 0.0;
        double newAvgPrice = prevAvgPrice;
        double newTotalShares = prevShares;
        double totalValue = 0.0;

        InvestmentEntry.InvestmentEntryBuilder entryBuilder = InvestmentEntry.builder().userId(userId).investmentId(investment.getId()).investmentName(investment.getName()).type(request.getType()).previousShares(prevShares).previousAveragePrice(prevAvgPrice).notes(request.getNotes()).date(LocalDate.parse(request.getDate()));

        switch (request.getType()) {

            case APORTE -> {
                double shares = request.getShares() != null ? request.getShares() : 0.0;
                double price = request.getSharePrice() != null ? request.getSharePrice() : 0.0;
                totalValue = shares * price;
                newTotalShares = prevShares + shares;

                // ─── Fórmula de preço médio ponderado ───
                newAvgPrice = newTotalShares > 0 ? (prevAvgPrice * prevShares + price * shares) / newTotalShares : 0.0;

                investment.setShares(newTotalShares);
                investment.setAveragePrice(newAvgPrice);
                investment.setInvestedValue(investment.getInvestedValue() + totalValue);

                entryBuilder.shares(shares).sharePrice(price).totalValue(totalValue).newAveragePrice(newAvgPrice).newTotalShares(newTotalShares);
            }

            case RESGATE -> {
                double shares = request.getShares() != null ? request.getShares() : 0.0;
                double price = request.getSharePrice() != null ? request.getSharePrice() : 0.0;
                totalValue = shares * price;
                newTotalShares = Math.max(0, prevShares - shares);
                // PM não muda no resgate
                investment.setShares(newTotalShares);
                investment.setInvestedValue(Math.max(0, investment.getInvestedValue() - (prevAvgPrice * shares)));

                entryBuilder.shares(shares).sharePrice(price).totalValue(totalValue).newAveragePrice(prevAvgPrice).newTotalShares(newTotalShares);
            }

            case ATUALIZACAO_VALOR -> {
                if (request.getNewCurrentValue() != null) {
                    double newCurrentValue;
                    // If asset has shares, input is price per share — calculate total position
                    if (investment.getShares() != null && investment.getShares() > 0) {
                        newCurrentValue = request.getNewCurrentValue() * investment.getShares();
                    } else {
                        // No shares (CDB, crypto etc) — input is total position value directly
                        newCurrentValue = request.getNewCurrentValue();
                    }
                    totalValue = newCurrentValue;
                    double profSnap = investment.getInvestedValue() > 0 ? ((newCurrentValue - investment.getInvestedValue()) / investment.getInvestedValue()) * 100 : 0.0;
                    investment.getHistory().add(Investment.ProfitabilitySnapshot.builder().date(request.getDate()).value(newCurrentValue).invested(investment.getInvestedValue()).profitability(profSnap).build());
                    investment.setCurrentValue(newCurrentValue);
                }
                entryBuilder.totalValue(totalValue).newAveragePrice(prevAvgPrice).newTotalShares(prevShares);
            }

        }

        // Recalcula rentabilidade
        if (investment.getInvestedValue() > 0) {
            investment.setProfitability(((investment.getCurrentValue() - investment.getInvestedValue()) / investment.getInvestedValue()) * 100);
        }
        investmentRepository.save(investment);

        return toEntryResponse(entryRepository.save(entryBuilder.build()));
    }

    public void deleteEntry(String userId, String entryId) {
        InvestmentEntry entry = entryRepository.findById(entryId).orElseThrow(() -> new RuntimeException("Lançamento não encontrado"));
        if (!entry.getUserId().equals(userId)) throw new RuntimeException("Sem permissão");

        Investment investment = investmentRepository.findById(entry.getInvestmentId()).orElse(null);
        if (investment != null) {
            switch (entry.getType()) {
                case APORTE -> {
                    investment.setShares(entry.getPreviousShares());
                    investment.setAveragePrice(entry.getPreviousAveragePrice());
                    investment.setInvestedValue(Math.max(0, investment.getInvestedValue() - entry.getTotalValue()));
                    investmentRepository.save(investment);
                }
                case RESGATE -> {
                    investment.setShares(entry.getPreviousShares());
                    investment.setInvestedValue(investment.getInvestedValue() + (entry.getPreviousAveragePrice() * entry.getShares()));
                    investmentRepository.save(investment);
                }
                default -> { /* ATUALIZACAO_VALOR não reverte */ }
            }
        }

        entryRepository.delete(entry);
    }

    // ─── DIVIDENDOS ───

    public InvestmentDtos.DividendSummaryResponse getDividends(String userId, Integer year, Integer month) {
        List<Dividend> dividends;
        if (year != null && month != null) {
            YearMonth ym = YearMonth.of(year, month);
            dividends = dividendRepository.findByUserIdAndDateBetweenOrderByDateDesc(userId, ym.atDay(1), ym.atEndOfMonth());
        } else {
            dividends = dividendRepository.findByUserIdOrderByDateDesc(userId);
        }

        LocalDate now = LocalDate.now();
        double totalReceived = dividends.stream().mapToDouble(Dividend::getAmount).sum();
        double thisYear = dividends.stream().filter(d -> d.getDate().getYear() == now.getYear()).mapToDouble(Dividend::getAmount).sum();
        double thisMonth = dividends.stream().filter(d -> d.getDate().getYear() == now.getYear() && d.getDate().getMonthValue() == now.getMonthValue()).mapToDouble(Dividend::getAmount).sum();

        return InvestmentDtos.DividendSummaryResponse.builder().dividends(dividends.stream().map(this::toDividendResponse).collect(Collectors.toList())).totalReceived(totalReceived).totalReceivedThisYear(thisYear).totalReceivedThisMonth(thisMonth).projectedAnnual(computeProjectedAnnual(userId)).build();
    }

    public InvestmentDtos.DividendResponse addDividend(String userId, InvestmentDtos.CreateDividendRequest request) {
        Investment investment = investmentRepository.findById(request.getInvestmentId()).orElseThrow(() -> new RuntimeException("Investimento não encontrado"));
        if (!investment.getUserId().equals(userId)) throw new RuntimeException("Sem permissão");

        double yieldPercent = investment.getInvestedValue() > 0 ? (request.getAmount() / investment.getInvestedValue()) * 100 : 0.0;

        Dividend dividend = Dividend.builder().userId(userId).investmentId(investment.getId()).investmentName(investment.getName()).amount(request.getAmount()).yieldPercent(yieldPercent).date(LocalDate.parse(request.getDate())).type(request.getType()).build();

        return toDividendResponse(dividendRepository.save(dividend));
    }

    public void deleteDividend(String userId, String dividendId) {
        Dividend dividend = dividendRepository.findById(dividendId).orElseThrow(() -> new RuntimeException("Dividendo não encontrado"));
        if (!dividend.getUserId().equals(userId)) throw new RuntimeException("Sem permissão");
        dividendRepository.delete(dividend);
    }

    // ─── REBALANCEAMENTO ───

    public InvestmentDtos.RebalanceResponse getRebalance(String userId) {
        List<Investment> investments = investmentRepository.findByUserId(userId);
        double total = investments.stream().mapToDouble(Investment::getCurrentValue).sum();

        List<InvestmentDtos.RebalanceItem> items = investments.stream().map(inv -> {
            double currentPercent = total > 0 ? (inv.getCurrentValue() / total) * 100 : 0.0;
            double targetPercent = inv.getAllocationTarget() != null ? inv.getAllocationTarget() : 0.0;
            double difference = currentPercent - targetPercent;
            String action = Math.abs(difference) < 1.0 ? "OK" : difference > 0 ? "VENDER" : "COMPRAR";
            return InvestmentDtos.RebalanceItem.builder().investmentId(inv.getId()).name(inv.getName()).type(inv.getType().name()).currentValue(inv.getCurrentValue()).currentPercent(currentPercent).targetPercent(targetPercent).difference(difference).action(action).amountToAdjust(Math.abs(difference / 100.0 * total)).build();
        }).collect(Collectors.toList());

        return InvestmentDtos.RebalanceResponse.builder().items(items).totalCurrentValue(total).build();
    }

    // ─── BENCHMARK ───

    public InvestmentDtos.BenchmarkResponse getBenchmark(String userId) {
        double cdiYear = fetchCdi();
        double cdiMonth = (Math.pow(1.0 + cdiYear / 100.0, 1.0 / 12.0) - 1.0) * 100.0;

        List<Investment> investments = investmentRepository.findByUserId(userId);
        double totalInvested = investments.stream().mapToDouble(Investment::getInvestedValue).sum();
        double totalCurrent = investments.stream().mapToDouble(Investment::getCurrentValue).sum();
        double portfolioReturn = totalInvested > 0 ? ((totalCurrent - totalInvested) / totalInvested) * 100 : 0.0;

        return InvestmentDtos.BenchmarkResponse.builder().cdiRateYear(cdiYear).cdiRateMonth(cdiMonth).portfolioReturn(portfolioReturn).vsCdi(portfolioReturn - cdiYear).lastUpdated(cdiLastFetched != null ? cdiLastFetched.toString() : "N/A").build();
    }

    @SuppressWarnings("unchecked")
    private double fetchCdi() {
        if (cachedCdiYear != null && LocalDate.now().equals(cdiLastFetched)) return cachedCdiYear;
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.bcb.gov.br/dados/serie/bcdata.sgs.4389/dados/ultimos/1?formato=json";
            List<Map<String, String>> response = restTemplate.getForObject(url, List.class);
            if (response != null && !response.isEmpty()) {
                cachedCdiYear = Double.parseDouble(response.get(0).get("valor").replace(",", "."));
                cdiLastFetched = LocalDate.now();
                return cachedCdiYear;
            }
        } catch (Exception ignored) {
            if (cachedCdiYear != null) return cachedCdiYear;
        }
        return 10.5; // fallback
    }

    private double computeProjectedAnnual(String userId) {
        LocalDate now = LocalDate.now();
        List<Dividend> recent = dividendRepository.findByUserIdAndDateBetweenOrderByDateDesc(userId, now.minusMonths(3), now);
        return (recent.stream().mapToDouble(Dividend::getAmount).sum() / 3.0) * 12;
    }

    // ─── Histórico do portfólio ───

    public InvestmentDtos.PortfolioHistoryResponse getPortfolioHistory(String userId) {
        List<Investment> investments = investmentRepository.findByUserId(userId);

        Set<String> allMonths = new TreeSet<>();
        for (Investment inv : investments) {
            if (inv.getHistory() != null) {
                inv.getHistory().forEach(s -> allMonths.add(s.getDate().substring(0, 7)));
            }
        }

        List<InvestmentDtos.PortfolioHistorySnapshotResponse> result = new ArrayList<>();
        for (String month : allMonths) {
            LocalDate monthEnd = YearMonth.parse(month).atEndOfMonth();
            double totalValue = 0.0;
            double totalInvested = 0.0;
            boolean hasData = false;

            for (Investment inv : investments) {
                if (inv.getHistory() == null) continue;
                java.util.Optional<Investment.ProfitabilitySnapshot> snap = inv.getHistory().stream()
                        .filter(s -> !LocalDate.parse(s.getDate()).isAfter(monthEnd))
                        .max(Comparator.comparing(s -> LocalDate.parse(s.getDate())));
                if (snap.isPresent()) {
                    totalValue += snap.get().getValue();
                    totalInvested += snap.get().getInvested();
                    hasData = true;
                }
            }

            if (hasData) {
                double profitability = totalInvested > 0 ? ((totalValue - totalInvested) / totalInvested) * 100 : 0.0;
                result.add(InvestmentDtos.PortfolioHistorySnapshotResponse.builder()
                        .date(month)
                        .totalValue(totalValue)
                        .totalInvested(totalInvested)
                        .profitability(profitability)
                        .build());
            }
        }

        return InvestmentDtos.PortfolioHistoryResponse.builder().history(result).build();
    }

    // ─── Mappers ───

    private InvestmentDtos.InvestmentResponse toResponse(Investment i) {
        List<InvestmentDtos.SnapshotResponse> history = i.getHistory() == null ? List.of() : i.getHistory().stream().map(s -> InvestmentDtos.SnapshotResponse.builder().date(s.getDate()).value(s.getValue()).invested(s.getInvested()).profitability(s.getProfitability()).build()).collect(Collectors.toList());

        LocalDate today = LocalDate.now();
        return InvestmentDtos.InvestmentResponse.builder()
                .id(i.getId()).name(i.getName()).type(i.getType().name())
                .currentValue(i.getCurrentValue()).investedValue(i.getInvestedValue())
                .profitability(i.getProfitability()).shares(i.getShares())
                .averagePrice(i.getAveragePrice()).allocationTarget(i.getAllocationTarget())
                .history(history)
                .return1M(computeWindowReturn(i, today.minusDays(30)))
                .return3M(computeWindowReturn(i, today.minusDays(90)))
                .return6M(computeWindowReturn(i, today.minusDays(180)))
                .returnYtd(computeWindowReturn(i, LocalDate.of(today.getYear(), 1, 1)))
                .return12M(computeWindowReturn(i, today.minusDays(365)))
                .returnAll(computeAllTimeReturn(i))
                .build();
    }

    private Double computeWindowReturn(Investment investment, LocalDate from) {
        if (investment.getCurrentValue() == null || investment.getHistory() == null || investment.getHistory().isEmpty()) return null;
        return investment.getHistory().stream()
                .filter(s -> !LocalDate.parse(s.getDate()).isAfter(from))
                .max(Comparator.comparing(s -> LocalDate.parse(s.getDate())))
                .map(s -> s.getValue() != null && s.getValue() > 0
                        ? ((investment.getCurrentValue() - s.getValue()) / s.getValue()) * 100
                        : null)
                .orElse(null);
    }

    private Double computeAllTimeReturn(Investment investment) {
        if (investment.getCurrentValue() == null || investment.getHistory() == null || investment.getHistory().isEmpty()) return null;
        return investment.getHistory().stream()
                .min(Comparator.comparing(s -> LocalDate.parse(s.getDate())))
                .map(s -> s.getValue() != null && s.getValue() > 0
                        ? ((investment.getCurrentValue() - s.getValue()) / s.getValue()) * 100
                        : null)
                .orElse(null);
    }

    private InvestmentDtos.EntryResponse toEntryResponse(InvestmentEntry e) {
        return InvestmentDtos.EntryResponse.builder().id(e.getId()).investmentId(e.getInvestmentId()).investmentName(e.getInvestmentName()).type(e.getType().name()).shares(e.getShares()).sharePrice(e.getSharePrice()).totalValue(e.getTotalValue()).previousShares(e.getPreviousShares()).previousAveragePrice(e.getPreviousAveragePrice()).newAveragePrice(e.getNewAveragePrice()).newTotalShares(e.getNewTotalShares()).adjustedShares(e.getAdjustedShares()).adjustedAveragePrice(e.getAdjustedAveragePrice()).notes(e.getNotes()).date(e.getDate() != null ? e.getDate().toString() : null).build();
    }

    private InvestmentDtos.DividendResponse toDividendResponse(Dividend d) {
        return InvestmentDtos.DividendResponse.builder().id(d.getId()).investmentId(d.getInvestmentId()).investmentName(d.getInvestmentName()).amount(d.getAmount()).yieldPercent(d.getYieldPercent()).date(d.getDate().toString()).type(d.getType().name()).build();
    }
}