package microarch.delivery.core.domain.model.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VolumeTest {

    /**
     * Сценарий: успешное создание Volume с валидным значением.
     * <p>
     * Given: передано валидное значение объема value = 3.
     * When: вызывается фабричный метод Volume.create.
     * Then: возвращается успешный результат, а Volume содержит переданное значение.
     */
    @Test
    @DisplayName("Успешное создание Volume с валидным значением")
    void create_whenValueIsValid_shouldReturnSuccess() {
        var result = Volume.create(3);

        assertTrue(result.isSuccess());

        var volume = result.getValue();

        assertEquals(3, volume.getValue());
    }

    /**
     * Сценарий: успешное создание Volume с минимально допустимым значением.
     * <p>
     * Given: передано минимально допустимое значение объема value = 1.
     * When: вызывается фабричный метод Volume.create.
     * Then: возвращается успешный результат, а Volume содержит значение value = 1.
     */
    @Test
    @DisplayName("Успешное создание Volume с минимально допустимым значением")
    void create_whenValueIsMinBoundary_shouldReturnSuccess() {
        var result = Volume.create(1);

        assertTrue(result.isSuccess());

        var volume = result.getValue();

        assertEquals(1, volume.getValue());
    }

    /**
     * Сценарий: ошибка создания Volume, если значение меньше минимально допустимого.
     * <p>
     * Given: передано значение объема value = 0, которое меньше минимально допустимого.
     * When: вызывается фабричный метод Volume.create.
     * Then: возвращается результат с ошибкой.
     */
    @Test
    @DisplayName("Ошибка создания Volume, если значение меньше минимально допустимого")
    void create_whenValueIsLessThanMin_shouldReturnFailure() {
        var result = Volume.create(0);

        assertTrue(result.isFailure());
    }

    /**
     * Сценарий: две Volume с одинаковым значением должны быть эквивалентны.
     * <p>
     * Given: созданы две Volume с одинаковым значением value = 3.
     * When: выполняется сравнение объектов через equals.
     * Then: объекты считаются равными и имеют одинаковый hashCode.
     */
    @Test
    @DisplayName("Две Volume с одинаковым значением должны быть эквивалентны")
    void equals_whenVolumesHaveSameValue_shouldBeEqual() {
        var first = Volume.create(3).getValue();
        var second = Volume.create(3).getValue();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    /**
     * Сценарий: две Volume с разными значениями не должны быть эквивалентны.
     * <p>
     * Given: созданы две Volume с разными значениями value = 3 и value = 5.
     * When: выполняется сравнение объектов через equals.
     * Then: объекты не считаются равными.
     */
    @Test
    @DisplayName("Две Volume с разными значениями не должны быть эквивалентны")
    void equals_whenVolumesHaveDifferentValues_shouldNotBeEqual() {
        var first = Volume.create(3).getValue();
        var second = Volume.create(5).getValue();

        assertNotEquals(first, second);
    }
}
