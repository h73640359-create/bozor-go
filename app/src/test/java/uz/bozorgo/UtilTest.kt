package uz.bozorgo

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import uz.bozorgo.core.util.MoneyFormatter
import uz.bozorgo.core.util.TextNormalizer
import uz.bozorgo.core.util.humanReadableSize
import uz.bozorgo.core.util.isValidUzPhone

class UtilTest {

    @Test
    fun moneyIsFormattedWithSpaces() {
        // guruh ajratuvchi — uzilmaydigan bo'shliq (U+00A0)
        val space = '\u00A0'
        assertThat(MoneyFormatter.formatUzs(18_900.0)).isEqualTo("18${space}900 so'm")
        assertThat(MoneyFormatter.formatUzs(1_250_000.0)).isEqualTo("1${space}250${space}000 so'm")
        assertThat(MoneyFormatter.formatUzs(0.0)).isEqualTo("0 so'm")
    }

    @Test
    fun apostrophesAndCaseAreIgnored() {
        assertThat(TextNormalizer.normalize("O‘ZBEK")).isEqualTo("o'zbek")
        assertThat(TextNormalizer.normalize("  Go'sht  ")).isEqualTo("go'sht")
        assertThat(TextNormalizer.matches("Olma Golden 1 kg", "olma")).isTrue()
        assertThat(TextNormalizer.matches("Olma Golden 1 kg", "GOLDEN")).isTrue()
        assertThat(TextNormalizer.matches("Olma Golden 1 kg", "nok")).isFalse()
        assertThat(TextNormalizer.matches("Sut 2,5% 1 litr", "sut litr")).isTrue()
    }

    @Test
    fun phoneValidationAcceptsUzbekNumbers() {
        assertThat(isValidUzPhone("+998 90 123 45 67")).isTrue()
        assertThat(isValidUzPhone("998901234567")).isTrue()
        assertThat(isValidUzPhone("90 123 45 67")).isTrue()
        assertThat(isValidUzPhone("123")).isFalse()
        assertThat(isValidUzPhone("")).isFalse()
        assertThat(isValidUzPhone("+998901234567890")).isFalse()
    }

    @Test
    fun fileSizeIsHumanReadable() {
        assertThat(humanReadableSize(512)).isEqualTo("512 B")
        assertThat(humanReadableSize(2048)).isEqualTo("2.0 KB")
        assertThat(humanReadableSize(5 * 1024 * 1024)).isEqualTo("5.0 MB")
    }
}
