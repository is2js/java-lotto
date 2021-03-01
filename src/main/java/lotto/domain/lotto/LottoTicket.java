package lotto.domain.lotto;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class LottoTicket {

    public static final BigInteger PRICE = BigInteger.valueOf(1000);
    public static final int SIZE_OF_LOTTO_NUMBERS = 6;

    private final List<LottoNumber> lottoNumbers;

    public LottoTicket(List<LottoNumber> lottoNumbers) {
        lottoNumbers = new ArrayList<>(lottoNumbers);

        if (invalidSizeOrContainsDuplicatedNumbers(lottoNumbers)) {
            throw new IllegalArgumentException(
                    String.format("로또는 중복된 수 없이 %d개의 숫자로 이루어져야 합니다.", SIZE_OF_LOTTO_NUMBERS));
        }

        this.lottoNumbers = lottoNumbers;
    }

    private boolean invalidSizeOrContainsDuplicatedNumbers(List<LottoNumber> lottoNumbers) {
        Set<LottoNumber> set = new HashSet<>(lottoNumbers);
        return lottoNumbers.size() != SIZE_OF_LOTTO_NUMBERS || set.size() != lottoNumbers.size();
    }

    public LottoTicket(String lottoNumbersValue) {
        this(covertToLottoNumbers(lottoNumbersValue));
    }

    private static List<LottoNumber> covertToLottoNumbers(String lottoNumbersValue) {
        return Arrays.stream(lottoNumbersValue.split(","))
                .map(LottoNumber::valueOf)
                .collect(Collectors.toList());
    }

    public boolean contains(LottoNumber lottoNumber) {
        return lottoNumbers.contains(lottoNumber);
    }

    public List<LottoNumber> toUnmodifiableList() {
        return Collections.unmodifiableList(lottoNumbers);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LottoTicket that = (LottoTicket) o;
        return Objects.equals(lottoNumbers, that.lottoNumbers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lottoNumbers);
    }
}