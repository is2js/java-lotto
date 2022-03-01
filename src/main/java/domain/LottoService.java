package domain;

import domain.generator.AutoLottoGenerator;
import domain.generator.LottoGenerator;
import dto.LottoDto;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class LottoService {

    private static final int LOTTO_SIZE = 6;
    private static final int RANK_COUNT_UNIT = 1;
    private static final int RANK_COUNT_INIT_NUMBER = 0;
    private static final int INIT_WIN_PRICE = 0;
    private static final String ERROR_BONUS_NUMBER_CONTAIN_MESSAGE = "보너스 볼 번호가 지난 주 당첨 번호와 일치할 수 없습니다.";
    private static final String ERROR_LOTTO_SIZE_MESSAGE = "번호는 6개를 입력하셔야 합니다.";

    private final Money money;
    private Lotto lastWinLotto;
    private LottoNumber bonusNumber;
    private List<Lotto> issuedLotto;

    public LottoService(final int money) {
        this.money = new Money(money);
    }

    public void initLastWinLotto(final List<String> inputLotto) {
        validate(inputLotto);
        this.lastWinLotto = Lotto.fromInput(inputLotto);
    }

    private void validate(final List<String> inputLotto) {
        if (inputLotto.size() != LOTTO_SIZE) {
            throw new IllegalArgumentException(ERROR_LOTTO_SIZE_MESSAGE);
        }
    }

    public void issueLotto() {
        this.issuedLotto = generateLotto(money.calculateCounts(), new AutoLottoGenerator());
    }

    private List<Lotto> generateLotto(final int number, LottoGenerator lottoGenerator) {
        final List<Lotto> issuedLotto = new ArrayList<>();
        Count count = new Count(number);
        while (!count.isEnd()) {
            count = count.decrease();
            issuedLotto.add(lottoGenerator.generate());
        }
        return Collections.unmodifiableList(issuedLotto);
    }

    public List<LottoDto> getIssuedLotto() {
        return issuedLotto.stream()
            .map(LottoDto::from)
            .collect(Collectors.toUnmodifiableList());
    }

    public SortedMap<RankPrize, Integer> calculateResult(final int bonusNumberInput) {
        this.bonusNumber = new LottoNumber(bonusNumberInput);
        if (isBonusNumberContain(this.lastWinLotto, bonusNumber)) {
            throw new IllegalArgumentException(ERROR_BONUS_NUMBER_CONTAIN_MESSAGE);
        }
        return extractRankCount(this.issuedLotto);
    }

    private boolean isBonusNumberContain(final Lotto lotto, final LottoNumber bonusNumber) {
        return lotto.isContainNumber(bonusNumber);
    }

    private Lotto convertToLotto(final LottoDto lottoDto) {
        return new Lotto(lottoDto.get().stream()
            .map(lottoNumberDto -> new LottoNumber(lottoNumberDto.getNumber()))
            .collect(Collectors.toList()));
    }

    private SortedMap<RankPrize, Integer> extractRankCount(final List<Lotto> issuedLotto) {
        SortedMap<RankPrize, Integer> rankCount = new TreeMap<>(Collections.reverseOrder());
        initRank(rankCount);
        for (Lotto lotto : issuedLotto) {
            countRankedLotto(rankCount, lotto);
        }
        return rankCount;
    }

    private void initRank(final SortedMap<RankPrize, Integer> rankCount) {
        Arrays.stream(RankPrize.values())
            .forEach(e -> rankCount.put(e, RANK_COUNT_INIT_NUMBER));
    }

    private void countRankedLotto(final SortedMap<RankPrize, Integer> rankCount, final Lotto lotto) {
        final MatchedCount matchedCount = getMatchedCount(lotto);
        if (matchedCount.isInRank()) {
            final RankPrize rankPrize = matchedCount.findRankPrice(checkBonus(lotto));
            rankCount.put(rankPrize, rankCount.get(rankPrize) + RANK_COUNT_UNIT);
        }
    }

    private MatchedCount getMatchedCount(final Lotto lotto) {
        return new MatchedCount(lotto.compare(this.lastWinLotto));
    }

    private boolean checkBonus(final Lotto lotto) {
        return lotto.isContainNumber(bonusNumber);
    }

    public double calculateProfit(final SortedMap<RankPrize, Integer> rankCounts) {
        int totalWinPrice = INIT_WIN_PRICE;
        for (RankPrize rankPrize : rankCounts.keySet()) {
            totalWinPrice += rankPrize.getPrice() * rankCounts.get(rankPrize);
        }
        return money.calculateProfit(totalWinPrice);
    }
}
