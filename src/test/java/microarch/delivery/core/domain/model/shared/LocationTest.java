package microarch.delivery.core.domain.model.shared;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocationTest {

    /**
     * Сценарий: успешное создание Location с валидными координатами.
     * <p>
     * Given: переданы валидные координаты x = 5, y = 7.
     * When: вызывается фабричный метод Location.create.
     * Then: возвращается успешный результат, а Location содержит переданные координаты.
     */
    @Test
    @DisplayName("Успешное создание Location с валидными координатами")
    void create_whenCoordinatesAreValid_shouldReturnSuccess() {
        var result = Location.create(5, 7);

        assertTrue(result.isSuccess());

        var location = result.getValue();

        assertEquals(5, location.getX());
        assertEquals(7, location.getY());
    }

    /**
     * Сценарий: успешное создание Location с минимально допустимыми координатами.
     * <p>
     * Given: переданы минимально допустимые координаты x = 1, y = 1.
     * When: вызывается фабричный метод Location.create.
     * Then: возвращается успешный результат, а Location содержит координаты x = 1, y = 1.
     */
    @Test
    @DisplayName("Успешное создание Location с минимально допустимыми координатами")
    void create_whenCoordinatesAreMinBoundary_shouldReturnSuccess() {
        var result = Location.create(1, 1);

        assertTrue(result.isSuccess());

        var location = result.getValue();

        assertEquals(1, location.getX());
        assertEquals(1, location.getY());
    }

    /**
     * Сценарий: успешное создание Location с максимально допустимыми координатами.
     * <p>
     * Given: переданы максимально допустимые координаты x = 10, y = 10.
     * When: вызывается фабричный метод Location.create.
     * Then: возвращается успешный результат, а Location содержит координаты x = 10, y = 10.
     */
    @Test
    @DisplayName("Успешное создание Location с максимально допустимыми координатами")
    void create_whenCoordinatesAreMaxBoundary_shouldReturnSuccess() {
        var result = Location.create(10, 10);

        assertTrue(result.isSuccess());

        var location = result.getValue();

        assertEquals(10, location.getX());
        assertEquals(10, location.getY());
    }

    /**
     * Сценарий: ошибка создания Location, если координата X меньше минимально допустимого значения.
     * <p>
     * Given: передана координата x = 0, которая меньше минимально допустимого значения, и валидная координата y = 5.
     * When: вызывается фабричный метод Location.create.
     * Then: возвращается результат с ошибкой.
     */
    @Test
    @DisplayName("Ошибка создания Location, если координата X меньше минимально допустимого значения")
    void create_whenXIsLessThanMin_shouldReturnFailure() {
        var result = Location.create(0, 5);

        assertTrue(result.isFailure());
    }

    /**
     * Сценарий: ошибка создания Location, если координата Y меньше минимально допустимого значения.
     * <p>
     * Given: передана валидная координата x = 5 и координата y = 0, которая меньше минимально допустимого значения.
     * When: вызывается фабричный метод Location.create.
     * Then: возвращается результат с ошибкой.
     */
    @Test
    @DisplayName("Ошибка создания Location, если координата Y меньше минимально допустимого значения")
    void create_whenYIsLessThanMin_shouldReturnFailure() {
        var result = Location.create(5, 0);

        assertTrue(result.isFailure());
    }

    /**
     * Сценарий: ошибка создания Location, если координата X больше максимально допустимого значения.
     * <p>
     * Given: передана координата x = 11, которая больше максимально допустимого значения, и валидная координата y = 5.
     * When: вызывается фабричный метод Location.create.
     * Then: возвращается результат с ошибкой.
     */
    @Test
    @DisplayName("Ошибка создания Location, если координата X больше максимально допустимого значения")
    void create_whenXIsGreaterThanMax_shouldReturnFailure() {
        var result = Location.create(11, 5);

        assertTrue(result.isFailure());
    }

    /**
     * Сценарий: ошибка создания Location, если координата Y больше максимально допустимого значения.
     * <p>
     * Given: передана валидная координата x = 5 и координата y = 11, которая больше максимально допустимого значения.
     * When: вызывается фабричный метод Location.create.
     * Then: возвращается результат с ошибкой.
     */
    @Test
    @DisplayName("Ошибка создания Location, если координата Y больше максимально допустимого значения")
    void create_whenYIsGreaterThanMax_shouldReturnFailure() {
        var result = Location.create(5, 11);

        assertTrue(result.isFailure());
    }

    /**
     * Сценарий: две Location с одинаковыми координатами должны быть эквивалентны.
     * <p>
     * Given: созданы две Location с одинаковыми координатами x = 3, y = 4.
     * When: выполняется сравнение объектов через equals.
     * Then: объекты считаются равными и имеют одинаковый hashCode.
     */
    @Test
    @DisplayName("Две Location с одинаковыми координатами должны быть эквивалентны")
    void equals_whenLocationsHaveSameCoordinates_shouldBeEqual() {
        var first = Location.create(3, 4).getValue();
        var second = Location.create(3, 4).getValue();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    /**
     * Сценарий: две Location с разными координатами X не должны быть эквивалентны.
     * <p>
     * Given: созданы две Location с одинаковой координатой Y, но разными координатами X.
     * When: выполняется сравнение объектов через equals.
     * Then: объекты не считаются равными.
     */
    @Test
    @DisplayName("Две Location с разными координатами X не должны быть эквивалентны")
    void equals_whenLocationsHaveDifferentX_shouldNotBeEqual() {
        var first = Location.create(3, 4).getValue();
        var second = Location.create(5, 4).getValue();

        assertNotEquals(first, second);
    }

    /**
     * Сценарий: два Location с разными координатами Y не должны быть эквивалентны.
     * <p>
     * Given: созданы два Location с одинаковой координатой X, но разными координатами Y.
     * When: выполняется сравнение объектов через equals.
     * Then: объекты не считаются равными.
     */
    @Test
    @DisplayName("Два Location с разными координатами Y не должны быть эквивалентны")
    void equals_whenLocationsHaveDifferentY_shouldNotBeEqual() {
        var first = Location.create(3, 4).getValue();
        var second = Location.create(3, 6).getValue();

        assertNotEquals(first, second);
    }

    /**
     * Сценарий: расчёт расстояния между двумя Location.
     * <p>
     * Given: есть начальный Location с координатами x = 2, y = 6 и конечный Location с координатами x = 4, y = 9.
     * When: вызывается метод distanceTo.
     * Then: возвращается расстояние 5, рассчитанное как сумма шагов по X и Y.
     */
    @Test
    @DisplayName("Расчёт расстояния между двумя Location")
    void distanceTo_whenLocationsAreDifferent_shouldReturnManhattanDistance() {
        var first = Location.create(2, 6).getValue();
        var second = Location.create(4, 9).getValue();

        var distance = first.distanceTo(second);

        assertEquals(5, distance);
    }

    /**
     * Сценарий: расстояние между одинаковыми Location равно нулю.
     * <p>
     * Given: есть два Location с одинаковыми координатами x = 5, y = 5.
     * When: вызывается метод distanceTo.
     * Then: возвращается расстояние 0.
     */
    @Test
    @DisplayName("Расстояние между одинаковыми Location равно нулю")
    void distanceTo_whenLocationsAreSame_shouldReturnZero() {
        var first = Location.create(5, 5).getValue();
        var second = Location.create(5, 5).getValue();

        var distance = first.distanceTo(second);

        assertEquals(0, distance);
    }

    /**
     * Сценарий: расстояние между Location рассчитывается одинаково в обе стороны.
     * <p>
     * Given: есть два Location с координатами x = 2, y = 3 и x = 8, y = 9.
     * When: расстояние рассчитывается от первого Location ко второму и от второго Location к первому.
     * Then: оба результата равны, так как расстояние не зависит от направления движения.
     */
    @Test
    @DisplayName("Расстояние между Location рассчитывается одинаково в обе стороны")
    void distanceTo_whenCalledInBothDirections_shouldReturnSameDistance() {
        var first = Location.create(2, 3).getValue();
        var second = Location.create(8, 9).getValue();

        var distanceFromFirstToSecond = first.distanceTo(second);
        var distanceFromSecondToFirst = second.distanceTo(first);

        assertEquals(distanceFromFirstToSecond, distanceFromSecondToFirst);
        assertEquals(12, distanceFromFirstToSecond);
    }
}