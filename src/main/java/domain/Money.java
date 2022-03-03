package domain;

public class Money {

    private static final int LOTTO_PRICE = 1_000;
    private static final String ERROR_LOWER_THAN_LOTTO_PRICE_MESSAGE = "원 미만은 입력할 수 없습니다.";

    private final int money;

    public Money(final int input) {
        validateMoneyRange(input);
        this.money = input;
    }

    private void validateMoneyRange(final int money) {
        if (money < LOTTO_PRICE) {
            throw new IllegalArgumentException(LOTTO_PRICE + ERROR_LOWER_THAN_LOTTO_PRICE_MESSAGE);
        }
    }

    public int getLottoCount() {
        return this.money / LOTTO_PRICE;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        final Money money1 = (Money) object;

        return money == money1.money;
    }

    @Override
    public int hashCode() {
        return money;
    }
}
