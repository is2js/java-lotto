package domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class LottoService {

    private static final int LOTTO_SIZE = 6;
    private static final int RANK_COUNT_UNIT = 1;
    private static final int RANK_COUNT_INIT_NUMBER = 0;
    private static final int LOTTO_NUMBER_MAX = 45;
    private static final int LOTTO_NUMBER_UNIT_TO_CORRECT = 1;
    private static final int INIT_WIN_PRICE = 0;

    private final Money money;
    private Lotto lastWinLotto;
    private LottoNumber bonusNumber;

    public LottoService(final Money money) {
        this.money = money;
    }

    public List<Lotto> issueLotto() {
        final ArrayList<Lotto> issuedLotto = generateLotto(money.calculateCounts());
        return Collections.unmodifiableList(issuedLotto);
    }

    private ArrayList<Lotto> generateLotto(int number) {
        return issueLottoWithCount(number);
    }

    private ArrayList<Lotto> issueLottoWithCount(final int number) {
        final ArrayList<Lotto> issuedLotto = new ArrayList<>();
        Count count = new Count(number);
        while (!count.isEnd()) {
            count = count.decrease();
            issuedLotto.add(generateAutoLotto());
        }
        return issuedLotto;
    }

    private Lotto generateAutoLotto() {
        return getAutoLottoFrom(generateAutoLottoNumbers());
    }

    private HashSet<LottoNumber> generateAutoLottoNumbers() {
        HashSet<LottoNumber> autoLottoNumbers = new HashSet<>();
        while (autoLottoNumbers.size() < LOTTO_SIZE) {
            autoLottoNumbers.add(
                new LottoNumber(
                    ThreadLocalRandom.current().nextInt(LOTTO_NUMBER_MAX) + LOTTO_NUMBER_UNIT_TO_CORRECT));
        }
        return autoLottoNumbers;
    }

    private Lotto getAutoLottoFrom(final HashSet<LottoNumber> autoLottoNumbers) {
        return new Lotto(autoLottoNumbers.stream()
            .sorted()
            .collect(Collectors.toList()));
    }

    public SortedMap<RankPrice, Integer> run(final Lotto lastWinLotto, final LottoNumber bonusNumber,
                                             final List<Lotto> issuedLotto) {
        this.lastWinLotto = lastWinLotto;
        this.bonusNumber = bonusNumber;
        return extractRankCount(issuedLotto);
    }

    private SortedMap<RankPrice, Integer> extractRankCount(final List<Lotto> issuedLotto) {
        SortedMap<RankPrice, Integer> rankCount = new TreeMap<>(Collections.reverseOrder());
        initRank(rankCount);
        for (Lotto lotto : issuedLotto) {
            countRankedLotto(rankCount, lotto);
        }
        return rankCount;
    }

    private void initRank(final SortedMap<RankPrice, Integer> rankCount) {
        Arrays.stream(RankPrice.values())
            .forEach(e -> rankCount.put(e, RANK_COUNT_INIT_NUMBER));
    }

    private void countRankedLotto(final SortedMap<RankPrice, Integer> rankCount, final Lotto lotto) {
        final MatchedCount matchedCount = getMatchedCount(lotto);
        if (matchedCount.isInRank()) {
            final RankPrice rankPrice = matchedCount.findRankPrice(checkBonus(lotto));
            rankCount.put(rankPrice, rankCount.get(rankPrice) + RANK_COUNT_UNIT);
        }
    }

    private MatchedCount getMatchedCount(final Lotto lotto) {
        return new MatchedCount(lotto.compare(this.lastWinLotto));
    }

    private boolean checkBonus(final Lotto lotto) {
        return lotto.isContainNumber(bonusNumber);
    }

    public double calculateProfit(final SortedMap<RankPrice, Integer> rankCounts) {
        int totalWinPrice = INIT_WIN_PRICE;
        for (RankPrice rankPrice : rankCounts.keySet()) {
            totalWinPrice += rankPrice.getPrice() * rankCounts.get(rankPrice);
        }
        return money.calculateProfit(totalWinPrice);
    }
}
